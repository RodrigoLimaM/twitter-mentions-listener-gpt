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
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

@Slf4j
public class TwitterHttpClient {

    public static final String TWITTER_API_BASE_URL = "https://api.twitter.com/2";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void postTweet(final String text, final String inReplyToTweetId) throws URISyntaxException, IOException, InterruptedException {
        final var endpoint = "/tweets";
        final var oAuth1HeaderGenerator = new TwitterOAuth1HeaderGenerator(false);
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

        final var body = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body();
        log.info("Tweet posted response={}", body);
    }

    public MentionResponse getMentionsFromUserId(final String userId) throws URISyntaxException, IOException, InterruptedException {
        final var endpoint = String.format("/users/%s/mentions", userId);
        final var queryParams = new HashMap<String, String>();
        queryParams.put("tweet.fields", "referenced_tweets");
        final var oAuth1HeaderGenerator = new TwitterOAuth1HeaderGenerator(true);
        final var httpRequest = HttpRequest.newBuilder()
                .uri(buildURI(endpoint, queryParams))
                .GET()
                .header("Authorization", oAuth1HeaderGenerator.generateHeader("GET", TWITTER_API_BASE_URL + endpoint, queryParams))
                .build();

        final var mentionResponse = objectMapper.readValue(httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body(), MentionResponse.class);
        log.info("Mentions obtained response={}", mentionResponse);
        return mentionResponse;
    }

    public SingleTweetResponse getTweetDataByTweetId(final String tweetId) throws URISyntaxException, IOException, InterruptedException {
        final var endpoint = String.format("/tweets/%s", tweetId);
        final var oAuth1HeaderGenerator = new TwitterOAuth1HeaderGenerator(true);
        final var httpRequest = HttpRequest.newBuilder()
                .uri(new URI(TWITTER_API_BASE_URL + endpoint))
                .GET()
                .header("Authorization", oAuth1HeaderGenerator.generateHeader("GET", TWITTER_API_BASE_URL + endpoint, null))
                .build();

        final var singleTweetResponse = objectMapper.readValue(httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body(), SingleTweetResponse.class);
        log.info("Tweet data obtained response={}", singleTweetResponse);
        return singleTweetResponse;
    }

    private URI buildURI(final String endpoint, final Map<String, String> queryParams) throws URISyntaxException {
        return new URIBuilder(TWITTER_API_BASE_URL + endpoint)
                .addParameters(
                        queryParams.entrySet()
                                .stream().map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue())
                                )
                                .collect(Collectors.toList()))
                .build();
    }
}
