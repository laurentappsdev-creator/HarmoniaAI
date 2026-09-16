package com.harmonia.ai;

public final class VoiceSettings {
    private VoiceSettings() {}

    public static final class Preset {
        public final float volume;
        public final float rate;
        public final float pitch;

        public Preset(float volume,float rate,float pitch) {
            this.volume=clampVolume(volume);
            this.rate=clampRate(rate);
            this.pitch=clampPitch(pitch);
        }
    }

    public static float clampVolume(float value) {
        return Math.max(0f,Math.min(1f,value));
    }

    public static float clampRate(float value) {
        return Math.max(0.65f,Math.min(1.25f,value));
    }

    public static float clampPitch(float value) {
        return Math.max(0.75f,Math.min(1.25f,value));
    }

    public static Preset natural() {
        return new Preset(0.92f,0.96f,1.00f);
    }

    public static Preset soft() {
        return new Preset(0.84f,0.90f,0.96f);
    }

    public static Preset sensual() {
        return new Preset(0.88f,0.84f,0.96f);
    }

    public static int volumeToProgress(float volume) {
        return Math.round(clampVolume(volume)*100f);
    }

    public static float progressToVolume(int progress) {
        return clampVolume(progress/100f);
    }

    public static int rateToProgress(float rate) {
        return Math.round((clampRate(rate)-0.65f)/0.60f*100f);
    }

    public static float progressToRate(int progress) {
        return clampRate(0.65f+(progress/100f)*0.60f);
    }

    public static int pitchToProgress(float pitch) {
        return Math.round((clampPitch(pitch)-0.75f)/0.50f*100f);
    }

    public static float progressToPitch(int progress) {
        return clampPitch(0.75f+(progress/100f)*0.50f);
    }
}
