package br.com.twitter.mentions.listener.gpt.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Reply(@JsonProperty("in_reply_to_tweet_id") String inReplyToTweetId) {

}
