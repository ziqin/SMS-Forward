package com.enixcoda.smsforward;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class AbstractWebForwarder implements Forwarder {
    private final String TAG = getClass().getSimpleName();
    private final URL endpoint;

    public AbstractWebForwarder(String endpoint) {
        try {
            this.endpoint = new URL(endpoint);
            String protocol = this.endpoint.getProtocol();
            if (!("https".equals(protocol) || "http".equals(protocol))) {
                throw new IllegalArgumentException("Expect https or http URL");
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Malformed endpoint URL", e);
        }
    }

    @Override
    public void forward(String fromNumber, String content) throws IOException {
        byte[] body = makeBody(fromNumber, content);

        HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection();
        try {
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setDoOutput(true);
            connection.setDoInput(false);
            connection.setRequestProperty("Content-Type", getContentType());

            try (OutputStream out = connection.getOutputStream()) {
                out.write(body);
                out.flush();
            }

            int status = connection.getResponseCode();
            Log.d(TAG, String.format("response: status=%d", status));
        } finally {
            connection.disconnect();
        }
    }

    protected abstract byte[] makeBody(String fromNumber, String content);

    protected abstract String getContentType();
}
