package com.lpu.admin.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lpu.admin.service.dto.DeliveryDto;
import com.lpu.admin.service.service.AdminService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import com.lpu.admin.service.config.SecurityConfig;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testDashboard() throws Exception {

        Map<String, Object> mockData = new HashMap<>();
        mockData.put("totalDeliveries", 5);

        Mockito.when(adminService.getDashboard())
                .thenReturn(mockData);

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetDeliveries() throws Exception {

        Mockito.when(adminService.getAllDeliveries())
                .thenReturn(List.of(new DeliveryDto()));

        mockMvc.perform(get("/admin/deliveries"))
                .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testResolveDelivery() throws Exception {

        Mockito.when(adminService.resolveDelivery(Mockito.anyLong(), Mockito.any()))
                .thenReturn(new DeliveryDto());

        mockMvc.perform(put("/admin/deliveries/1/resolve")
                .param("status", "DELIVERED")
                .with(csrf()))
                .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void testResolveDelivery_Unauthorized() throws Exception {

        mockMvc.perform(put("/admin/deliveries/1/resolve")
                .param("status", "DELIVERED")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testReports() throws Exception {

        Map<String, Long> mock = new HashMap<>();
        mock.put("DELIVERED", 10L);

        Mockito.when(adminService.generateReports())
                .thenReturn(mock);

        mockMvc.perform(get("/admin/reports"))
                .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testUsers() throws Exception {

        Mockito.when(adminService.getUsers())
                .thenReturn("User management via Auth Service");

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testHubs() throws Exception {

        Mockito.when(adminService.getHubs())
                .thenReturn("Hub management module");

        mockMvc.perform(get("/admin/hubs"))
                .andExpect(status().isOk());
    }
}
