package com.harmonia.ai;

public final class MockData {
    private MockData() {}

    public static Profile[] profiles() {
        return new Profile[] {
            new Profile(
                    "Emma",31,"Besançon",34,
                    "Curieuse, naturelle et spontanée. J'aime les week-ends improvisés, les animaux et les longues balades.",
                    new String[]{"Randonnée","Animaux","Voyages","Cuisine"},
                    "serieuse",false,
                    new String[]{"spontanee","curieuse","affectueuse"},
                    new String[]{"nature","voyages","animaux"},
                    94,88,100,82
            ),
            new Profile(
                    "Lina",29,"Pontarlier",21,
                    "Plutôt calme au quotidien, mais toujours partante pour découvrir un nouvel endroit ou un bon concert.",
                    new String[]{"Musique","Nature","Photo","Road trips"},
                    "serieuse",false,
                    new String[]{"calme","ouverte","curieuse"},
                    new String[]{"nature","sorties","musique"},
                    87,93,91,89
            ),
            new Profile(
                    "Chloé",35,"Neuchâtel",46,
                    "Indépendante, positive et très attachée aux échanges sincères. Je cherche une relation sérieuse sans précipitation.",
                    new String[]{"Lecture","Lacs","Sport","Cinéma"},
                    "serieuse",false,
                    new String[]{"independante","positive","sincere"},
                    new String[]{"sport","culture","sorties"},
                    90,91,98,85
            ),
            new Profile(
                    "Sarah",33,"Morteau",8,
                    "Simple, drôle et assez active. Un café, une balade et une vraie conversation valent mieux qu'un long discours.",
                    new String[]{"Fitness","Café","Montagne","Humour"},
                    "serieuse",false,
                    new String[]{"drole","active","simple"},
                    new String[]{"sport","montagne","sorties"},
                    84,89,95,92
            )
        };
    }
}
