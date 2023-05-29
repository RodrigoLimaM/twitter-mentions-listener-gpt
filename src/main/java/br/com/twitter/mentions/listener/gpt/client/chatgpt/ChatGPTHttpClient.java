package br.com.twitter.mentions.listener.gpt.client.chatgpt;

import br.com.twitter.mentions.listener.gpt.model.chatgpt.ChatGPTInputRequest;
import br.com.twitter.mentions.listener.gpt.model.chatgpt.Message;
import br.com.twitter.mentions.listener.gpt.model.chatgpt.OutputResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

@Slf4j
public class ChatGPTHttpClient {

    public static final String CHAT_GPT_BASE_URL = "https://api.openai.com/v1";
    public static final String GPT_MODEL = "gpt-3.5-turbo";
    public static final String SYSTEM_DEFAULT_INPUT = "Você é um comediante de stand up genérico e um rapper, suas respostas sempre são estereotipadas e com rimas, com pelo menos uma piadinha infame. O texto não deve ultrapassar 280 caracteres.";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public OutputResponse generateOutput(final String tweetText) throws URISyntaxException, IOException, InterruptedException {
        final var endpoint = "/chat/completions";
        final var httpRequest = HttpRequest.newBuilder()
                .uri(new URI(CHAT_GPT_BASE_URL + endpoint))
                .POST(
                        HttpRequest.BodyPublishers.ofString(
                                objectMapper.writeValueAsString(
                                        buildChatGPTInputRequest(tweetText)
                                )
                        )
                )
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " +System.getenv("CHAT_GPT_API_KEY"))
                .build();

        final var outputResponse = objectMapper.readValue(httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body(), OutputResponse.class);
        log.info("Output generated reponse={}", outputResponse);
        return outputResponse;
    }

    private static ChatGPTInputRequest buildChatGPTInputRequest(final String tweetText) {
        return ChatGPTInputRequest.builder()
                .model(GPT_MODEL)
                .messages(Arrays.asList(
                        Message.builder()
                                .role("system")
                                .content(SYSTEM_DEFAULT_INPUT)
                                .build(),
                        Message.builder()
                                .role("user")
                                .content(tweetText)
                                .build()
                ))
                .build();
    }

}
