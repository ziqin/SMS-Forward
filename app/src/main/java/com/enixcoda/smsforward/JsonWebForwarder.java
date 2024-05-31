package com.enixcoda.smsforward;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public final class JsonWebForwarder extends AbstractWebForwarder {
    private static final String TAG = JsonWebForwarder.class.getSimpleName();

    public JsonWebForwarder(String endpoint) {
        super(endpoint);
    }

    @Override
    protected byte[] makeBody(String fromNumber, String content) {
        JSONObject body = new JSONObject();
        try {
            body.put("from", fromNumber);
            body.put("message", content);
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
