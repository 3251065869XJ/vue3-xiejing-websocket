// ReceptionSession.java
package com.example.visitor.entity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReceptionSession {
    private Integer id;
    private Integer receptionistId;
    private String product;
    private LocalDateTime receptionTime;
    private LocalDateTime createTime;
}