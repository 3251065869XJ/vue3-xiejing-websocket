package com.example.attendance.service;

import com.example.attendance.vo.DeptTreeNodeVO;
import java.util.List;

public interface DeptService {
    List<DeptTreeNodeVO> getDeptTree();
}