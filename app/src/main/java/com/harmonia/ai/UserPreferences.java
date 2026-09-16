package com.harmonia.ai;

import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class UserPreferences {
    public final int minAge;
    public final int maxAge;
    public final int maxDistanceKm;
    public final String relationshipIntent;
    public final boolean rejectSmokers;
    public final Set<String> preferredInterests;

    public UserPreferences(int minAge,int maxAge,int maxDistanceKm,String relationshipIntent,
                           boolean rejectSmokers,Set<String> preferredInterests) {
        this.minAge=minAge;
        this.maxAge=maxAge;
        this.maxDistanceKm=maxDistanceKm;
        this.relationshipIntent=clean(relationshipIntent);
        this.rejectSmokers=rejectSmokers;
        this.preferredInterests=preferredInterests==null
                ? new HashSet<>()
                : new HashSet<>(preferredInterests);
    }

    public static UserPreferences from(SharedPreferences prefs) {
        int min=prefs.getInt("pref_min_age",25);
        int max=prefs.getInt("pref_max_age",40);
        int distance=prefs.getInt("pref_max_distance",50);
        String intent=prefs.getString("pref_relationship","serieuse");
        boolean rejectSmokers=prefs.getBoolean("pref_reject_smokers",false);

        String raw=prefs.getString("pref_interests","nature,animaux,voyages,sorties");
        Set<String> interests=new HashSet<>();
        if(raw!=null) {
            for(String part:raw.split(",")) {
                String v=clean(part);
                if(!v.isEmpty()) interests.add(v);
            }
        }
        return new UserPreferences(min,max,distance,intent,rejectSmokers,interests);
    }

    public static String clean(String value) {
        return value==null?"":value.trim().toLowerCase();
    }

    public static Set<String> set(String... values) {
        return new HashSet<>(Arrays.asList(values));
    }
}
