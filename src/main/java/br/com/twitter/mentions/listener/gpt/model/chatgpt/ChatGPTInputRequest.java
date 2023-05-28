package br.com.twitter.mentions.listener.gpt.model.chatgpt;

import lombok.Builder;

import java.util.List;

@Builder
public record ChatGPTInputRequest(String model, List<Message> messages) {

}
