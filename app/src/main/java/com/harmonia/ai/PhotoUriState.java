package com.harmonia.ai;

public final class PhotoUriState {
    private PhotoUriState() {}

    public static String clean(String uri) {
        return uri == null ? "" : uri.trim();
    }

    public static boolean hasPhoto(String uri) {
        return !clean(uri).isEmpty();
    }
}
