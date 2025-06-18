package com.example.cashcard;

import com.example.cashcard.model.CashCard;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.sun.tools.jconsole.JConsoleContext;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest (webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/data.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)

class CashcardApplicationTests {
	@Autowired
	TestRestTemplate restTemplate;

	@Test
	public void shouldReturnCashCardWhenDataIsSaved(){
		ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/99", String.class);

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
		ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/1000",String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

	}

	@Test
	public void shouldCreateANewCashCard(){
		CashCard newCashCard = new CashCard(null, 250.00);
		ResponseEntity<Void> response = restTemplate.postForEntity("/cashcards", newCashCard, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI location = response.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate.getForEntity(location, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Double amount = documentContext.read("$.amount");
		assertThat(amount).isEqualTo(250.00);
	}

	@Test
	public void shouldReturnAllCashCardsWhenListIsRequested(){
		ResponseEntity<String> response = restTemplate.getForEntity("/cashcards", String.class);
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
		ResponseEntity<String> response = restTemplate.getForEntity("/cashcards?page=0&size=2",String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		System.out.println(response.getBody());
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int cashCardCount = documentContext.read("$.length()");
		assertThat(cashCardCount).isEqualTo(2);
	}

	@Test
	public void shouldReturnASortedPageOfCashCards(){
		ResponseEntity<String> response = restTemplate.getForEntity("/cashcards?page=0&size=2&sort=amount,desc", String.class);
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
		ResponseEntity<String> response = restTemplate.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray cashcards = documentContext.read("$[*]");
		assertThat(cashcards.size()).isEqualTo(3);

		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactly(150.00,123.45,1.00);
	}

}
