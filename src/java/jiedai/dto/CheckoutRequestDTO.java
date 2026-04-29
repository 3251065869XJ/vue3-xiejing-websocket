// CheckoutRequestDTO.java
package com.example.visitor.dto;
import lombok.Data;

@Data
public class CheckoutRequestDTO {
    private Integer visitorReceptionId; // 访客接待明细ID
    private String verifyCode;          // 工号 或 身份证号
}