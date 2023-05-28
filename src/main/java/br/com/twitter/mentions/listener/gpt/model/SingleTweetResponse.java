package br.com.twitter.mentions.listener.gpt.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SingleTweetResponse(TweetData data) {

}
