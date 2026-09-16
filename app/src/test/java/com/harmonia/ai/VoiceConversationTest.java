package com.harmonia.ai;

import org.junit.Test;
import static org.junit.Assert.*;

public class VoiceConversationTest {
    @Test public void cleansRecognizedSpeech() {
        assertEquals("Bonjour Harmonia", VoiceConversation.clean("  Bonjour Harmonia  "));
    }

    @Test public void rejectsEmptyRecognizedSpeech() {
        assertFalse(VoiceConversation.hasText("   "));
        assertFalse(VoiceConversation.hasText(null));
        assertTrue(VoiceConversation.hasText("Trouve-moi Emma"));
    }
}
