// DeptTreeNodeVO.java
package com.example.attendance.vo;

import lombok.Data;
import java.util.List;

@Data
public class DeptTreeNodeVO {
    private Long id;
    private String label;
    private List<DeptTreeNodeVO> children;
}