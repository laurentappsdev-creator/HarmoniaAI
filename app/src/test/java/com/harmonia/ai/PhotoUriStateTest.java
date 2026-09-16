package com.harmonia.ai;

import org.junit.Test;
import static org.junit.Assert.*;

public class PhotoUriStateTest {
    @Test public void cleansAndKeepsValidContentUri() {
        assertEquals("content://media/photo/42", PhotoUriState.clean("  content://media/photo/42  "));
    }

    @Test public void turnsNullIntoEmptyString() {
        assertEquals("", PhotoUriState.clean(null));
    }

    @Test public void detectsWhetherPhotoExists() {
        assertFalse(PhotoUriState.hasPhoto("   "));
        assertTrue(PhotoUriState.hasPhoto("content://media/photo/42"));
    }
}
