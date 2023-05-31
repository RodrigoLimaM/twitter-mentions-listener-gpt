package br.com.twitter.mentions.listener.gpt;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import br.com.twitter.mentions.listener.gpt.service.TwitterService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    static TwitterService twitterService = new TwitterService();

    public static void main(String[] args) {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
                log.info("Starting iteration...");
                twitterService.run();
                log.info("Iteration finished.");
        }, 0, 60, TimeUnit.SECONDS);
    }

}
