// ====================== DTO ======================
package com.example.schedule.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 排班提交请求DTO
 */
public class ScheduleSubmitRequest {
    @NotBlank(message = "班次日期不能为空")
    private String classDate;          // YYYY-MM-DD格式

    @NotBlank(message = "班次名称不能为空")
    private String shiftName;          // 白班 或 夜班

    @NotNull(message = "排班数据不能为空")
    private List<ScheduleItemDto> scheduleData;

    // getter/setter
    public String getClassDate() { return classDate; }
    public void setClassDate(String classDate) { this.classDate = classDate; }
    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }
    public List<ScheduleItemDto> getScheduleData() { return scheduleData; }
    public void setScheduleData(List<ScheduleItemDto> scheduleData) { this.scheduleData = scheduleData; }
}

/**
 * 单条排班数据DTO
 */
public class ScheduleItemDto {
    private String employeeId;       // 员工ID
    private String scheduleInfo;     // 排班详情
    // 可根据实际业务扩展字段

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getScheduleInfo() { return scheduleInfo; }
    public void setScheduleInfo(String scheduleInfo) { this.scheduleInfo = scheduleInfo; }
}