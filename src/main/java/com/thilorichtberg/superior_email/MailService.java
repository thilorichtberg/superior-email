package com.thilorichtberg.superior_email;

import jakarta.mail.Address;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Service
public class MailService {

    private final String host;
    private final int port;
    private final String username;
    private final String password;

    public MailService(
            @Value("${mail.imap.host}") String host,
            @Value("${mail.imap.port}") int port,
            @Value("${mail.imap.username}") String username,
            @Value("${mail.imap.password}") String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    /**
     * Returns up to {@code count} of the newest INBOX messages, newest first.
     * The IMAP connection is fully closed before this method returns.
     */
    public List<EmailSummary> fetchRecent(int count) throws MessagingException {
        Properties props = new Properties();
        // "imaps" = IMAP over an implicit TLS socket (Gmail: imap.gmail.com:993).
        props.put("mail.store.protocol", "imaps");

        Session session = Session.getInstance(props);

        // Store and Folder are AutoCloseable, so try-with-resources guarantees the
        // socket and the folder handle are released even if fetching throws.
        try (Store store = session.getStore("imaps")) {
            store.connect(host, port, username, password);

            try (Folder inbox = store.getFolder("INBOX")) {
                inbox.open(Folder.READ_ONLY);

                // Jakarta Mail message numbers are 1-based, oldest = 1.
                int total = inbox.getMessageCount();
                int first = Math.max(1, total - count + 1);

                Message[] messages = inbox.getMessages(first, total);

                List<EmailSummary> summaries = new ArrayList<>(messages.length);
                for (Message message : messages) {
                    summaries.add(toSummary(message));
                }
                // getMessages() hands them back oldest-first; we want newest-first.
                Collections.reverse(summaries);
                return summaries;
            }
        }
    }

    private EmailSummary toSummary(Message message) throws MessagingException {
        Address[] fromAddresses = message.getFrom();
        String from = (fromAddresses != null && fromAddresses.length > 0)
                ? fromAddresses[0].toString()
                : "(unknown sender)";

        String subject = message.getSubject();
        if (subject == null) {
            subject = "(no subject)";
        }

        // INTERNALDATE (server receive time); fall back to the Date: header.
        Date received = message.getReceivedDate();
        if (received == null) {
            received = message.getSentDate();
        }
        Instant receivedAt = (received != null) ? received.toInstant() : null;

        return new EmailSummary(from, subject, receivedAt);
    }
}
