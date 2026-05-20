package com.swiftlinkai.ai;

import java.util.List;

/**
 * Immutable result returned by the AI metadata pipeline.
 */
public record AiMetadataResult(
        String alias,
        List<String> tags,
        String category,
        String seoTitle,
        String seoDescription,
        boolean aiGenerated
) {
    /** Fallback result when AI is unavailable. */
    public static AiMetadataResult fallback(String alias) {
        return new AiMetadataResult(alias, List.of(), "General", null, null, false);
    }
}
