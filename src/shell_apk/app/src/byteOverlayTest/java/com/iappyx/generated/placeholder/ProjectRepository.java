package com.iappyx.generated.placeholder;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.UUID;

/** Local, app-private project catalog. File bytes will live in a separate private directory. */
public final class ProjectRepository extends SQLiteOpenHelper {
    private static final String DB_NAME = "byte_projects.db";
    private static final int DB_VERSION = 3;

    public ProjectRepository(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE projects (" +
                "id TEXT PRIMARY KEY, name TEXT NOT NULL, description TEXT NOT NULL DEFAULT '', " +
                "status TEXT NOT NULL DEFAULT 'active', created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE project_notes (" +
                "id TEXT PRIMARY KEY, project_id TEXT NOT NULL, title TEXT NOT NULL, body TEXT NOT NULL, " +
                "created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL, " +
                "FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE project_files (" +
                "id TEXT PRIMARY KEY, project_id TEXT NOT NULL, display_name TEXT NOT NULL, " +
                "relative_path TEXT NOT NULL, mime_type TEXT NOT NULL DEFAULT 'application/octet-stream', " +
                "size_bytes INTEGER NOT NULL DEFAULT 0, added_at INTEGER NOT NULL, " +
                "extraction_status TEXT NOT NULL DEFAULT 'pending', extracted_text TEXT NOT NULL DEFAULT '', " +
                "FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE CASCADE)");
        db.execSQL("CREATE INDEX idx_project_notes_project ON project_notes(project_id)");
        db.execSQL("CREATE INDEX idx_project_files_project ON project_files(project_id)");
        db.execSQL("PRAGMA foreign_keys=ON");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE project_files ADD COLUMN extraction_status TEXT NOT NULL DEFAULT 'pending'");
            db.execSQL("ALTER TABLE project_files ADD COLUMN extracted_text TEXT NOT NULL DEFAULT ''");
        }
    }

    public synchronized JSONObject getProjectDetails(String projectId) {
        JSONObject result = new JSONObject();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT id,name,description,status,created_at,updated_at FROM projects WHERE id=?", new String[]{projectId})) {
            if (!c.moveToFirst()) return result;
            result.put("id", c.getString(0)); result.put("name", c.getString(1));
            result.put("description", c.getString(2)); result.put("status", c.getString(3));
            result.put("created_at", c.getLong(4)); result.put("updated_at", c.getLong(5));
            JSONArray files = new JSONArray();
            try (Cursor f = db.rawQuery("SELECT id,display_name,mime_type,size_bytes,added_at,extraction_status,extracted_text FROM project_files WHERE project_id=? ORDER BY added_at DESC", new String[]{projectId})) {
                while (f.moveToNext()) {
                    JSONObject item = new JSONObject();
                    item.put("id", f.getString(0)); item.put("name", f.getString(1));
                    item.put("mime_type", f.getString(2)); item.put("size_bytes", f.getLong(3));
                    item.put("added_at", f.getLong(4)); item.put("extraction_status", f.getString(5));
                    item.put("indexed_chars", f.getString(6) == null ? 0 : f.getString(6).length()); files.put(item);
                }
            }
            JSONArray notes = new JSONArray();
            try (Cursor n = db.rawQuery("SELECT id,title,body,created_at,updated_at FROM project_notes WHERE project_id=? ORDER BY updated_at DESC", new String[]{projectId})) {
                while (n.moveToNext()) {
                    JSONObject item = new JSONObject();
                    item.put("id", n.getString(0)); item.put("title", n.getString(1));
                    item.put("body", n.getString(2)); item.put("created_at", n.getLong(3));
                    item.put("updated_at", n.getLong(4)); notes.put(item);
                }
            }
            result.put("files", files); result.put("notes", notes);
        } catch (Exception ignored) { }
        return result;
    }

    public synchronized boolean addProjectFile(String projectId, String fileId, String displayName, String relativePath, String mimeType, long sizeBytes) {
        if (projectId == null || fileId == null || relativePath == null || displayName == null) return false;
        android.content.ContentValues values = new android.content.ContentValues();
        values.put("id", fileId); values.put("project_id", projectId); values.put("display_name", displayName);
        values.put("relative_path", relativePath); values.put("mime_type", mimeType == null ? "application/octet-stream" : mimeType);
        values.put("size_bytes", sizeBytes); values.put("added_at", System.currentTimeMillis());
        boolean saved = getWritableDatabase().insert("project_files", null, values) != -1;
        if (saved) touchProject(projectId);
        return saved;
    }

    public synchronized String getProjectFilePath(String fileId) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT relative_path FROM project_files WHERE id=?", new String[]{fileId})) {
            return c.moveToFirst() ? c.getString(0) : null;
        }
    }

    public synchronized boolean deleteProjectFile(String fileId) {
        SQLiteDatabase db = getWritableDatabase();
        String projectId = null;
        try (Cursor c = db.rawQuery("SELECT project_id FROM project_files WHERE id=?", new String[]{fileId})) {
            if (c.moveToFirst()) projectId = c.getString(0);
        }
        int removed = db.delete("project_files", "id=?", new String[]{fileId});
        if (removed > 0 && projectId != null) touchProject(projectId);
        return removed > 0;
    }

    public synchronized boolean addProjectNote(String projectId, String title, String body) {
        if (projectId == null || title == null || body == null || title.trim().isEmpty()) return false;
        if (title.trim().length() > 120 || body.length() > 10000) return false;
        long now = System.currentTimeMillis();
        android.content.ContentValues values = new android.content.ContentValues();
        values.put("id", UUID.randomUUID().toString()); values.put("project_id", projectId);
        values.put("title", title.trim()); values.put("body", body.trim());
        values.put("created_at", now); values.put("updated_at", now);
        boolean saved = getWritableDatabase().insert("project_notes", null, values) != -1;
        if (saved) touchProject(projectId);
        return saved;
    }


    public synchronized boolean updateFileExtraction(String fileId, String status, String text) {
        if (fileId == null || status == null) return false;
        android.content.ContentValues values = new android.content.ContentValues();
        values.put("extraction_status", status);
        values.put("extracted_text", text == null ? "" : text);
        return getWritableDatabase().update("project_files", values, "id=?", new String[]{fileId}) > 0;
    }

    public synchronized JSONArray searchProjectFiles(String projectId, String query) {
        JSONArray results = new JSONArray();
        if (projectId == null || query == null) return results;
        String term = query.trim();
        if (term.length() < 2 || term.length() > 120) return results;
        String needle = term.toLowerCase(java.util.Locale.ROOT);
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT display_name,extracted_text FROM project_files WHERE project_id=? AND extraction_status='indexed' AND instr(lower(extracted_text), lower(?))>0 ORDER BY added_at DESC LIMIT 30",
                new String[]{projectId, term})) {
            while (c.moveToNext()) {
                String name = c.getString(0);
                String content = c.getString(1);
                String lower = content == null ? "" : content.toLowerCase(java.util.Locale.ROOT);
                int at = lower.indexOf(needle);
                int start = Math.max(0, at - 100);
                int end = Math.min(content.length(), at + term.length() + 160);
                String snippet = content.substring(start, end).replaceAll("\\s+", " ").trim();
                if (start > 0) snippet = "… " + snippet;
                if (end < content.length()) snippet += " …";
                JSONObject item = new JSONObject();
                item.put("name", name);
                item.put("snippet", snippet);
                results.put(item);
            }
        } catch (Exception ignored) { }
        return results;
    }

    private void touchProject(String projectId) {
        android.content.ContentValues values = new android.content.ContentValues();
        values.put("updated_at", System.currentTimeMillis());
        getWritableDatabase().update("projects", values, "id=?", new String[]{projectId});
    }

    public synchronized JSONArray listProjects() {
        JSONArray result = new JSONArray();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT id,name,description,status,created_at,updated_at FROM projects ORDER BY updated_at DESC", null)) {
            while (c.moveToNext()) {
                JSONObject item = new JSONObject();
                try {
                    item.put("id", c.getString(0));
                    item.put("name", c.getString(1));
                    item.put("description", c.getString(2));
                    item.put("status", c.getString(3));
                    item.put("created_at", c.getLong(4));
                    item.put("updated_at", c.getLong(5));
                    result.put(item);
                } catch (Exception ignored) { }
            }
        }
        return result;
    }

    public synchronized boolean createProject(String rawName, String rawDescription) {
        String name = rawName == null ? "" : rawName.trim();
        String description = rawDescription == null ? "" : rawDescription.trim();
        if (name.isEmpty() || name.length() > 80 || description.length() > 2000) return false;
        long now = System.currentTimeMillis();
        android.content.ContentValues values = new android.content.ContentValues();
        values.put("id", UUID.randomUUID().toString());
        values.put("name", name);
        values.put("description", description);
        values.put("status", "active");
        values.put("created_at", now);
        values.put("updated_at", now);
        return getWritableDatabase().insert("projects", null, values) != -1;
    }

    public synchronized boolean updateProject(String id, String rawName, String rawDescription, String status) {
        if (id == null || id.trim().isEmpty()) return false;
        String name = rawName == null ? "" : rawName.trim();
        String description = rawDescription == null ? "" : rawDescription.trim();
        String safeStatus = "archived".equals(status) ? "archived" : "active";
        if (name.isEmpty() || name.length() > 80 || description.length() > 2000) return false;
        android.content.ContentValues values = new android.content.ContentValues();
        values.put("name", name);
        values.put("description", description);
        values.put("status", safeStatus);
        values.put("updated_at", System.currentTimeMillis());
        return getWritableDatabase().update("projects", values, "id=?", new String[]{id}) > 0;
    }

    public synchronized boolean deleteProject(String id) {
        if (id == null || id.trim().isEmpty()) return false;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("project_notes", "project_id=?", new String[]{id});
            db.delete("project_files", "project_id=?", new String[]{id});
            int removed = db.delete("projects", "id=?", new String[]{id});
            db.setTransactionSuccessful();
            return removed > 0;
        } finally { db.endTransaction(); }
    }

    @Override public synchronized void close() { super.close(); }
}