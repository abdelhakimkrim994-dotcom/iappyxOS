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
    private static final int DB_VERSION = 1;

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
                "FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE CASCADE)");
        db.execSQL("CREATE INDEX idx_project_notes_project ON project_notes(project_id)");
        db.execSQL("CREATE INDEX idx_project_files_project ON project_files(project_id)");
        db.execSQL("PRAGMA foreign_keys=ON");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Schema migrations will be added explicitly; never discard a user's project data.
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