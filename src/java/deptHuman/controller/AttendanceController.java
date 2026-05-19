package com.example.attendance.controller;

import com.example.attendance.dto.AttendanceQueryDTO;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.service.DeptService;
import com.example.attendance.vo.AttendanceDetailVO;
import com.example.attendance.vo.AttendanceSummaryVO;
import com.example.attendance.vo.DeptTreeNodeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // 开发环境允许跨域
public class AttendanceController {

    @Autowired
    private DeptService deptService;
    @Autowired
    private AttendanceService attendanceService;

    @GetMapping("/dept/tree")
    public List<DeptTreeNodeVO> getDeptTree() {
        return deptService.getDeptTree();
    }

    @GetMapping("/attendance/summary")
    public AttendanceSummaryVO getSummary(AttendanceQueryDTO queryDTO) {
        // 参数校验
        if (queryDTO.getDeptId() == null || queryDTO.getDate() == null) {
            throw new IllegalArgumentException("部门ID和日期不能为空");
        }
        return attendanceService.getSummary(queryDTO);
    }

    @GetMapping("/attendance/detail")
    public List<AttendanceDetailVO> getDetail(AttendanceQueryDTO queryDTO) {
        if (queryDTO.getDeptId() == null || queryDTO.getDate() == null || queryDTO.getShiftType() == null) {
            throw new IllegalArgumentException("部门ID、日期和班次不能为空");
        }
        // 明细接口班次必须为 day 或 night
        if (!"day".equals(queryDTO.getShiftType()) && !"night".equals(queryDTO.getShiftType())) {
            throw new IllegalArgumentException("班次参数错误，需为 day 或 night");
        }
        return attendanceService.getDetail(queryDTO);
    }
}