package com.iappyx.generated.placeholder;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.view.ViewGroup;

/**
 * Standalone, minimal host for the Byte overlay permission experiment.
 * Exposes only the four methods needed by the local test page.
 */
public final class ByteOverlayTestActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        webView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);

        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new ByteOverlayBridge(), "byteOverlay");
        setContentView(webView);
        webView.loadUrl("file:///android_asset/app/index.html");
    }

    private final class ByteOverlayBridge {
        @JavascriptInterface
        public boolean hasOverlayPermission() {
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                    || Settings.canDrawOverlays(ByteOverlayTestActivity.this);
        }

        @JavascriptInterface
        public void requestOverlayPermission() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                    || Settings.canDrawOverlays(ByteOverlayTestActivity.this)) return;
            runOnUiThread(() -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            });
        }

        @JavascriptInterface
        public boolean showByteOverlay() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && !Settings.canDrawOverlays(ByteOverlayTestActivity.this)) return false;
            try {
                startService(new Intent(ByteOverlayTestActivity.this, OverlayTestService.class));
                return true;
            } catch (Exception ignored) {
                return false;
            }
        }

        @JavascriptInterface
        public boolean hideByteOverlay() {
            try {
                return stopService(new Intent(ByteOverlayTestActivity.this, OverlayTestService.class));
            } catch (Exception ignored) {
                return false;
            }
        }

        @JavascriptInterface
        public String getMessagesJson() {
            return ConversationManager.getMessagesJson(ByteOverlayTestActivity.this);
        }

        @JavascriptInterface
        public boolean sendMessage(String message) {
            return ConversationManager.send(ByteOverlayTestActivity.this, message);
        }

        @JavascriptInterface
        public boolean toggleByteOverlay() {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && !Settings.canDrawOverlays(ByteOverlayTestActivity.this)) {
                    requestOverlayPermission();
                    return false;
                }
                boolean stopped = stopService(new Intent(ByteOverlayTestActivity.this, OverlayTestService.class));
                if (stopped) return false;
                startService(new Intent(ByteOverlayTestActivity.this, OverlayTestService.class));
                return true;
            } catch (Exception ignored) {
                return false;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.removeJavascriptInterface("byteOverlay");
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
