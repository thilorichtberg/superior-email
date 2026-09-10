package com.thilorichtberg.superior_email;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MailService mailService;

    @Test
    void returnsSummariesAsJson() throws Exception {
        given(mailService.fetchRecent(10)).willReturn(List.of(
                new EmailSummary("Google <no-reply@accounts.google.com>", "Security alert",
                        Instant.parse("2026-09-10T01:29:06Z"))));

        mockMvc.perform(get("/accounts/1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].from").value("Google <no-reply@accounts.google.com>"))
                .andExpect(jsonPath("$[0].subject").value("Security alert"))
                .andExpect(jsonPath("$[0].receivedAt").value("2026-09-10T01:29:06Z"));
    }
}
