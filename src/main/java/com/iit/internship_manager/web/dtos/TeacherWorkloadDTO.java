package com.iit.internship_manager.web.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor // Added for better compatibility with mapping tools
@Builder // Added to make it easier to build in the Service layer
public class TeacherWorkloadDTO {
    private Long teacherId;
    private String teacherName;
    private String anneeId; // Added: Focuses the workload on a specific cycle
    private int currentEncadrements;
    private int maxQuota;
    private double occupationPercentage; // (Current / Max) * 100
}