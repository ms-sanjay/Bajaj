package com.example.bfh;

import com.example.bfh.model.GenerateWebhookResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class StartupRunner {

    private final WebClient webClient = WebClient.create();

    private static final String SQL_QUERY = """
            SELECT
                e.EMP_ID,
                e.FIRST_NAME,
                e.LAST_NAME,
                d.DEPARTMENT_NAME,
                COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT
            FROM EMPLOYEE e
            JOIN DEPARTMENT d
            ON e.DEPARTMENT = d.DEPARTMENT_ID
            LEFT JOIN EMPLOYEE e2
            ON e.DEPARTMENT = e2.DEPARTMENT
            AND e2.DOB > e.DOB
            GROUP BY e.EMP_ID, e.FIRST_NAME, e.LAST_NAME, d.DEPARTMENT_NAME
            ORDER BY e.EMP_ID DESC;
    """;

    @PostConstruct
    public void onStartup() {
        System.out.println("Application started. Generating webhook...");

        GenerateWebhookResponse response = webClient.post()
            .uri("https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of(
                "name", "Sanjay",
                "regNo", "22BML0106",
                "email", "mssanjay180@gmail.com"
            ))
            .retrieve()
            .bodyToMono(GenerateWebhookResponse.class)
            .block();

        if (response != null) {
            System.out.println("Webhook generated");
            submitFinalQuery(response.getWebhook(), response.getAccessToken());
        } else {
            System.err.println("Failed to generate webhook.");
        }
    }

    private void submitFinalQuery(String webhookUrl, String accessToken) {
        System.out.println("Submitting final query...");

        webClient.post()
            .uri(webhookUrl)
            .header(HttpHeaders.AUTHORIZATION, accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("finalQuery", SQL_QUERY))
            .retrieve()
            .bodyToMono(String.class)
            .doOnError(e -> System.err.println("Submission failed: " + e.getMessage()))
            .doOnSuccess(response -> System.out.println("Submission response: " + response))
            .block();
    }
}
