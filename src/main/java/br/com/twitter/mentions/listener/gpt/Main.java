package br.com.twitter.mentions.listener.gpt;

import br.com.twitter.mentions.listener.gpt.client.TwitterHttpClient;
import br.com.twitter.mentions.listener.gpt.model.ReferencedTweets;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        TwitterHttpClient twitterHttpClient = new TwitterHttpClient();

//        System.out.println(twitterHttpClient.postTweet("Teste Twitter API", "1660127933380296704"));


        final var tweetIdToBeReplied = twitterHttpClient.getMentionsFromUserId("1657210389795422210").data().get(0).referencedTweets().stream().filter(referencedTweets -> "replied_to".equals(referencedTweets.type())).findAny().map(ReferencedTweets::id).orElse(null);

        System.out.println(tweetIdToBeReplied);
        System.out.println(twitterHttpClient.getTweetDataByTweetId(tweetIdToBeReplied).data().text());

    }


}
