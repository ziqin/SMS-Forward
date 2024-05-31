package com.enixcoda.smsforward;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public final class TelegramForwarder extends AbstractWebForwarder {
    private static final String TAG = TelegramForwarder.class.getCanonicalName();

    private final String chatId;

    public TelegramForwarder(String token, String chatId) {
        super(new Uri.Builder()
                .scheme("https")
                .authority("api.telegram.org")
                .appendPath("bot" + token)
                .appendPath("sendMessage")
                .build()
                .toString());
        this.chatId = chatId;
    }

    @Override
    protected byte[] makeBody(String fromNumber, String content) {
        JSONObject body = new JSONObject();
        try {
            body.put("chat_id", chatId);
            body.put("text", String.format("Message from %s:\n%s", fromNumber, content));
        } catch (JSONException e) {
            Log.wtf(TAG, e);
            throw new RuntimeException(e);
        }
        return body.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected String getContentType() {
        return "application/json";
    }
}
