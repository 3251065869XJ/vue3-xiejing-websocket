// CheckinRequestDTO.java
package com.example.visitor.dto;
import lombok.Data;
import java.util.List;

@Data
public class CheckinRequestDTO {
    private String receptionistEmployeeId;  // 接待人工号
    private String product;                 // 产品名称
    private List<VisitorItemDTO> visitors;  // 访客列表
}