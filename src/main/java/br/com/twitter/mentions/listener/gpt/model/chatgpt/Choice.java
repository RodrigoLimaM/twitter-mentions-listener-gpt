package br.com.twitter.mentions.listener.gpt.model.chatgpt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Choice(Message message) {

}
