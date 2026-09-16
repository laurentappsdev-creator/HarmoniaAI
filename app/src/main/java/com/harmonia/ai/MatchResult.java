package com.harmonia.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MatchResult {
    public final int score;
    public final boolean eligible;
    public final int visual;
    public final int personality;
    public final int intent;
    public final int lifestyle;
    public final int distance;
    public final int learnedAdjustment;
    private final List<String> reasons;
    private final List<String> blockers;

    public MatchResult(int score,boolean eligible,int visual,int personality,int intent,
                       int lifestyle,int distance,int learnedAdjustment,
                       List<String> reasons,List<String> blockers) {
        this.score=Math.max(0,Math.min(100,score));
        this.eligible=eligible;
        this.visual=visual;
        this.personality=personality;
        this.intent=intent;
        this.lifestyle=lifestyle;
        this.distance=distance;
        this.learnedAdjustment=learnedAdjustment;
        this.reasons=Collections.unmodifiableList(new ArrayList<>(reasons));
        this.blockers=Collections.unmodifiableList(new ArrayList<>(blockers));
    }

    public List<String> reasons() { return reasons; }
    public List<String> blockers() { return blockers; }
}
