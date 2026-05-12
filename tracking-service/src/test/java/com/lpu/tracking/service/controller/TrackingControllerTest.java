package com.lpu.tracking.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lpu.tracking.service.service.TrackingService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.context.annotation.Import;

import com.lpu.tracking.service.config.SecurityConfig;

import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(
        controllers = TrackingController.class,
        properties = {
                "spring.cloud.config.enabled=false",
                "spring.cloud.config.fail-fast=false",
                "eureka.client.enabled=false"
        }
)
@Import(SecurityConfig.class)
class TrackingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrackingService trackingService;

    @Autowired
    private ObjectMapper objectMapper;
    
    //GET Tracking History
    @Test
    @WithMockUser(roles = "USER")
    void testGetTrackingHistory() throws Exception {

        Mockito.when(trackingService.getTrackingHistory("123"))
                .thenReturn(java.util.List.of());

        mockMvc.perform(get("/tracking/123"))
                .andExpect(status().isOk());
    }
    
    //Add Tracking Event (ADMIN)
    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddTrackingEvent() throws Exception {

        Mockito.when(trackingService.addTrackingEvent(Mockito.any()))
                .thenReturn("Tracking event added");

        mockMvc.perform(post("/tracking/events")
                .with(csrf())
                .contentType("application/json")
                .content("{\"trackingNumber\":\"123\",\"status\":\"IN_TRANSIT\"}"))
                .andExpect(status().isOk());
    }
    
    //user not allowed
    @Test
    @WithMockUser(roles = "USER")
    void testAddTrackingEvent_Unauthorized() throws Exception {

        mockMvc.perform(post("/tracking/events")
                .with(csrf())
                .contentType("application/json")
                .content("{\"trackingNumber\":\"123\",\"status\":\"IN_TRANSIT\"}"))
                .andExpect(status().isForbidden());
    }
    
    //File Upload Test
    @Test
    @WithMockUser(roles = "USER")
    void testUploadDocument() throws Exception {

        Mockito.when(trackingService.uploadDocument(Mockito.any(), Mockito.any()))
                .thenReturn("File uploaded successfully");

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile(
                        "file",
                        "test.txt",
                        "text/plain",
                        "Hello World".getBytes()
                );

        mockMvc.perform(multipart("/tracking/documents/upload")
                .file(file)
                .param("trackingNumber", "123")
                .with(csrf()))
                .andExpect(status().isOk());
    }
}
