// package com.swiftlinkai.config;

// import org.springframework.ai.chat.client.ChatClient;
// import org.springframework.ai.openai.OpenAiChatModel;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// @Configuration
// public class AiConfig {

//     @Bean
//     public ChatClient chatClient(OpenAiChatModel model) {
//         return ChatClient.builder(model).build();
//     }
// }

package com.swiftlinkai.config;

import org.springframework.context.annotation.Configuration;

/**
 * AI configuration.
 * Using Gemini REST API directly via Java HttpClient — no Spring AI dependency needed.
 * Provider bean: GeminiMetadataProvider (auto-registered as @Component)
 */
@Configuration
public class AiConfig {
    // No beans required — GeminiMetadataProvider uses Java 11 HttpClient directly
}
