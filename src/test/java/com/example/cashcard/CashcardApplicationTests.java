package com.example.cashcard;

import com.example.cashcard.model.CashCard;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.sun.tools.jconsole.JConsoleContext;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ResourceBanner;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import java.net.URI;

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
		CashCard newCashCard = new CashCard(null, 250.00, null);
		ResponseEntity<Void> response = restTemplate.withBasicAuth("sarah1","abc123").postForEntity("/cashcards", newCashCard, Void.class);
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
		System.out.println(response.getBody());
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
		HttpEntity<CashCard> request = new HttpEntity<>(new CashCard(null,19.99,null));

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

		HttpEntity<CashCard> request = new HttpEntity<CashCard>(new CashCard(null,888.88,null));
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1","abc123")
				.exchange("/cashcards/10000", HttpMethod.PUT, request, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	public void shouldNotUpdateACashCardThatIsOwnedBySomeoneElse(){
		HttpEntity<CashCard> request = new HttpEntity<CashCard>(new CashCard(null,33.33,null));
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
}
