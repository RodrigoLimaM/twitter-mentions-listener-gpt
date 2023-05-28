package br.com.poeteirogpt.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TwitterHttpClient {

    public static final String TWITTER_API_BASE_URL = "https://api.twitter.com/2";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String postTweet(final String text) throws URISyntaxException, IOException, InterruptedException {
        final var endpoint = "/tweets";
        final var oAuth1HeaderGenerator = new OAuth1HeaderGenerator(
                System.getenv("CONSUMER_KEY"),
                System.getenv("CONSUMER_SECRET"),
                System.getenv("ACCESS_TOKEN"),
                System.getenv("TOKEN_SECRET")
        );
        final var httpRequest = HttpRequest.newBuilder()
                .uri(new URI(TWITTER_API_BASE_URL + endpoint))
                .POST(
                        HttpRequest.BodyPublishers.ofString(
                                objectMapper.writeValueAsString(new PostTweetRequest(text))
                        )
                )
                .header("Content-Type", "application/json")
                .header("Authorization", oAuth1HeaderGenerator.generateHeader("POST", TWITTER_API_BASE_URL + endpoint, null))
                .build();

        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body();
    }
}
