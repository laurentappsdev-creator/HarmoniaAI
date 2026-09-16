package com.harmonia.ai;

import org.junit.Test;
import static org.junit.Assert.*;

public class MatchEngineTest {
    private Profile profile(String intent,boolean smoker,int distance,String[] interests) {
        return new Profile(
                "Test",31,"Morteau",distance,"Bio",interests,
                intent,smoker,new String[]{"calme","spontanee"},
                new String[]{"nature","sorties"},90,88,95,86
        );
    }

    private UserPreferences prefs() {
        return new UserPreferences(
                25,40,50,"serieuse",true,
                UserPreferences.set("nature","animaux","voyages")
        );
    }

    @Test public void blocksOutsideAgeRange() {
        Profile p=new Profile("Test",45,"Morteau",8,"Bio",
                new String[]{"Nature"},"serieuse",false,
                new String[]{"calme"},new String[]{"nature"},90,88,95,86);
        MatchResult r=MatchEngine.evaluate(p,prefs(),0);
        assertFalse(r.eligible);
        assertEquals(0,r.score);
        assertFalse(r.blockers().isEmpty());
    }

    @Test public void blocksDistanceAndSmokingWhenRequired() {
        MatchResult far=MatchEngine.evaluate(profile("serieuse",false,80,new String[]{"Nature"}),prefs(),0);
        assertFalse(far.eligible);

        MatchResult smoker=MatchEngine.evaluate(profile("serieuse",true,8,new String[]{"Nature"}),prefs(),0);
        assertFalse(smoker.eligible);
    }

    @Test public void matchingInterestsImproveLifestyleScore() {
        MatchResult withOverlap=MatchEngine.evaluate(
                profile("serieuse",false,8,new String[]{"Nature","Voyages","Animaux"}),prefs(),0);
        MatchResult withoutOverlap=MatchEngine.evaluate(
                profile("serieuse",false,8,new String[]{"Gaming","Auto"}),prefs(),0);
        assertTrue(withOverlap.lifestyle>withoutOverlap.lifestyle);
        assertTrue(withOverlap.score>withoutOverlap.score);
    }

    @Test public void learnedAdjustmentChangesScoreWithinBounds() {
        MatchResult base=MatchEngine.evaluate(profile("serieuse",false,8,new String[]{"Nature"}),prefs(),0);
        MatchResult learned=MatchEngine.evaluate(profile("serieuse",false,8,new String[]{"Nature"}),prefs(),6);
        assertEquals(Math.min(100,base.score+6),learned.score);
    }
}
