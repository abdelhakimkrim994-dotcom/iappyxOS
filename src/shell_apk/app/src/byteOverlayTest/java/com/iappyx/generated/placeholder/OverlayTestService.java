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
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Isolated keyboard feasibility spike.
 * Tests whether an application overlay can become focusable, accept text,
 * show the Android IME, and return to a non-focusable floating robot state.
 * Messages are local echo-only test data; there is no AI or persistence.
 */
public final class OverlayTestService extends Service {
    private WindowManager windowManager;
    private View overlay;
    private WindowManager.LayoutParams layoutParams;
    private LinearLayout companionPanel;
    private TextView transcript;
    private EditText messageInput;
    private boolean panelOpen;

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
        companionPanel.setPadding(dp(12), dp(12), dp(12), dp(12));
        GradientDrawable panelBg = new GradientDrawable();
        panelBg.setColor(Color.rgb(20, 30, 49));
        panelBg.setCornerRadius(dp(18));
        panelBg.setStroke(dp(1), Color.rgb(79, 195, 247));
        companionPanel.setBackground(panelBg);
        companionPanel.setElevation(dp(10));
        companionPanel.setVisibility(View.GONE);

        TextView title = new TextView(this);
        title.setText("Byte keyboard test");
        title.setTextColor(Color.WHITE);
        title.setTextSize(16);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        companionPanel.addView(title);

        TextView hint = new TextView(this);
        hint.setText("Tap the field, type a message, then press Send. This is a local echo test.");
        hint.setTextColor(Color.rgb(205, 216, 231));
        hint.setTextSize(12);
        hint.setPadding(0, dp(5), 0, dp(8));
        companionPanel.addView(hint);

        ScrollView historyScroll = new ScrollView(this);
        historyScroll.setFillViewport(false);
        transcript = new TextView(this);
        transcript.setText("Byte: Floating keyboard test ready.\n");
        transcript.setTextColor(Color.WHITE);
        transcript.setTextSize(13);
        transcript.setPadding(dp(8), dp(8), dp(8), dp(8));
        historyScroll.addView(transcript);
        LinearLayout.LayoutParams historyParams =
                new LinearLayout.LayoutParams(dp(250), dp(105));
        companionPanel.addView(historyScroll, historyParams);

        LinearLayout composer = new LinearLayout(this);
        composer.setOrientation(LinearLayout.HORIZONTAL);
        composer.setGravity(Gravity.CENTER_VERTICAL);
        composer.setPadding(0, dp(8), 0, dp(4));

        messageInput = new EditText(this);
        messageInput.setSingleLine(true);
        messageInput.setTextColor(Color.WHITE);
        messageInput.setHintTextColor(Color.rgb(150, 160, 180));
        messageInput.setHint("Type here...");
        messageInput.setTextSize(14);
        messageInput.setPadding(dp(10), 0, dp(10), 0);
        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setColor(Color.rgb(34, 42, 62));
        inputBg.setCornerRadius(dp(22));
        inputBg.setStroke(dp(1), Color.rgb(79, 195, 247));
        messageInput.setBackground(inputBg);
        composer.addView(messageInput, new LinearLayout.LayoutParams(0, dp(46), 1f));

        TextView send = new TextView(this);
        send.setText("Send");
        send.setTextColor(Color.rgb(10, 30, 45));
        send.setTextSize(13);
        send.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        send.setGravity(Gravity.CENTER);
        send.setBackground(roundBg(Color.rgb(79, 195, 247), dp(20)));
        LinearLayout.LayoutParams sendParams = new LinearLayout.LayoutParams(dp(58), dp(44));
        sendParams.leftMargin = dp(7);
        composer.addView(send, sendParams);
        send.setOnClickListener(v -> sendLocalEcho());
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            sendLocalEcho();
            return true;
        });
        companionPanel.addView(composer);

        TextView closePanel = new TextView(this);
        closePanel.setText("Minimize panel");
        closePanel.setTextColor(Color.rgb(79, 195, 247));
        closePanel.setTextSize(13);
        closePanel.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        closePanel.setPadding(0, dp(8), 0, dp(2));
        closePanel.setOnClickListener(v -> closePanel());
        companionPanel.addView(closePanel);

        LinearLayout.LayoutParams panelParams =
                new LinearLayout.LayoutParams(dp(280), LinearLayout.LayoutParams.WRAP_CONTENT);
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
                        if (!moved) {
                            if (panelOpen) closePanel();
                            else openPanel();
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
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.TOP | Gravity.END;
        layoutParams.x = dp(12);
        layoutParams.y = dp(170);
        layoutParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;

        overlay = root;
        try { windowManager.addView(overlay, layoutParams); }
        catch (Exception e) { overlay = null; stopSelf(); }
    }

    private void openPanel() {
        if (companionPanel == null || panelOpen) return;
        panelOpen = true;
        companionPanel.setVisibility(View.VISIBLE);
        layoutParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        layoutParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
        try {
            windowManager.updateViewLayout(overlay, layoutParams);
            messageInput.requestFocus();
            messageInput.postDelayed(() -> {
                InputMethodManager imm =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(messageInput, InputMethodManager.SHOW_IMPLICIT);
            }, 180);
        } catch (Exception e) {
            appendLine("Byte: Could not focus the floating panel: " + e.getClass().getSimpleName());
        }
    }

    private void closePanel() {
        if (companionPanel == null || !panelOpen) return;
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && messageInput != null) imm.hideSoftInputFromWindow(messageInput.getWindowToken(), 0);
        panelOpen = false;
        companionPanel.setVisibility(View.GONE);
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        try { windowManager.updateViewLayout(overlay, layoutParams); }
        catch (Exception ignored) { }
    }

    private void sendLocalEcho() {
        if (messageInput == null) return;
        String value = messageInput.getText().toString().trim();
        if (value.isEmpty()) return;
        appendLine("You: " + value);
        appendLine("Byte: Keyboard input received. (Local test only.)");
        messageInput.setText("");
    }

    private void appendLine(String line) {
        if (transcript == null) return;
        transcript.append(line + "\n");
    }

    private GradientDrawable roundBg(int color, int radius) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(radius);
        return bg;
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
