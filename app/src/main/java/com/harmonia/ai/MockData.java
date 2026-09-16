package com.harmonia.ai;

public final class MockData {
    private MockData() {}

    public static Profile[] profiles() {
        return new Profile[] {
            new Profile("Emma", 31, "Besançon · 34 km",
                    "Curieuse, naturelle et spontanée. J'aime les week-ends improvisés, les animaux et les longues balades.",
                    new String[]{"Randonnée", "Animaux", "Voyages", "Cuisine"}, 94, 88, 100, 82, 78),
            new Profile("Lina", 29, "Pontarlier · 21 km",
                    "Plutôt calme au quotidien, mais toujours partante pour découvrir un nouvel endroit ou un bon concert.",
                    new String[]{"Musique", "Nature", "Photo", "Road trips"}, 87, 93, 91, 89, 90),
            new Profile("Chloé", 35, "Neuchâtel · 46 km",
                    "Indépendante, positive et très attachée aux échanges sincères. Je cherche une relation sérieuse sans précipitation.",
                    new String[]{"Lecture", "Lacs", "Sport", "Cinéma"}, 90, 91, 98, 85, 70),
            new Profile("Sarah", 33, "Morteau · 8 km",
                    "Simple, drôle et assez active. Un café, une balade et une vraie conversation valent mieux qu'un long discours.",
                    new String[]{"Fitness", "Café", "Montagne", "Humour"}, 84, 89, 95, 92, 98)
        };
    }
}
