package com.lsm.controller;

import com.lsm.config.SecurityConfigTest;
import com.lsm.config.TestConfig;
import com.lsm.config.TestEnvConfig;
import com.lsm.model.DTOs.AnnouncementDTO;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.service.AnnouncementService;
import com.lsm.service.AppUserService;
import com.lsm.service.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnnouncementController.class)
@Import({TestConfig.class, SecurityConfigTest.class})
@ActiveProfiles("test")
class AnnouncementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnnouncementService announcementService;

    @Autowired
    private AppUser testAppUser;

    private AnnouncementDTO announcementDTO;

    @BeforeEach
    void setUp() {
        announcementDTO = new AnnouncementDTO();
        announcementDTO.setId(1L);
        announcementDTO.setTitle("Test Announcement");
        announcementDTO.setContent("Test Content");
        announcementDTO.setClassId(1L);
        announcementDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void simpleTest() throws Exception {
        List<AnnouncementDTO> announcements = Arrays.asList(announcementDTO);
        when(announcementService.getAnnouncementsByClassId(any(AppUser.class), eq(1L)))
                .thenReturn(announcements);

        mockMvc.perform(get("/api/v1/announcements/class/1")
                        .with(authentication(new TestingAuthenticationToken(testAppUser, null, "ROLE_TEACHER")))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}