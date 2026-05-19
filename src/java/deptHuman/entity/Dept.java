// Dept.java
package com.example.attendance.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Dept {
    private Long id;
    private Long parentId;
    private String name;
    private Integer level;
    private Integer systemManpower;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}