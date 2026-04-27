package com.iit.internship_manager.web.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class TeacherWorkloadDTO {
    private Long teacherId;
    private String teacherName;
    private int currentEncadrements;
    private int maxQuota;
    private double occupationPercentage; // (Current / Max) * 100
}