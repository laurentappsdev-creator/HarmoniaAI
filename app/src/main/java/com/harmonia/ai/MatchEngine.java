package com.harmonia.ai;

public final class MatchEngine {
    private MatchEngine() {}

    public static int total(int visual, int personality, int intent, int lifestyle, int distance) {
        double s = visual * 0.30
                + personality * 0.25
                + intent * 0.20
                + lifestyle * 0.15
                + distance * 0.10;
        return (int) Math.round(s);
    }

    public static String verdict(int score) {
        if (score >= 90) return "Très forte compatibilité";
        if (score >= 80) return "Forte compatibilité";
        if (score >= 70) return "Bonne compatibilité";
        return "Compatibilité à explorer";
    }
}
