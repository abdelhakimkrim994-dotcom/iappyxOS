package com.iappyx.generated.placeholder;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.UUID;

/** One durable message store shared by the full-screen WebView and floating overlay. */
public final class ConversationRepository {
    private static final String PREFS = "byte_shared_conversation_v1";
    private static final String KEY_MESSAGES = "messages";
    private static final Object LOCK = new Object();

    private ConversationRepository() {}

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static JSONArray getMessages(Context context) {
        synchronized (LOCK) {
            String raw = prefs(context).getString(KEY_MESSAGES, null);
            if (raw != null) {
                try { return new JSONArray(raw); } catch (Exception ignored) { }
            }
            JSONArray initial = new JSONArray();
            initial.put(create("assistant", "Hey! I'm Byte! 😊\n\nI'm your personal little AI companion. You don't have to know everything. We'll figure it out together!"));
            initial.put(create("assistant", "You can talk to me, ask questions, or simply tell me what you're working on.\n\nI'm all ears! Well... all sensors. 😄"));
            save(context, initial);
            return initial;
        }
    }

    public static void append(Context context, String role, String text) {
        synchronized (LOCK) {
            JSONArray messages = getMessages(context);
            messages.put(create(role, text));
            // Keep the local prototype bounded; newest 300 messages are retained.
            JSONArray bounded = new JSONArray();
            int start = Math.max(0, messages.length() - 300);
            for (int i = start; i < messages.length(); i++) bounded.put(messages.optJSONObject(i));
            save(context, bounded);
        }
    }

    private static JSONObject create(String role, String text) {
        JSONObject item = new JSONObject();
        try {
            item.put("id", UUID.randomUUID().toString());
            item.put("role", role);
            item.put("text", text);
            item.put("timestamp", System.currentTimeMillis());
        } catch (Exception ignored) { }
        return item;
    }

    private static void save(Context context, JSONArray messages) {
        prefs(context).edit().putString(KEY_MESSAGES, messages.toString()).apply();
    }
}
