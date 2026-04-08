package Wigell.Sushi.API.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class CurrencyService {

    private final WebClient webClient;

    public CurrencyService(WebClient.Builder webClientBuilder) {
        // Ersätt med Kristinas faktiska URL när hon är klar
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
    }

    public double convertSekToJpy(double amountSek) {
        try {
            // Här antar vi att hennes API ser ut så här: /api/v1/convert?from=SEK&to=JPY&amount=100
            Double rate = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/convert")
                            .queryParam("from", "SEK")
                            .queryParam("to", "JPY")
                            .queryParam("amount", amountSek)
                            .build())
                    .retrieve()
                    .bodyToMono(Double.class)
                    .block(); // .block() används här för att hålla det enkelt/synkront

            return rate != null ? rate : amountSek * 14.5; // Fallback till fast kurs
        } catch (Exception e) {
            // Om hennes tjänst är nere, använd fast växelkurs för att appen inte ska krascha
            return amountSek * 14.5;
        }
    }
}