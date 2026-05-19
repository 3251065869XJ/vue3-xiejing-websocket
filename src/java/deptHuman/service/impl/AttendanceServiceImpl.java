package com.example.attendance.service.impl;

import com.example.attendance.dto.AttendanceQueryDTO;
import com.example.attendance.entity.Dept;
import com.example.attendance.mapper.AttendanceMapper;
import com.example.attendance.mapper.DeptMapper;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.vo.AttendanceDetailVO;
import com.example.attendance.vo.AttendanceSummaryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private DeptMapper deptMapper;
    @Autowired
    private AttendanceMapper attendanceMapper;

    @Override
    public AttendanceSummaryVO getSummary(AttendanceQueryDTO queryDTO) {
        Long deptId = queryDTO.getDeptId();
        LocalDate date = queryDTO.getDate();
        String shiftType = queryDTO.getShiftType();
        Dept rootDept = deptMapper.selectById(deptId);
        if (rootDept == null) return null;
        return buildSummaryTree(rootDept, date, shiftType);
    }

    private AttendanceSummaryVO buildSummaryTree(Dept dept, LocalDate date, String shiftType) {
        AttendanceSummaryVO vo = new AttendanceSummaryVO();
        vo.setId(String.valueOf(dept.getId()));
        vo.setDeptName(dept.getName());

        // 处理当前部门的指标
        boolean needDay = shiftType.equals("all") || shiftType.equals("day");
        boolean needNight = shiftType.equals("all") || shiftType.equals("night");

        if (needDay) {
            Map<String, Object> dayStats = attendanceMapper.sumMetricsByDeptId(dept.getId(), date, "day");
            vo.setDaySystemManpower(dept.getSystemManpower());  // 系统人力取自部门编制
            vo.setDayRequiredManpower(dept.getSystemManpower()); // 应出勤人力 = 系统人力
            vo.setDayActualAttendance(getIntValue(dayStats, "actual_attendance"));
            vo.setDayInventoryManpower(getIntValue(dayStats, "inventory_manpower"));
            vo.setDayReceiveCount(getIntValue(dayStats, "receive_count"));
            vo.setDaySupportCount(getIntValue(dayStats, "support_count"));
        }
        if (needNight) {
            Map<String, Object> nightStats = attendanceMapper.sumMetricsByDeptId(dept.getId(), date, "night");
            vo.setNightSystemManpower(dept.getSystemManpower());
            vo.setNightRequiredManpower(dept.getSystemManpower());
            vo.setNightActualAttendance(getIntValue(nightStats, "actual_attendance"));
            vo.setNightInventoryManpower(getIntValue(nightStats, "inventory_manpower"));
            vo.setNightReceiveCount(getIntValue(nightStats, "receive_count"));
            vo.setNightSupportCount(getIntValue(nightStats, "support_count"));
        }

        // 处理子部门
        List<Dept> children = deptMapper.selectByParentId(dept.getId());
        if (children != null && !children.isEmpty()) {
            List<AttendanceSummaryVO> childVOs = new ArrayList<>();
            for (Dept child : children) {
                AttendanceSummaryVO childVO = buildSummaryTree(child, date, shiftType);
                childVOs.add(childVO);
            }
            vo.setChildren(childVOs);
        }

        return vo;
    }

    private Integer getIntValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return 0;
        return ((Number) val).intValue();
    }

    @Override
    public List<AttendanceDetailVO> getDetail(AttendanceQueryDTO queryDTO) {
        Long deptId = queryDTO.getDeptId();
        LocalDate date = queryDTO.getDate();
        String shift = queryDTO.getShiftType();
        // 如果 shiftType 是 all，默认为 day（明细必须选择具体班次，前端传值应保证 day/night）
        if ("all".equals(shift)) {
            shift = "day";
        }
        return attendanceMapper.selectDetailByDeptAndShift(deptId, date, shift);
    }
}