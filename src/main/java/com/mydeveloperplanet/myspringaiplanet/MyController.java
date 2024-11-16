package com.mydeveloperplanet.myspringaiplanet;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController
class MyController {

    private final ChatClient chatClient;

    public MyController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/basic")
    String basic(@RequestParam String message) {
        return this.chatClient.prompt()
            .user(message)
            .call()
            .content();
    }

    @GetMapping("/chatresponse")
    String chatResponse(@RequestParam String message) {
        ChatResponse chatResponse = this.chatClient.prompt()
                .user(message)
                .call()
                .chatResponse();
        return chatResponse.toString();
    }

    @GetMapping("/entityresponse")
    String entityResponse() {
        ArtistSongs artistSongs = this.chatClient.prompt()
                .user("Generate a list of songs of Bruce Springsteen. Limit the list to 10 songs.")
                .call()
                .entity(ArtistSongs.class);
        return artistSongs.toString();
    }

    record ArtistSongs(String artist, List<String> songs) {}

    @GetMapping("/stream")
    Flux<String> stream(@RequestParam String message) {
        return this.chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }

    @GetMapping("/system")
    String system() {
        return this.chatClient.prompt()
                .system("You are a chat bot who uses quotes of The Terminator when responding.")
                .user("Who is Bruce Springsteen?")
                .call()
                .content();
    }

    private final InMemoryChatMemory chatMemory = new InMemoryChatMemory();

    @GetMapping("/chatmemory")
    String chatMemory(@RequestParam String message) {
        return this.chatClient.prompt()
                .advisors(new MessageChatMemoryAdvisor(chatMemory))
                .user(message)
                .call()
                .content();
    }

    @GetMapping("/promptwhois")
    String promptWhoIs(@RequestParam String name) {

        PromptTemplate promptTemplate = new PromptTemplate("Who is {name}");
        Prompt prompt = promptTemplate.create(Map.of("name", name));

        return this.chatClient.prompt(prompt)
                .call()
                .content();
    }

    @GetMapping("/promptmessages")
    String promptMessages(@RequestParam String movie) {

        Message userMessage = new UserMessage("Telll me a joke");

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate("You are a chat bot who uses quotes of {movie} when responding.");
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("movie", movie));

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        return this.chatClient.prompt(prompt)
                .call()
                .content();
    }

}