package com.enixcoda.smsforward;

public interface Forwarder {
    void forward(String fromNumber, String content) throws Exception;
}
