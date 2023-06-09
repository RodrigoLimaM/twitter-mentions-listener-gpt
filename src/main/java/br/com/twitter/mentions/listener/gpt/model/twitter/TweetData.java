package br.com.twitter.mentions.listener.gpt.model.twitter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TweetData(String id, String text, @JsonProperty("referenced_tweets") List<ReferencedTweet> referencedTweets) {

}
