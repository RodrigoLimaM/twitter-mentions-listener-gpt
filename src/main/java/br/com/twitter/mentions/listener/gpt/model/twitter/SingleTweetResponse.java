package br.com.twitter.mentions.listener.gpt.model.twitter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SingleTweetResponse(TweetData data) {

}
