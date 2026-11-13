package com.iappyx.generated.placeholder;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Byte Overlay Test 01.
 * A temporary, user-started floating bubble. It does not inspect or control other apps.
 */
public final class OverlayTestService extends Service {
    private WindowManager windowManager;
    private View overlay;
    private WindowManager.LayoutParams layoutParams;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        addOverlay();
    }

    private void addOverlay() {
        if (overlay != null) return;

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setPadding(dp(13), dp(8), dp(7), dp(8));

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(20, 39, 65));
        background.setCornerRadius(dp(28));
        background.setStroke(dp(1), Color.rgb(79, 195, 247));
        container.setBackground(background);
        container.setElevation(dp(8));

        TextView label = new TextView(this);
        label.setText("🤖  BYTE");
        label.setTextColor(Color.WHITE);
        label.setTextSize(15);
        label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        label.setGravity(Gravity.CENTER_VERTICAL);
        label.setPadding(dp(3), 0, dp(10), 0);
        container.addView(label, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dp(42)));

        TextView close = new TextView(this);
        close.setText("×");
        close.setTextColor(Color.WHITE);
        close.setTextSize(24);
        close.setGravity(Gravity.CENTER);
        close.setContentDescription("Close Byte overlay");
        GradientDrawable closeBackground = new GradientDrawable();
        closeBackground.setColor(Color.rgb(15, 52, 96));
        closeBackground.setCornerRadius(dp(21));
        close.setBackground(closeBackground);
        container.addView(close, new LinearLayout.LayoutParams(dp(42), dp(42)));
        close.setOnClickListener(v -> stopSelf());

        int type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.TOP | Gravity.END;
        layoutParams.x = dp(14);
        layoutParams.y = dp(180);

        container.setOnTouchListener(new View.OnTouchListener() {
            private int startX;
            private int startY;
            private float downX;
            private float downY;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = layoutParams.x;
                        startY = layoutParams.y;
                        downX = event.getRawX();
                        downY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        layoutParams.x = Math.max(0, startX + (int) (downX - event.getRawX()));
                        layoutParams.y = Math.max(0, startY + (int) (event.getRawY() - downY));
                        try {
                            if (overlay != null) windowManager.updateViewLayout(overlay, layoutParams);
                        } catch (Exception ignored) { }
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                    default:
                        return false;
                }
            }
        });

        overlay = container;
        try {
            windowManager.addView(overlay, layoutParams);
        } catch (Exception e) {
            overlay = null;
            stopSelf();
        }
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onDestroy() {
        if (overlay != null && windowManager != null) {
            try {
                windowManager.removeView(overlay);
            } catch (Exception ignored) { }
            overlay = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
