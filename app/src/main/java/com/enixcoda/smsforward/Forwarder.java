package com.enixcoda.smsforward;

import java.io.IOException;

public interface Forwarder {
    void forward(String fromNumber, String content) throws IOException;
}
