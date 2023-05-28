package br.com.twitter.mentions.listener.gpt.model.chatgpt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record Message(String role, String content) {

}
