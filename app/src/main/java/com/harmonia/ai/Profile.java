package com.harmonia.ai;

public class Profile {
    public final String name;
    public final int age;
    public final String city;
    public final String bio;
    public final String[] interests;
    public final int visual;
    public final int personality;
    public final int intent;
    public final int lifestyle;
    public final int distance;

    public Profile(String name, int age, String city, String bio, String[] interests,
                   int visual, int personality, int intent, int lifestyle, int distance) {
        this.name = name;
        this.age = age;
        this.city = city;
        this.bio = bio;
        this.interests = interests;
        this.visual = visual;
        this.personality = personality;
        this.intent = intent;
        this.lifestyle = lifestyle;
        this.distance = distance;
    }

    public int score() {
        return MatchEngine.total(visual, personality, intent, lifestyle, distance);
    }
}
