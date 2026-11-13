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

/** User-started floating Byte avatar prototype. No screen inspection or control. */
public final class OverlayTestService extends Service {
    private WindowManager windowManager;
    private View overlay;
    private WindowManager.LayoutParams layoutParams;
    private LinearLayout companionPanel;

    @Override public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        addOverlay();
    }

    private void addOverlay() {
        if (overlay != null) return;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.END);
        root.setPadding(dp(5), dp(5), dp(5), dp(5));

        ByteRobotView robot = new ByteRobotView(this);
        root.addView(robot, new LinearLayout.LayoutParams(dp(76), dp(76)));

        companionPanel = new LinearLayout(this);
        companionPanel.setOrientation(LinearLayout.VERTICAL);
        companionPanel.setPadding(dp(16), dp(13), dp(16), dp(14));
        GradientDrawable panelBg = new GradientDrawable();
        panelBg.setColor(Color.rgb(20, 30, 49));
        panelBg.setCornerRadius(dp(18));
        panelBg.setStroke(dp(1), Color.rgb(79, 195, 247));
        companionPanel.setBackground(panelBg);
        companionPanel.setElevation(dp(10));
        companionPanel.setVisibility(View.GONE);

        TextView title = new TextView(this);
        title.setText("Hi! I'm Byte.");
        title.setTextColor(Color.WHITE);
        title.setTextSize(17);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        companionPanel.addView(title);

        TextView message = new TextView(this);
        message.setText("Your floating companion is here. The conversation interface is our next step.");
        message.setTextColor(Color.rgb(205, 216, 231));
        message.setTextSize(13);
        message.setPadding(0, dp(6), 0, dp(10));
        companionPanel.addView(message);

        TextView closePanel = new TextView(this);
        closePanel.setText("Close panel");
        closePanel.setTextColor(Color.rgb(79, 195, 247));
        closePanel.setTextSize(14);
        closePanel.setGravity(Gravity.END);
        closePanel.setPadding(0, dp(6), 0, dp(4));
        closePanel.setOnClickListener(v -> companionPanel.setVisibility(View.GONE));
        companionPanel.addView(closePanel);

        LinearLayout.LayoutParams panelParams = new LinearLayout.LayoutParams(dp(230), LinearLayout.LayoutParams.WRAP_CONTENT);
        panelParams.topMargin = dp(8);
        root.addView(companionPanel, panelParams);

        robot.setOnTouchListener(new View.OnTouchListener() {
            private int startX, startY;
            private float downX, downY;
            private boolean moved;

            @Override public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = layoutParams.x;
                        startY = layoutParams.y;
                        downX = event.getRawX();
                        downY = event.getRawY();
                        moved = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - downX;
                        float dy = event.getRawY() - downY;
                        if (Math.abs(dx) > dp(5) || Math.abs(dy) > dp(5)) moved = true;
                        if (moved) {
                            layoutParams.x = Math.max(0, startX - (int) dx);
                            layoutParams.y = Math.max(0, startY + (int) dy);
                            try { windowManager.updateViewLayout(overlay, layoutParams); }
                            catch (Exception ignored) { }
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!moved && companionPanel != null) {
                            companionPanel.setVisibility(companionPanel.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                        }
                        return true;
                    default: return true;
                }
            }
        });

        layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.TOP | Gravity.END;
        layoutParams.x = dp(12);
        layoutParams.y = dp(170);

        overlay = root;
        try { windowManager.addView(overlay, layoutParams); }
        catch (Exception e) { overlay = null; stopSelf(); }
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override public void onDestroy() {
        if (overlay != null && windowManager != null) {
            try { windowManager.removeView(overlay); } catch (Exception ignored) { }
            overlay = null;
        }
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}