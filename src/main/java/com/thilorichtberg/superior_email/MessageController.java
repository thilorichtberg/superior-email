package com.thilorichtberg.superior_email;

import jakarta.mail.MessagingException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MessageController {

    private final MailService mailService;

    public MessageController(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * Preview data for the inbox list: subject, sender, date for the 10 newest messages.
     * {@code id} is a placeholder until the Account entity lands in Milestone 2 — there is
     * only one hardcoded mailbox for now, so the path variable is accepted but not used.
     */
    @GetMapping("/accounts/{id}/messages")
    public List<EmailSummary> listMessages(@PathVariable String id) throws MessagingException {
        return mailService.fetchRecent(10);
    }
}
