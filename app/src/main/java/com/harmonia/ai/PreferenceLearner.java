package com.harmonia.ai;

import android.content.SharedPreferences;

public final class PreferenceLearner {
    private PreferenceLearner() {}

    public static void record(SharedPreferences prefs,Profile profile,boolean liked) {
        if(profile==null || prefs==null) return;
        SharedPreferences.Editor editor=prefs.edit();

        for(String interest:profile.interests) {
            String key=keyForInterest(interest);
            int current=prefs.getInt(key,0);
            int next=current+(liked?1:-1);
            editor.putInt(key,Math.max(-8,Math.min(8,next)));
        }

        String cityKey="learn_city_"+slug(profile.cityName);
        int cityCurrent=prefs.getInt(cityKey,0);
        editor.putInt(cityKey,Math.max(-5,Math.min(5,cityCurrent+(liked?1:-1))));
        editor.apply();
    }

    public static int adjustment(SharedPreferences prefs,Profile profile) {
        if(profile==null || prefs==null) return 0;

        int sum=0;
        int count=0;
        for(String interest:profile.interests) {
            sum+=prefs.getInt(keyForInterest(interest),0);
            count++;
        }

        if(count==0) return 0;
        double average=sum/(double)count;
        return (int)Math.round(Math.max(-6,Math.min(6,average)));
    }

    private static String keyForInterest(String value) {
        return "learn_interest_"+slug(value);
    }

    private static String slug(String value) {
        String s=UserPreferences.clean(value);
        return s.replaceAll("[^a-z0-9]+","_");
    }
}
