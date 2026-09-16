package com.harmonia.ai;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MatchEngine {
    private MatchEngine() {}

    public static int total(int visual,int personality,int intent,int lifestyle,int distance) {
        double s=visual*0.30
                +personality*0.25
                +intent*0.20
                +lifestyle*0.15
                +distance*0.10;
        return clamp((int)Math.round(s));
    }

    public static MatchResult evaluate(Profile profile,UserPreferences prefs,int learnedAdjustment) {
        List<String> blockers=new ArrayList<>();
        List<String> reasons=new ArrayList<>();

        if(profile.age<prefs.minAge || profile.age>prefs.maxAge) {
            blockers.add("Âge hors de votre plage "+prefs.minAge+"–"+prefs.maxAge+" ans");
        }
        if(profile.distanceKm>prefs.maxDistanceKm) {
            blockers.add("Distance supérieure à votre limite de "+prefs.maxDistanceKm+" km");
        }
        if(!prefs.relationshipIntent.isEmpty()
                && !profile.relationshipIntent.isEmpty()
                && !prefs.relationshipIntent.equals(profile.relationshipIntent)) {
            blockers.add("Projet de relation différent du vôtre");
        }
        if(prefs.rejectSmokers && profile.smoker) {
            blockers.add("Profil fumeur alors que ce critère est éliminatoire");
        }

        int overlap=interestOverlap(profile.interests,prefs.preferredInterests);
        int interestScore=interestScore(profile.interests,prefs.preferredInterests);

        int visual=clamp(profile.visual);
        int personality=clamp(profile.personality);
        int intent=prefs.relationshipIntent.isEmpty()
                ? clamp(profile.intent)
                : (prefs.relationshipIntent.equals(profile.relationshipIntent)?100:clamp(profile.intent));
        int lifestyle=clamp((int)Math.round(profile.lifestyle*0.70+interestScore*0.30));
        int distance=Profile.distanceScore(profile.distanceKm,prefs.maxDistanceKm);

        if(!blockers.isEmpty()) {
            return new MatchResult(0,false,visual,personality,intent,lifestyle,distance,0,reasons,blockers);
        }

        int adjustment=Math.max(-6,Math.min(6,learnedAdjustment));
        int score=clamp(total(visual,personality,intent,lifestyle,distance)+adjustment);

        if(intent>=95) reasons.add("Vous recherchez le même type de relation");
        if(personality>=88) reasons.add("Forte compatibilité de personnalité");
        else if(personality>=78) reasons.add("Personnalités globalement compatibles");

        if(visual>=88) reasons.add("Ce profil correspond fortement à vos préférences visuelles personnelles");

        if(overlap>=3) reasons.add(overlap+" centres d’intérêt correspondent à vos préférences");
        else if(overlap==2) reasons.add("Plusieurs centres d’intérêt correspondent à vos préférences");
        else if(overlap==1) reasons.add("Un centre d’intérêt important correspond à vos préférences");

        if(profile.distanceKm<=10) reasons.add("Habite tout près de vous ("+profile.distanceKm+" km)");
        else if(profile.distanceKm<=Math.max(15,prefs.maxDistanceKm/2))
            reasons.add("Distance compatible avec votre recherche ("+profile.distanceKm+" km)");

        if(adjustment>=3) reasons.add("Vos likes précédents indiquent une affinité croissante avec ce type de profil");
        else if(adjustment<=-3) reasons.add("Vos choix précédents suggèrent une affinité plus faible avec ce type de profil");

        if(reasons.isEmpty()) reasons.add("Plusieurs critères généraux sont compatibles avec vos préférences");

        return new MatchResult(score,true,visual,personality,intent,lifestyle,distance,
                adjustment,reasons,blockers);
    }

    private static int interestOverlap(String[] profileInterests,Set<String> preferred) {
        if(profileInterests==null || preferred==null || preferred.isEmpty()) return 0;
        Set<String> normalized=new HashSet<>();
        for(String p:preferred) normalized.add(UserPreferences.clean(p));

        int count=0;
        for(String interest:profileInterests) {
            if(normalized.contains(UserPreferences.clean(interest))) count++;
        }
        return count;
    }

    private static int interestScore(String[] profileInterests,Set<String> preferred) {
        if(preferred==null || preferred.isEmpty()) return 70;
        int overlap=interestOverlap(profileInterests,preferred);
        double ratio=overlap/(double)preferred.size();
        return clamp((int)Math.round(45+55*Math.min(1.0,ratio)));
    }

    public static String verdict(int score) {
        if(score>=90) return "Très forte compatibilité";
        if(score>=80) return "Forte compatibilité";
        if(score>=70) return "Bonne compatibilité";
        if(score>=55) return "Compatibilité à explorer";
        return "Compatibilité limitée";
    }

    private static int clamp(int value) {
        return Math.max(0,Math.min(100,value));
    }
}
