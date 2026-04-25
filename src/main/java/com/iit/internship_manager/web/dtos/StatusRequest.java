package com.iit.internship_manager.web.dtos;

import com.iit.internship_manager.domain.enums.SujetStatus;

import lombok.Data;

// Inside com.iit.internship_manager.web.dtos
@Data
public class StatusRequest {
    private SujetStatus status;
}