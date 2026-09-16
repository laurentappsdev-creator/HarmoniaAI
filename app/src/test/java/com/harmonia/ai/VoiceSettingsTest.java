package com.harmonia.ai;

import org.junit.Test;
import static org.junit.Assert.*;

public class VoiceSettingsTest {
    @Test public void clampsVolume() {
        assertEquals(0f, VoiceSettings.clampVolume(-1f), 0.001f);
        assertEquals(1f, VoiceSettings.clampVolume(2f), 0.001f);
        assertEquals(0.6f, VoiceSettings.clampVolume(0.6f), 0.001f);
    }

    @Test public void clampsRateAndPitch() {
        assertEquals(0.65f, VoiceSettings.clampRate(0.3f), 0.001f);
        assertEquals(1.25f, VoiceSettings.clampRate(2f), 0.001f);
        assertEquals(0.75f, VoiceSettings.clampPitch(0.2f), 0.001f);
        assertEquals(1.25f, VoiceSettings.clampPitch(2f), 0.001f);
    }

    @Test public void sensualPresetIsSlowerAndSofter() {
        VoiceSettings.Preset p=VoiceSettings.sensual();
        assertTrue(p.rate < 1f);
        assertTrue(p.pitch < 1f);
        assertTrue(p.volume > 0.7f);
    }
}
