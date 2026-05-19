// Employee.java
package com.example.attendance.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Employee {
    private Long id;
    private String employeeNo;
    private String name;
    private Long deptId;
    private String section;
    private String type;
    private String esdSubmitter;
    private LocalDateTime createdAt;
}