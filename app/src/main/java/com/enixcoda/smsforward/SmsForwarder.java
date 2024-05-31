package com.enixcoda.smsforward;

import android.telephony.SmsManager;

import java.util.ArrayList;

public final class SmsForwarder implements Forwarder {
    private final String forwardToNumber;

    public SmsForwarder(String forwardToNumber) {
        this.forwardToNumber = forwardToNumber;
    }

    public static void sendSmsTo(String number, String content) {
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> parts = smsManager.divideMessage(content);
        smsManager.sendMultipartTextMessage(number, null, parts, null, null);
    }

    @Override
    public void forward(String fromNumber, String content) {
        String message = String.format("From %s:\n%s", fromNumber, content);
        SmsForwarder.sendSmsTo(forwardToNumber, message);
    }
}
