package com.harmonia.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ConversationHistory {
    public static final class Message {
        public final String role;
        public final String content;

        Message(String role,String content) {
            this.role=role;
            this.content=content;
        }
    }

    private final int maxMessages;
    private final ArrayList<Message> messages=new ArrayList<>();

    public ConversationHistory(int maxMessages) {
        this.maxMessages=Math.max(1,maxMessages);
    }

    public void add(String role,String content) {
        String cleaned=content==null?"":content.trim();
        if(cleaned.isEmpty()) return;

        String normalized="assistant".equals(role)?"assistant":"user";
        messages.add(new Message(normalized,cleaned));

        while(messages.size()>maxMessages) {
            messages.remove(0);
        }
    }

    public int size() {
        return messages.size();
    }

    public Message get(int index) {
        return messages.get(index);
    }

    public List<Message> all() {
        return Collections.unmodifiableList(new ArrayList<>(messages));
    }
}
