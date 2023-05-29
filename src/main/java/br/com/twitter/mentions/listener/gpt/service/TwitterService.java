package br.com.twitter.mentions.listener.gpt.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import br.com.twitter.mentions.listener.gpt.client.chatgpt.ChatGPTHttpClient;
import br.com.twitter.mentions.listener.gpt.client.twitter.TwitterHttpClient;
import br.com.twitter.mentions.listener.gpt.model.chatgpt.Choice;
import br.com.twitter.mentions.listener.gpt.model.chatgpt.Message;
import br.com.twitter.mentions.listener.gpt.model.twitter.ReferencedTweet;
import br.com.twitter.mentions.listener.gpt.model.twitter.TweetData;

public class TwitterService {

    public static final String BOT_USER_ID = "1657210389795422210";
    public static final String REFERENCED_TWEET_REPLY_TYPE = "replied_to";
    private final TwitterHttpClient twitterHttpClient = new TwitterHttpClient();

    private final ChatGPTHttpClient chatGPTHttpClient = new ChatGPTHttpClient();

    private final RedisService redisService = new RedisService();

    public boolean run() throws URISyntaxException, IOException, InterruptedException {

        final var mentionsResponses = twitterHttpClient.getMentionsFromUserId(BOT_USER_ID).data();

        mentionsResponses.forEach(tweetData -> {
            final var tweetIdToBeReplied = tweetData.id();

            if (isElegibleToReply(tweetData, tweetIdToBeReplied))
                return;

            final var referencedTweetId = getReferencedTweetId(tweetData.referencedTweets());

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

            redisService.set(tweetIdToBeReplied);
        });

        return false;
    }

    private boolean isElegibleToReply(TweetData tweetData, String tweetIdToBeReplied) {
        return tweetData.referencedTweets() == null || redisService.get(tweetIdToBeReplied) != null;
    }

    private static String getReferencedTweetId(final List<ReferencedTweet> referencedTweets) {
        return referencedTweets.stream().filter(referencedTweet -> REFERENCED_TWEET_REPLY_TYPE.equals(referencedTweet.type())).findAny().map(ReferencedTweet::id).orElse(null);
    }

}
