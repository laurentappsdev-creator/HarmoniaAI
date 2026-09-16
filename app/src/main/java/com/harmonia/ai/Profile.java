package com.harmonia.ai;

public class Profile {
    public final String name;
    public final int age;
    public final String city;
    public final String cityName;
    public final int distanceKm;
    public final String bio;
    public final String[] interests;
    public final String relationshipIntent;
    public final boolean smoker;
    public final String[] personalityTraits;
    public final String[] lifestyleTraits;

    // Base affinity signals. They remain user-relative, not objective ratings.
    public final int visual;
    public final int personality;
    public final int intent;
    public final int lifestyle;
    public final int distance;

    public Profile(String name,int age,String cityName,int distanceKm,String bio,String[] interests,
                   String relationshipIntent,boolean smoker,String[] personalityTraits,
                   String[] lifestyleTraits,int visual,int personality,int intent,int lifestyle) {
        this.name=name;
        this.age=age;
        this.cityName=cityName;
        this.distanceKm=Math.max(0,distanceKm);
        this.city=cityName+" · "+this.distanceKm+" km";
        this.bio=bio;
        this.interests=interests==null?new String[0]:interests;
        this.relationshipIntent=UserPreferences.clean(relationshipIntent);
        this.smoker=smoker;
        this.personalityTraits=personalityTraits==null?new String[0]:personalityTraits;
        this.lifestyleTraits=lifestyleTraits==null?new String[0]:lifestyleTraits;
        this.visual=clamp(visual);
        this.personality=clamp(personality);
        this.intent=clamp(intent);
        this.lifestyle=clamp(lifestyle);
        this.distance=distanceScore(this.distanceKm,50);
    }

    // Backward-compatible constructor.
    public Profile(String name,int age,String city,String bio,String[] interests,
                   int visual,int personality,int intent,int lifestyle,int distance) {
        this.name=name;
        this.age=age;
        this.city=city;
        this.cityName=city.contains(" · ")?city.substring(0,city.indexOf(" · ")):city;
        this.distanceKm=parseDistance(city);
        this.bio=bio;
        this.interests=interests==null?new String[0]:interests;
        this.relationshipIntent="serieuse";
        this.smoker=false;
        this.personalityTraits=new String[0];
        this.lifestyleTraits=new String[0];
        this.visual=clamp(visual);
        this.personality=clamp(personality);
        this.intent=clamp(intent);
        this.lifestyle=clamp(lifestyle);
        this.distance=clamp(distance);
    }

    public int score() {
        return MatchEngine.total(visual,personality,intent,lifestyle,distance);
    }

    private static int clamp(int value) {
        return Math.max(0,Math.min(100,value));
    }

    private static int parseDistance(String text) {
        if(text==null) return 0;
        String digits=text.replaceAll("[^0-9]"," ").trim();
        if(digits.isEmpty()) return 0;
        try { return Integer.parseInt(digits.split("\\s+")[0]); }
        catch(Exception ignored) { return 0; }
    }

    public static int distanceScore(int km,int maxKm) {
        if(maxKm<=0) return km==0?100:0;
        if(km>=maxKm) return 0;
        return (int)Math.round(100.0*(1.0-(km/(double)maxKm)));
    }
}
