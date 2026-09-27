package com.martini.fish_gen.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class ClaudeConfiguration {

    @Bean
    public ChatClient chatClient(ChatModel chatModel,
            @Value("classpath:prompts/chatbot-system-prompt.st") Resource systemPrompt) {
        return ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .build();
    }

    @Bean
    public PromptTemplate promptTemplate(@Value("classpath:prompts/chatbot-system-prompt.st")
                                         Resource systemPrompt) {
        return PromptTemplate.builder()
                .resource(systemPrompt)
                .renderer(StTemplateRenderer.builder()
                        .startDelimiterToken('<')
                        .endDelimiterToken('>')
                        .build())
                .build();
    }
}
