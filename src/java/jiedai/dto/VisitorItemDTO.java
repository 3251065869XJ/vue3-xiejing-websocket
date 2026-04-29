// VisitorItemDTO.java
package com.example.visitor.dto;
import lombok.Data;

@Data
public class VisitorItemDTO {
    private String name;
    private String typeLabel;      // "外部门员工" 或 "外公司人员"
    private String identityInfo;   // 前端展示用
    private String employeeId;     // 外部门员工工号
    private String department;     // 外部门员工部门
    private String company;        // 外公司名称
    private String idNumber;       // 身份证号
    // 内部标识类型
    private String type;           // "internal" 或 "external"
}