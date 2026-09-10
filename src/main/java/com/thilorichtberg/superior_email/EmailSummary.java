package com.thilorichtberg.superior_email;

import java.time.Instant;

/**
 * A minimal, library-agnostic view of one message. Deliberately does not expose
 * any Jakarta Mail types so callers never touch a connection that is already closed.
 */
public record EmailSummary(String from, String subject, Instant receivedAt) {
}
