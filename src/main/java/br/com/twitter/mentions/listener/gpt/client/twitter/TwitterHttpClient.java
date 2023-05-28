package br.com.twitter.mentions.listener.gpt.client.twitter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import br.com.twitter.mentions.listener.gpt.model.twitter.MentionResponse;
import br.com.twitter.mentions.listener.gpt.model.twitter.PostTweetRequest;
import br.com.twitter.mentions.listener.gpt.model.twitter.Reply;
import br.com.twitter.mentions.listener.gpt.model.twitter.SingleTweetResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

public class TwitterHttpClient {

    public static final String TWITTER_API_BASE_URL = "https://api.twitter.com/2";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String postTweet(final String text, final String inReplyToTweetId) throws URISyntaxException, IOException, InterruptedException {
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
                                objectMapper.writeValueAsString(new PostTweetRequest(text, new Reply(inReplyToTweetId)))
                        )
                )
                .header("Content-Type", "application/json")
                .header("Authorization", oAuth1HeaderGenerator.generateHeader("POST", TWITTER_API_BASE_URL + endpoint, null))
                .build();

        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body();
    }

    public MentionResponse getMentionsFromUserId(final String userId) throws URISyntaxException, IOException, InterruptedException {
        final var endpoint = String.format("/users/%s/mentions", userId);
        final var queryParams = new HashMap<String, String>();
        queryParams.put("tweet.fields", "referenced_tweets");
        final var oAuth1HeaderGenerator = new OAuth1HeaderGenerator(
                System.getenv("ELEVATED_CONSUMER_KEY"),
                System.getenv("ELEVATED_CONSUMER_SECRET"),
                System.getenv("ELEVATED_ACCESS_TOKEN"),
                System.getenv("ELEVATED_TOKEN_SECRET")
        );
        final var httpRequest = HttpRequest.newBuilder()
                .uri(buildURI(endpoint, queryParams))
                .GET()
                .header("Authorization", oAuth1HeaderGenerator.generateHeader("GET", TWITTER_API_BASE_URL + endpoint, queryParams))
                .build();

        return objectMapper.readValue(httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body(), MentionResponse.class);
    }

    public SingleTweetResponse getTweetDataByTweetId(final String tweetId) throws URISyntaxException, IOException, InterruptedException {
        final var endpoint = String.format("/tweets/%s", tweetId);
        final var oAuth1HeaderGenerator = new OAuth1HeaderGenerator(
                System.getenv("ELEVATED_CONSUMER_KEY"),
                System.getenv("ELEVATED_CONSUMER_SECRET"),
                System.getenv("ELEVATED_ACCESS_TOKEN"),
                System.getenv("ELEVATED_TOKEN_SECRET")
        );
        final var httpRequest = HttpRequest.newBuilder()
                .uri(new URI(TWITTER_API_BASE_URL + endpoint))
                .GET()
                .header("Authorization", oAuth1HeaderGenerator.generateHeader("GET", TWITTER_API_BASE_URL + endpoint, null))
                .build();

        return objectMapper.readValue(httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body(), SingleTweetResponse.class);
    }

    private URI buildURI(final String endpoint, final Map<String, String> queryParams) throws URISyntaxException {
        return new URIBuilder(TWITTER_API_BASE_URL + endpoint)
                .addParameters(
                        queryParams.entrySet()
                                .stream().map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue())
                )
                .collect(Collectors.toList())).build();
    }
}
