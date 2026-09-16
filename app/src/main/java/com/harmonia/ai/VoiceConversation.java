package com.harmonia.ai;

public final class VoiceConversation {
    private VoiceConversation() {}

    public static String clean(String text) {
        return text == null ? "" : text.trim();
    }

    public static boolean hasText(String text) {
        return !clean(text).isEmpty();
    }
}
