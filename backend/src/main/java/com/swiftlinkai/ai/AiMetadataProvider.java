package com.swiftlinkai.ai;

/**
 * Strategy interface for AI metadata providers.
 * Implementations: OpenAI, Claude, Gemini, Ollama.
 */
public interface AiMetadataProvider {

    /**
     * Generate alias, tags, and category from raw webpage content.
     *
     * @param pageContent extracted text/HTML from the target URL
     * @param url         the original URL for context
     * @return AI-generated metadata
     */
    AiMetadataResult generate(String pageContent, String url);

    /**
     * Provider identifier used in configuration and logging.
     */
    String providerName();
}
