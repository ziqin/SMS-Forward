package com.enixcoda.smsforward;

import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public final class EmailForwarder implements Forwarder {
    private final InternetAddress fromAddress;
    private final InternetAddress[] toAddresses;
    private final Properties props;
    private final Authenticator authenticator;

    public EmailForwarder(InternetAddress fromAddress, InternetAddress[] toAddresses, String smtpHost, short port, String username, String password) {
        this.fromAddress = fromAddress;
        this.toAddresses = toAddresses;

        props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.connectiontimeout", "10000");
        props.setProperty("mail.smtp.timeout", "10000");
        props.setProperty("mail.smtp.writetimeout", "10000");
        props.setProperty("mail.smtp.allow8bitmime", "true");
        props.setProperty("mail.smtp.host", smtpHost);
        props.setProperty("mail.smtp.port", Short.toString(port));
        props.setProperty("mail.smtp.auth", "true");

        // https://www.oracle.com/docs/tech/java/sslnotes142.txt
        props.setProperty("mail.smtp.ssl.checkserveridentity", "true");
        if (port == 465) {
            // Implicit TLS
            props.setProperty("mail.smtp.ssl.enable", "true");
        } else {
            props.setProperty("mail.smtp.starttls.enable", "true");
            props.setProperty("mail.smtp.starttls.required", "true");
        }

        PasswordAuthentication authentication = new PasswordAuthentication(username, password);
        authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return authentication;
            }
        };
    }

    @Override
    public void forward(String fromNumber, String content) throws MessagingException {
        Session session = Session.getInstance(props, authenticator);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(fromAddress);
        message.addRecipients(Message.RecipientType.TO, toAddresses);
        message.setSubject("SMS from: " + fromNumber);
        message.setText(content, "UTF-8");
        Transport.send(message);
    }
}
