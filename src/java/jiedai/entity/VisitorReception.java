// VisitorReception.java
package com.example.visitor.entity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VisitorReception {
    private Integer id;
    private Integer sessionId;
    private String visitorName;
    private Integer visitorType; // 0:外部门员工,1:外公司人员
    private String employeeId;
    private String department;
    private String company;
    private String idNumber;
    private Integer checkoutStatus; // 0:未签离,1:已签离
    private LocalDateTime checkoutTime;
    private LocalDateTime createTime;
}