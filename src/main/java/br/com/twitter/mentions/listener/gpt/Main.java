package br.com.twitter.mentions.listener.gpt;

import br.com.twitter.mentions.listener.gpt.service.TwitterService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    static TwitterService twitterService = new TwitterService();

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        executorService.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Starting iteration...");
                twitterService.run();
                System.out.println("Iteration finished.");
            } catch (URISyntaxException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, 0, 60, TimeUnit.SECONDS);

    }

}
