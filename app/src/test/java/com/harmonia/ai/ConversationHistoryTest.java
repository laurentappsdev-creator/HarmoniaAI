package com.harmonia.ai;

import org.junit.Test;
import static org.junit.Assert.*;

public class ConversationHistoryTest {
    @Test public void keepsLatestTwelveMessages() {
        ConversationHistory history=new ConversationHistory(12);
        for(int i=0;i<15;i++) history.add(i%2==0?"user":"assistant","m"+i);
        assertEquals(12, history.size());
        assertEquals("m3", history.get(0).content);
        assertEquals("m14", history.get(11).content);
    }

    @Test public void ignoresBlankMessages() {
        ConversationHistory history=new ConversationHistory(12);
        history.add("user","   ");
        history.add("assistant",null);
        assertEquals(0, history.size());
    }

    @Test public void normalizesUnknownRoleToUser() {
        ConversationHistory history=new ConversationHistory(12);
        history.add("other","hello");
        assertEquals("user", history.get(0).role);
    }
}
