// VisitorReceptionVO.java
package com.example.visitor.vo;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VisitorReceptionVO {
    private Integer id;
    private String visitorName;
    private String typeLabel;      // 外部门员工 / 外公司人员
    private String identityInfo;   // 用于前端展示
    private Integer visitorType;   // 0:内部,1:外部
    private String employeeId;     // 外部门员工工号
    private String department;
    private String company;
    private String idNumber;       // 身份证号
    private Integer checkoutStatus;
    private LocalDateTime checkoutTime;
}