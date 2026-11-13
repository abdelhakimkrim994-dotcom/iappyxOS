package com.iappyx.generated.placeholder;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.database.Cursor;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.UUID;
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
    private static final int PICK_PROJECT_FILES = 4107;
    private static final long MAX_PROJECT_FILE_BYTES = 104857600L;
    private WebView webView;
    private String pendingProjectId;

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
        public String getProjectsJson() {
            ProjectRepository repository = new ProjectRepository(ByteOverlayTestActivity.this);
            try { return repository.listProjects().toString(); }
            finally { repository.close(); }
        }

        @JavascriptInterface
        public boolean createProject(String name, String description) {
            ProjectRepository repository = new ProjectRepository(ByteOverlayTestActivity.this);
            try { return repository.createProject(name, description); }
            finally { repository.close(); }
        }

        @JavascriptInterface
        public boolean updateProject(String id, String name, String description, String status) {
            ProjectRepository repository = new ProjectRepository(ByteOverlayTestActivity.this);
            try { return repository.updateProject(id, name, description, status); }
            finally { repository.close(); }
        }

        @JavascriptInterface
        public boolean deleteProject(String id) {
            ProjectRepository repository = new ProjectRepository(ByteOverlayTestActivity.this);
            try { return repository.deleteProject(id); }
            finally { repository.close(); }
        }


        @JavascriptInterface
        public String getProjectDetailsJson(String projectId) {
            ProjectRepository repo = new ProjectRepository(ByteOverlayTestActivity.this);
            try { return repo.getProjectDetails(projectId).toString(); }
            finally { repo.close(); }
        }

        @JavascriptInterface
        public boolean pickProjectFiles(String projectId) { return runPickerOnUiThread(projectId); }

        @JavascriptInterface
        public String searchProjectFilesJson(String projectId, String query) {
            ProjectRepository repo = new ProjectRepository(ByteOverlayTestActivity.this);
            try { return repo.searchProjectFiles(projectId, query).toString(); }
            finally { repo.close(); }
        }

        @JavascriptInterface
        public boolean addProjectNote(String projectId, String title, String body) {
            ProjectRepository repo = new ProjectRepository(ByteOverlayTestActivity.this);
            try { return repo.addProjectNote(projectId, title, body); }
            finally { repo.close(); }
        }

        @JavascriptInterface
        public boolean deleteProjectFile(String fileId) { return removeProjectFile(fileId); }

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


    private boolean runPickerOnUiThread(String projectId) {
        if (projectId == null || projectId.trim().isEmpty()) return false;
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        final boolean[] result = new boolean[]{false};
        runOnUiThread(() -> { result[0] = launchProjectFilePicker(projectId); latch.countDown(); });
        try { return latch.await(3, java.util.concurrent.TimeUnit.SECONDS) && result[0]; }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); return false; }
    }

    private boolean launchProjectFilePicker(String projectId) {
        try {
            pendingProjectId = projectId;
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(intent, PICK_PROJECT_FILES);
            return true;
        } catch (Exception ignored) { pendingProjectId = null; return false; }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_PROJECT_FILES) return;
        String projectId = pendingProjectId;
        pendingProjectId = null;
        if (resultCode != RESULT_OK || data == null || projectId == null) return;
        int added = 0;
        if (data.getClipData() != null) {
            for (int i = 0; i < data.getClipData().getItemCount(); i++)
                if (copyProjectFile(projectId, data.getClipData().getItemAt(i).getUri())) added++;
        } else if (data.getData() != null && copyProjectFile(projectId, data.getData())) added++;
        final int count = added;
        if (webView != null) webView.post(() -> webView.evaluateJavascript("onProjectFilesImported(" + count + ")", null));
    }

    private boolean copyProjectFile(String projectId, Uri uri) {
        if (uri == null) return false;
        String displayName = "File";
        String mimeType = getContentResolver().getType(uri);
        long reportedSize = -1;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int ni = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int si = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (ni >= 0) displayName = cursor.getString(ni);
                if (si >= 0 && !cursor.isNull(si)) reportedSize = cursor.getLong(si);
            }
        } catch (Exception ignored) { }
        if (displayName == null || displayName.trim().isEmpty() || reportedSize > MAX_PROJECT_FILE_BYTES) return false;
        String fileId = UUID.randomUUID().toString();
        File dir = new File(new File(getFilesDir(), "projects"), projectId);
        if (!dir.exists() && !dir.mkdirs()) return false;
        String extension = "";
        int dot = displayName.lastIndexOf('.');
        if (dot >= 0 && dot < displayName.length() - 1) {
            String ext = displayName.substring(dot).replaceAll("[^A-Za-z0-9.]", "");
            if (ext.length() <= 12) extension = ext;
        }
        File target = new File(dir, fileId + extension);
        long copied = 0;
        try (InputStream in = getContentResolver().openInputStream(uri);
             FileOutputStream out = new FileOutputStream(target)) {
            if (in == null) return false;
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) != -1) {
                copied += n;
                if (copied > MAX_PROJECT_FILE_BYTES) { target.delete(); return false; }
                out.write(buffer, 0, n);
            }
            out.flush();
            ProjectRepository repo = new ProjectRepository(this);
            boolean saved;
            try {
                saved = repo.addProjectFile(projectId, fileId, displayName, projectId + "/" + target.getName(), mimeType, copied);
                if (saved) {
                    ProjectTextExtractor.Result extracted = ProjectTextExtractor.extract(target, displayName);
                    repo.updateFileExtraction(fileId, extracted.status, extracted.text);
                }
            } finally { repo.close(); }
            if (!saved) target.delete();
            return saved;
        } catch (Exception ignored) { target.delete(); return false; }
    }

    private boolean removeProjectFile(String fileId) {
        ProjectRepository repo = new ProjectRepository(this);
        String path;
        boolean removed;
        try { path = repo.getProjectFilePath(fileId); removed = repo.deleteProjectFile(fileId); }
        finally { repo.close(); }
        if (removed && path != null) {
            File root = new File(getFilesDir(), "projects");
            File target = new File(root, path);
            try {
                if (target.getCanonicalPath().startsWith(root.getCanonicalPath() + File.separator)) target.delete();
            } catch (Exception ignored) { }
        }
        return removed;
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
