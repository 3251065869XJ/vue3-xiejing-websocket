package com.example.attendance.mapper;

import com.example.attendance.entity.AttendanceDaily;
import com.example.attendance.vo.AttendanceDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface AttendanceMapper {
    // 统计某个部门在指定日期、班次下的各项指标（汇总当前部门直接下属员工，不包含子部门）
    Map<String, Object> sumMetricsByDeptId(@Param("deptId") Long deptId,
                                           @Param("date") LocalDate date,
                                           @Param("shift") String shift);
    
    // 查询明细列表（根据部门ID、日期、班次）
    List<AttendanceDetailVO> selectDetailByDeptAndShift(@Param("deptId") Long deptId,
                                                        @Param("date") LocalDate date,
                                                        @Param("shift") String shift);
}