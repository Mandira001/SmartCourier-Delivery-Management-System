package com.lpu.delivery_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lpu.delivery_service.dto.CreateDeliveryRequest;
import com.lpu.delivery_service.entity.Address;
import com.lpu.delivery_service.entity.DeliveryStatus;
import com.lpu.delivery_service.service.DeliveryService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.lpu.delivery_service.config.SecurityConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(
        controllers = DeliveryController.class,
        properties = {
                "spring.cloud.config.enabled=false",
                "spring.cloud.config.fail-fast=false",
                "eureka.client.enabled=false"
        }
)
@Import(SecurityConfig.class)
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeliveryService deliveryService;

    @Autowired
    private ObjectMapper objectMapper;

    
    @Test
    @WithMockUser(roles = "USER")
    void testCreateDelivery() throws Exception {

        // Prepare request
        CreateDeliveryRequest request = new CreateDeliveryRequest();

        Address sender = new Address();
        sender.setName("Sender");
        sender.setCity("City");
        sender.setPincode("123456");
        sender.setPhone("9999999999");

        Address receiver = new Address();
        receiver.setName("Receiver");
        receiver.setCity("City");
        receiver.setPincode("654321");
        receiver.setPhone("8888888888");

        request.setSenderAddress(sender);
        request.setReceiverAddress(receiver);

        // Mock service response
        Mockito.when(deliveryService.createDelivery(Mockito.any(), Mockito.any()))
                .thenReturn(new com.lpu.delivery_service.dto.DeliveryResponse());

        // Perform API call
        mockMvc.perform(post("/deliveries")
        		.with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Email", "test@mail.com")
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void testGetMyDeliveries() throws Exception {

        Mockito.when(deliveryService.getMyDeliveries(Mockito.any()))
                .thenReturn(java.util.List.of());

        mockMvc.perform(get("/deliveries/my")
                .header("X-User-Email", "test@mail.com"))
                .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateStatus() throws Exception {

        Mockito.when(deliveryService.updateStatus(Mockito.anyLong(), Mockito.any()))
                .thenReturn(new com.lpu.delivery_service.entity.Delivery());

        mockMvc.perform(put("/deliveries/1")
                .param("status", "PICKED_UP")
                .with(csrf()))
                .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void testUpdateStatus_Unauthorized() throws Exception {

        mockMvc.perform(put("/deliveries/1")
                .param("status", "PICKED_UP")
                .with(csrf()))
                .andExpect(status().isForbidden()); // 🔥 important
    }
}
