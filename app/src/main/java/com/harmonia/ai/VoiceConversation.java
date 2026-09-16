package com.harmonia.ai;

import java.util.Locale;

public final class VoiceConversation {
    private VoiceConversation() {}

    public static String clean(String text) {
        return text == null ? "" : text.trim();
    }

    public static boolean hasText(String text) {
        return !clean(text).isEmpty();
    }

    public static String languageCode(String languageTag) {
        String clean = clean(languageTag);
        if (clean.isEmpty()) return "fr";
        Locale locale = Locale.forLanguageTag(clean);
        String language = locale.getLanguage();
        if (language == null || language.trim().isEmpty()) return "fr";
        return language.toLowerCase(Locale.ROOT);
    }
}
