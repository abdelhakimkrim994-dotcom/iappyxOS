package com.iappyx.generated.placeholder;

import android.content.Context;
import org.json.JSONArray;

/** Coordinates a user message and the prototype reply for every Byte surface. */
public final class ConversationManager {
    private ConversationManager() {}

    public static boolean send(Context context, String rawMessage) {
        if (rawMessage == null) return false;
        String message = rawMessage.trim();
        if (message.isEmpty() || message.length() > 2000) return false;
        ConversationRepository.append(context, "user", message);
        ConversationRepository.append(context, "assistant", demoReply(message));
        return true;
    }

    public static String getMessagesJson(Context context) {
        JSONArray messages = ConversationRepository.getMessages(context);
        return messages.toString();
    }

    private static String demoReply(String message) {
        String text = message.toLowerCase();
        if (text.contains("joke"))
            return "Why did the robot go on holiday? Because it needed to recharge its batteries! 😂🔋";
        if (text.contains("yourself") || text.contains("who are you"))
            return "I'm Byte! Your friendly little robot companion. I love helping you explore, learn and figure things out. I can be playful, but I'll always be patient with you. 💙";
        if (text.contains("tip"))
            return "Here's a little tip: You don't need to understand everything at once. Break a big problem into tiny steps. One small victory at a time! ✨";
        if (text.contains("what can you do"))
            return "Right now, I'm a first prototype! Soon I'll be able to understand your screen, answer questions using AI, talk with you and help you interact with your phone. We're building those abilities together! 🚀";
        if (text.contains("hello") || text.contains("hi ") || text.equals("hi"))
            return "Heeey! 😄 Nice to see you! What are we exploring today?";
        if (text.contains("sad") || text.contains("tired"))
            return "Hey, take it easy. You don't have to figure everything out right now. I'm here whenever you need a hand. 💙";
        return "That's interesting! 😊 I'm still learning how to understand the world around you. My real AI brain isn't connected yet, but that's one of our next steps!";
    }
}
