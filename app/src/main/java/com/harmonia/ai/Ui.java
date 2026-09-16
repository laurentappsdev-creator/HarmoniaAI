package com.harmonia.ai;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

public final class Ui {
    public static final int BG = Color.rgb(12,13,19);
    public static final int SURFACE = Color.rgb(23,25,37);
    public static final int SURFACE2 = Color.rgb(34,37,54);
    public static final int TEXT = Color.rgb(247,244,251);
    public static final int MUTED = Color.rgb(169,166,181);
    public static final int PINK = Color.rgb(255,79,139);
    public static final int VIOLET = Color.rgb(138,99,255);
    public static final int MINT = Color.rgb(84,230,177);

    private Ui() {}

    public static int dp(Context c, int dp) {
        return Math.round(dp * c.getResources().getDisplayMetrics().density);
    }

    public static GradientDrawable rounded(int color, float radiusDp, Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(c, (int) radiusDp));
        return d;
    }

    public static GradientDrawable stroke(int fill, int stroke, int widthDp, float radiusDp, Context c) {
        GradientDrawable d = rounded(fill, radiusDp, c);
        d.setStroke(dp(c, widthDp), stroke);
        return d;
    }

    public static TextView text(Context c, String s, float sp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }

    public static void padding(View v, Context c, int l, int t, int r, int b) {
        v.setPadding(dp(c,l), dp(c,t), dp(c,r), dp(c,b));
    }
}
