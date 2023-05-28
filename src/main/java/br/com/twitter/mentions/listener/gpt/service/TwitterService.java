package br.com.twitter.mentions.listener.gpt.service;

import br.com.twitter.mentions.listener.gpt.client.chatgpt.ChatGPTHttpClient;
import br.com.twitter.mentions.listener.gpt.client.twitter.TwitterHttpClient;
import br.com.twitter.mentions.listener.gpt.model.chatgpt.Choice;
import br.com.twitter.mentions.listener.gpt.model.chatgpt.Message;
import br.com.twitter.mentions.listener.gpt.model.chatgpt.OutputResponse;
import br.com.twitter.mentions.listener.gpt.model.twitter.ReferencedTweet;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class TwitterService {

    public static final String BOT_USER_ID = "1657210389795422210";
    public static final String REFERENCED_TWEET_REPLY_TYPE = "replied_to";
    private final TwitterHttpClient twitterHttpClient = new TwitterHttpClient();

    private final ChatGPTHttpClient chatGPTHttpClient = new ChatGPTHttpClient();

    public boolean run() throws URISyntaxException, IOException, InterruptedException {

        final var mentionsResponses = twitterHttpClient.getMentionsFromUserId(BOT_USER_ID).data();

        mentionsResponses.forEach(tweetData -> {
            if (tweetData.referencedTweets() == null)
                return;

            final var referencedTweetId = getReferencedTweetId(tweetData.referencedTweets());

            final var tweetIdToBeReplied = tweetData.id();

            final String repliedTweetText;
            try {
                repliedTweetText = twitterHttpClient.getTweetDataByTweetId(referencedTweetId).data().text();
            } catch (URISyntaxException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            final String generatedTweetOutput;

            try {
                generatedTweetOutput = chatGPTHttpClient.generateOutput(repliedTweetText).choices().stream().findFirst().map(Choice::message).map(Message::content).orElse(null);
            } catch (URISyntaxException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (generatedTweetOutput == null)
                return;

            try {
                twitterHttpClient.postTweet(generatedTweetOutput, tweetIdToBeReplied);
            } catch (URISyntaxException | InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }

        });

//        System.out.println(twitterHttpClient.postTweet("Teste Twitter API", "1660127933380296704"));
//        final var tweetIdToBeReplied = twitterHttpClient.getMentionsFromUserId("1657210389795422210").data().get(0).referencedTweets().stream().filter(referencedTweets -> "replied_to".equals(referencedTweets.type())).findAny().map(ReferencedTweets::id).orElse(null);
//
//        System.out.println(tweetIdToBeReplied);
//        System.out.println(twitterHttpClient.getTweetDataByTweetId(tweetIdToBeReplied).data().text());

        return false;
    }

    private static String getReferencedTweetId(final List<ReferencedTweet> referencedTweets) {
        return referencedTweets.stream().filter(referencedTweet -> REFERENCED_TWEET_REPLY_TYPE.equals(referencedTweet.type())).findAny().map(ReferencedTweet::id).orElse(null);
    }

}
