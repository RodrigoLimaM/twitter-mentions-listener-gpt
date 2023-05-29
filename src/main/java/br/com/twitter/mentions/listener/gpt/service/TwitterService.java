package br.com.twitter.mentions.listener.gpt.service;

import java.util.List;
import br.com.twitter.mentions.listener.gpt.client.chatgpt.ChatGPTHttpClient;
import br.com.twitter.mentions.listener.gpt.client.twitter.TwitterHttpClient;
import br.com.twitter.mentions.listener.gpt.model.chatgpt.Choice;
import br.com.twitter.mentions.listener.gpt.model.chatgpt.Message;
import br.com.twitter.mentions.listener.gpt.model.twitter.ReferencedTweet;
import br.com.twitter.mentions.listener.gpt.model.twitter.TweetData;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TwitterService {

    public static final String BOT_USER_ID = "1657210389795422210";
    public static final String REFERENCED_TWEET_REPLY_TYPE = "replied_to";
    private final TwitterHttpClient twitterHttpClient = new TwitterHttpClient();

    private final ChatGPTHttpClient chatGPTHttpClient = new ChatGPTHttpClient();

    private final RedisService redisService = new RedisService();

    @SneakyThrows
    public void run() {
        twitterHttpClient.getMentionsFromUserId(BOT_USER_ID)
                .data()
                .forEach(this::handleMention);
    }

    @SneakyThrows
    private void handleMention(TweetData tweetData)  {
        final var tweetIdToBeReplied = tweetData.id();

        if (isEligibleToReply(tweetData, tweetIdToBeReplied)) {
            log.info("Mention not eligible to reply id={}", tweetIdToBeReplied);
            return;
        }

        final var referencedTweetId = getReferencedTweetId(tweetData.referencedTweets());
        final var textOfRepliedTweet = twitterHttpClient.getTweetDataByTweetId(referencedTweetId).data().text();
        final var generatedTweetOutput = generateTweetOutput(textOfRepliedTweet);

        if (generatedTweetOutput == null)
            return;

        twitterHttpClient.postTweet(generatedTweetOutput, tweetIdToBeReplied);

        redisService.set(tweetIdToBeReplied);
    }

    @SneakyThrows
    private String generateTweetOutput(final String textOfRepliedTweet) {
        return chatGPTHttpClient.generateOutput(textOfRepliedTweet)
                .choices()
                .stream()
                .findFirst()
                .map(Choice::message)
                .map(Message::content)
                .orElse(null);
    }

    private boolean isEligibleToReply(TweetData tweetData, String tweetIdToBeReplied) {
        return tweetData.referencedTweets() == null || redisService.get(tweetIdToBeReplied) != null;
    }

    private static String getReferencedTweetId(final List<ReferencedTweet> referencedTweets) {
        return referencedTweets.stream().filter(referencedTweet -> REFERENCED_TWEET_REPLY_TYPE.equals(referencedTweet.type())).findAny().map(ReferencedTweet::id).orElse(null);
    }

}
