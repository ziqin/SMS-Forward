package com.enixcoda.smsforward;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = SmsReceiver.class.getSimpleName();
    private static final Pattern REVERSE_MESSAGE_PATTERN = Pattern.compile("To (\\+?\\d+?):\\n((.|\\n)*)");

    private final Executor forwarderExecutor = Executors.newCachedThreadPool();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) return;

        // Large message might be broken into several parts.
        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        if (messages.length == 0) {
            Log.wtf(TAG, "Got empty message");
            return;
        }
        String senderNumber = messages[0].getDisplayOriginatingAddress();
        String messageContent = Arrays.stream(messages)
                .map(SmsMessage::getDisplayMessageBody)
                .collect(Collectors.joining());
        Log.d(TAG, String.format("Received SMS message from %s, content: %s", senderNumber, messageContent));

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enableSms = preferences.getBoolean(context.getString(R.string.key_enable_sms), false);
        String targetNumber = preferences.getString(context.getString(R.string.key_target_sms), "");
        boolean enableTelegram = preferences.getBoolean(context.getString(R.string.key_enable_telegram), false);
        String targetTelegram = preferences.getString(context.getString(R.string.key_target_telegram), "");
        String telegramToken = preferences.getString(context.getString(R.string.key_telegram_apikey), "");
        boolean enableWeb = preferences.getBoolean(context.getString(R.string.key_enable_web), false);
        String targetWeb = preferences.getString(context.getString(R.string.key_target_web), "");

        // TODO: add a dedicated preference item for reverse forwarding
        // Disables reverse forwarding too if no forwarders is enabled.
        if (!enableSms && !enableTelegram && !enableWeb) return;

        ArrayList<Forwarder> forwarders = new ArrayList<>(1);
        if (enableSms && !targetNumber.isEmpty()) {
            forwarders.add(new SmsForwarder(targetNumber));
        }
        if (enableTelegram && !targetTelegram.isEmpty() && !telegramToken.isEmpty()) {
            forwarders.add(new TelegramForwarder(targetTelegram, telegramToken));
        }
        if (enableWeb && !targetWeb.isEmpty()) {
            forwarders.add(new JsonWebForwarder(targetWeb));
        }

        if (senderNumber.equals(targetNumber)) {
            // Reverse message
            Matcher matcher = REVERSE_MESSAGE_PATTERN.matcher(messageContent);
            if (matcher.matches()) {
                String forwardNumber = matcher.replaceFirst("$1");
                String forwardContent = matcher.replaceFirst("$2");
                forwarderExecutor.execute(() -> {
                    try {
                        SmsForwarder.sendSmsTo(forwardNumber, forwardContent);
                    } catch (RuntimeException e) {
                        Log.e(SmsForwarder.class.getSimpleName(), "Failed to send SMS", e);
                    }
                });
            }
        } else {
            // Normal message, forwarded
            for (Forwarder forwarder : forwarders) {
                forwarderExecutor.execute(() -> {
                    try {
                        forwarder.forward(senderNumber, messageContent);
                    } catch (Exception e) {
                        Log.e(forwarder.getClass().getSimpleName(), "Failed to forward SMS", e);
                    }
                });
            }
        }
    }
}
