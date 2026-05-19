package com.example.attendance.service;

import com.example.attendance.dto.AttendanceQueryDTO;
import com.example.attendance.vo.AttendanceDetailVO;
import com.example.attendance.vo.AttendanceSummaryVO;
import java.util.List;

public interface AttendanceService {
    AttendanceSummaryVO getSummary(AttendanceQueryDTO queryDTO);
    List<AttendanceDetailVO> getDetail(AttendanceQueryDTO queryDTO);
}