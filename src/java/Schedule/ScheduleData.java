// ====================== 实体类 ======================
package com.example.schedule.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排班数据实体类
 */
@Entity
@Table(name = "schedule_data")
public class ScheduleData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_date", nullable = false)
    private LocalDate classDate;          // 班次日期

    @Column(name = "shift_name", nullable = false)
    private String shiftName;             // 班次名称（白班/夜班）

    @Column(name = "employee_id")
    private String employeeId;            // 员工ID（示例字段）

    @Column(name = "schedule_info")
    private String scheduleInfo;          // 排班详情（示例字段）

    @Column(name = "is_upload_mehr")
    private Integer isUploadMehr;         // 1-已上传，2-未上传，0-上传失败

    @Column(name = "retry_count")
    private Integer retryCount;           // 重试次数（默认0）

    @Column(name = "submit_time")
    private LocalDateTime submitTime;     // 提交时间

    @Column(name = "create_time")
    private LocalDateTime createTime;     // 创建时间

    // 省略getter/setter（实际使用Lombok或手动生成）
    // 以下为手动生成的getter/setter示例，实际建议使用Lombok @Data
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getClassDate() { return classDate; }
    public void setClassDate(LocalDate classDate) { this.classDate = classDate; }
    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getScheduleInfo() { return scheduleInfo; }
    public void setScheduleInfo(String scheduleInfo) { this.scheduleInfo = scheduleInfo; }
    public Integer getIsUploadMehr() { return isUploadMehr; }
    public void setIsUploadMehr(Integer isUploadMehr) { this.isUploadMehr = isUploadMehr; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public LocalDateTime getSubmitTime() { return submitTime; }
    public void setSubmitTime(LocalDateTime submitTime) { this.submitTime = submitTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}