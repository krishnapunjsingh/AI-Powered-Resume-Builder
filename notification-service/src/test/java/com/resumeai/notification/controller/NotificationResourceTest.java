package com.resumeai.notification.controller;

import com.resumeai.notification.dto.NotificationRequest;
import com.resumeai.notification.dto.NotificationResponse;
import com.resumeai.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class NotificationResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Test
    public void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/notifications/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Notification Service is running"));
    }

    @Test
    public void testSendNotification() throws Exception {
        String requestBody = """
                {
                    "recipientId": 1,
                    "type": "ATS_COMPLETE",
                    "title": "Resume Processed",
                    "message": "Your resume has been processed successfully",
                    "channel": "APP",
                    "relatedId": 100,
                    "relatedType": "RESUME"
                }
                """;

        mockMvc.perform(post("/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("Authorization", "Bearer dummy_token"))
                .andExpect(status().isCreated());
    }

    @Test
    public void testGetByRecipientId() throws Exception {
        mockMvc.perform(get("/notifications/recipient/1")
                        .header("Authorization", "Bearer dummy_token"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetUnreadCount() throws Exception {
        mockMvc.perform(get("/notifications/recipient/1/unread-count")
                        .header("Authorization", "Bearer dummy_token"))
                .andExpect(status().isOk());
    }

    @Test
    public void testMarkAsRead() throws Exception {
        mockMvc.perform(put("/notifications/1/mark-read")
                        .header("Authorization", "Bearer dummy_token"))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteNotification() throws Exception {
        mockMvc.perform(delete("/notifications/1")
                        .header("Authorization", "Bearer dummy_token"))
                .andExpect(status().isNoContent());
    }
}
