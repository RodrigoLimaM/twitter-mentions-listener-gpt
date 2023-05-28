package br.com.twitter.mentions.listener.gpt.model.chatgpt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OutputResponse(List<Choice> choices) {

}
