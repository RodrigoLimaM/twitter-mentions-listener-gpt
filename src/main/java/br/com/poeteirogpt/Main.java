package br.com.poeteirogpt;

import java.io.IOException;
import java.net.URISyntaxException;
import br.com.poeteirogpt.client.OAuth1HeaderGenerator;
import br.com.poeteirogpt.client.TwitterHttpClient;

public class Main {

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        TwitterHttpClient twitterHttpClient = new TwitterHttpClient();

        System.out.println(twitterHttpClient.postTweet("Teste Twitter API"));
    }


}
