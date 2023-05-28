package br.com.twitter.mentions.listener.gpt.model.twitter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MentionResponse(List<TweetData> data) {

}
