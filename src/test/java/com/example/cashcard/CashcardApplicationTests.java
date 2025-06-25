package com.example.cashcard;

import com.example.cashcard.dto.CashCardBulkUpdateDTO;
import com.example.cashcard.dto.CashCardRequestDTO;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest (webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/data.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CashcardApplicationTests {
	@Autowired
	TestRestTemplate restTemplate;

	@Test
	public void shouldReturnCashCardWhenDataIsSaved(){
		ResponseEntity<String> response =
				restTemplate
						.withBasicAuth("sarah1","abc123")
				.getForEntity("/cashcards/99", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		assertThat(id).isNotNull();
		assertThat(id).isEqualTo(99);
		assertThat(amount).isEqualTo(123.45);
	}

	@Test
	public void shouldNotReturnCashCardWithAnUnknownId(){
		ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1","abc123").getForEntity("/cashcards/1000",String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	@DirtiesContext
	public void shouldCreateANewCashCard(){
		CashCardRequestDTO cashCardRequestDTO = new CashCardRequestDTO(250.00);
		ResponseEntity<Void> response = restTemplate.withBasicAuth("sarah1","abc123").postForEntity("/cashcards", cashCardRequestDTO, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI location = response.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate.withBasicAuth("sarah1","abc123").getForEntity(location, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Double amount = documentContext.read("$.amount");
		assertThat(amount).isEqualTo(250.00);
	}

	@Test
	public void shouldReturnAllCashCardsWhenListIsRequested(){
		ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1","abc123").getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int cashCardCount = documentContext.read("$.length()");
		assertThat(cashCardCount).isEqualTo(3);

		JSONArray ids = documentContext.read("$..id");
		assertThat(ids).containsExactlyInAnyOrder(99,100,101);

		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactlyInAnyOrder(1.00,123.45,150.00);
	}

	@Test
	public void shouldReturnAPageOfCashCards(){
		// return 1st page with size of 2
		//if change url to /cashcards?page=1&size=2, it will return one card(id=101) on the second page
		ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1","abc123").getForEntity("/cashcards?page=0&size=2",String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int cashCardCount = documentContext.read("$.length()");
		assertThat(cashCardCount).isEqualTo(2);
	}

	@Test
	public void shouldReturnASortedPageOfCashCards(){
		ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1","abc123").getForEntity("/cashcards?page=0&size=2&sort=amount,desc", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray cashcards = documentContext.read("$[*]");
		assertThat(cashcards.size()).isEqualTo(2);

		Double amount0 = documentContext.read("$[0].amount");
		Double amount1 = documentContext.read("$[1].amount");
		assertThat(amount0).isEqualTo(150.00);
		assertThat(amount1).isEqualTo(123.45);
	}

	@Test
	public void shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues(){
		// By default, the sprint returns page = 0, size = 20
		ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1","abc123").getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray cashcards = documentContext.read("$[*]");
		assertThat(cashcards.size()).isEqualTo(3);

		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactly(150.00,123.45,1.00);
	}

	@Test
	public void shouldNotReturnACashCardWhenUsingBadCredentials(){
		ResponseEntity <String> responseBadUser = restTemplate
				.withBasicAuth("badUser","abc123")
				.getForEntity("/cashcards/99",String.class);

		assertThat(responseBadUser.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		ResponseEntity <String> responseBadPassword = restTemplate
				.withBasicAuth("sarah1","badPassword")
				.getForEntity("/cashcards/99",String.class);

		assertThat(responseBadUser.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

	}

	@Test
	public void shouldRejectUsersWhoAreNotCardOwners(){
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("hank-owns-no-cards", "qrs456")
				.getForEntity("/cashcards/99",String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	public void shouldNotAllowAccessToCashCardsTheyDoNotOwn(){
		ResponseEntity<String> response = restTemplate
				.withBasicAuth( "sarah1","abc123")
				.getForEntity("/cashcards/102",String.class);
		//return 404 not 401 unauthorized for security reason, don't want to expose too many info.
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	@DirtiesContext
	public void shouldUpdateAnExistingCashCard(){
		HttpEntity<CashCardRequestDTO> request = new HttpEntity<>(new CashCardRequestDTO(19.99));

		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1","abc123")
				.exchange("/cashcards/99", HttpMethod.PUT,request, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> getResponse = restTemplate.withBasicAuth("sarah1","abc123")
				.getForEntity("/cashcards/99", String.class);
		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		assertThat(id).isEqualTo(99);
		Double amount = documentContext.read("$.amount");
		assertThat(amount).isEqualTo(19.99);
	}

	@Test
	public void shouldNotUpdateACashCardThatDoesNotExist() {

		HttpEntity<CashCardRequestDTO> request = new HttpEntity<>(new CashCardRequestDTO(888.88));
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1","abc123")
				.exchange("/cashcards/10000", HttpMethod.PUT, request, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	public void shouldNotUpdateACashCardThatIsOwnedBySomeoneElse(){
		HttpEntity<CashCardRequestDTO> request = new HttpEntity<>(new CashCardRequestDTO(33.33));
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1","abc123")
				.exchange("/cashcards/102", HttpMethod.PUT, request, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	public void shouldDeleteAnExistingCashCard(){
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1","abc123")
				.exchange("/cashcards/99", HttpMethod.DELETE, null, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("sarah1","abc123")
				.getForEntity("/cashcards/99", String.class);

		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	public void shouldNotDeleteACashCardThatDoesNotExist(){
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1","abc123")
				.exchange("/cashcards/999", HttpMethod.DELETE, null, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

	}

	@Test
	public void shouldNotAllowDeletionOfCashCardsTheyDoNotOwn(){
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1","abc123")
				.exchange("/cashcards/102", HttpMethod.DELETE, null, Void.class);//102 belongs to kumar
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

		//check if kumar's cashcard is still there
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("kumar2","xyz789")
				.getForEntity("/cashcards/102", String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void shouldUpdateSelectedExistCashCards(){
		List<CashCardBulkUpdateDTO> cashcards = List.of(new CashCardBulkUpdateDTO(99L,1.0),
				new CashCardBulkUpdateDTO(100L,2.0)
				);
		HttpEntity<List<CashCardBulkUpdateDTO>> request = new HttpEntity<>(cashcards);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/cashcards/bulk", HttpMethod.PUT, request,Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("sarah1","abc123")
				.getForEntity("/cashcards", String.class);
		DocumentContext documentContext = JsonPath.parse( getResponse.getBody());
		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactly(150.00, 2.0,1.0);
	}

	@Test
	public void shouldNotUpdateAnythingIfNotOwnedCashCardInTheList(){
		List<CashCardBulkUpdateDTO> cashcards = List.of(new CashCardBulkUpdateDTO(99L,1.0),
				new CashCardBulkUpdateDTO(100L,2.0),
				new CashCardBulkUpdateDTO(102L,3.0)
		);
		HttpEntity<List<CashCardBulkUpdateDTO>> request = new HttpEntity<>(cashcards);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/cashcards/bulk", HttpMethod.PUT, request,Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("sarah1","abc123")
				.getForEntity("/cashcards", String.class);
		DocumentContext documentContext = JsonPath.parse( getResponse.getBody());
		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.0,150.0);

		ResponseEntity<String> getResponseKumar = restTemplate
				.withBasicAuth("kumar2","xyz789")
				.getForEntity("/cashcards/102", String.class);
		DocumentContext documentContextKumar = JsonPath.parse( getResponseKumar.getBody());
		Double amount = documentContextKumar.read("$.amount");
		assertThat(amount).isEqualTo(200.00);
	}

	@Test
	public void shouldNotUpdateNotExistCashCard(){
		List<CashCardBulkUpdateDTO> cashcards = List.of(new CashCardBulkUpdateDTO(999L,1.0),
				new CashCardBulkUpdateDTO(1009L,2.0),
				new CashCardBulkUpdateDTO(1029L,3.0)
		);
		HttpEntity<List<CashCardBulkUpdateDTO>> request = new HttpEntity<>(cashcards);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/cashcards/bulk", HttpMethod.PUT, request,Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	public void shouldDeleteAllRequestedCashCards(){
		HttpEntity<List<Long>> request = new HttpEntity<>(List.of(99L,100L));
		ResponseEntity<Void> response = restTemplate.withBasicAuth("sarah1","abc123")
				.exchange("/cashcards/bulk", HttpMethod.DELETE, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> getResponse = restTemplate.withBasicAuth("sarah1","abc123")
				.getForEntity("/cashcards", String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		int count = documentContext.read("$.length()");
		assertThat(count).isEqualTo(1);
		JSONArray ids = documentContext.read("$..id");
		assertThat(ids).containsExactly(101);

	}

	@Test
	public void shouldNotDeleteCashCardsNotOwned(){
		HttpEntity<List<Long>> request = new HttpEntity<>(List.of(99L,100L,102L));
		ResponseEntity<Void> response = restTemplate.withBasicAuth("sarah1","abc123")
				.exchange("/cashcards/bulk", HttpMethod.DELETE, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

		ResponseEntity<String> getResponse = restTemplate.withBasicAuth("sarah1","abc123")
				.getForEntity("/cashcards", String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		int count = documentContext.read("$.length()");
		assertThat(count).isEqualTo(3);
		JSONArray ids = documentContext.read("$..id");
		assertThat(ids).containsExactlyInAnyOrder(99,100,101);

		ResponseEntity<String> getResponseKumar = restTemplate.withBasicAuth("kumar2","xyz789")
				.getForEntity("/cashcards", String.class);
		assertThat(getResponseKumar.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContextKumar = JsonPath.parse(getResponseKumar.getBody());
		int countKumar = documentContextKumar.read("$.length()");
		assertThat(countKumar).isEqualTo(1);
		JSONArray idsKumar = documentContextKumar.read("$..id");
		assertThat(idsKumar).containsExactlyInAnyOrder(102);
	}

	@Test
	public void shouldNotDeleteCashCardsNotExist() {
		HttpEntity<List<Long>> request = new HttpEntity<>(List.of(1000L,2000L));
		ResponseEntity<Void> response = restTemplate.withBasicAuth("sarah1", "abc123")
				.exchange("/cashcards/bulk", HttpMethod.DELETE, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	public void shouldReturnFilteredCashCardsWithDefaultPage(){
		ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1","abc123")
				.getForEntity("/cashcards/filter?min=1&max=160",String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int length = documentContext.read("$.length()");;
		JSONArray amounts = documentContext.read("$..amount");
		assertThat(length).isEqualTo(3);
		assertThat(amounts).containsExactly(150.0,123.45,1.0);
	}

	@Test
	public void shouldReturnFilteredCashCardsWithPage(){
		ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1","abc123")
				.getForEntity("/cashcards/filter?min=1&max=160&page=0&size=3&sort=amount,desc",String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int length = documentContext.read("$.length()");;
		JSONArray amounts = documentContext.read("$..amount");
		assertThat(length).isEqualTo(3);
		assertThat(amounts).containsExactly(150.0,123.45,1.0);
	}

	@Test
	public void shouldReturnFilteredCashCardsWithoutAdminRole(){
		ResponseEntity<String> response = restTemplate.withBasicAuth("kumar2","xyz789")
				.getForEntity("/cashcards/filter?min=1&max=260&page=0&size=3&sort=amount,desc",String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

	}

	@Test
	public void shouldReturnFilteredCashCardsWithInvalidData(){
		ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1","abc123")
				.getForEntity("/cashcards/filter?min=s&max=t&page=0&size=3&sort=amount,desc",String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);


		ResponseEntity<String> responseNull = restTemplate.withBasicAuth("sarah1","abc123")
				.getForEntity("/cashcards/filter?page=0&size=3&sort=amount,desc",String.class);
		assertThat(responseNull.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		ResponseEntity<String> responseEmpty = restTemplate.withBasicAuth("sarah1","abc123")
				.getForEntity("/cashcards/filter?min=&max=&page=0&size=3&sort=amount,desc",String.class);
		assertThat(responseEmpty.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

	}


}
