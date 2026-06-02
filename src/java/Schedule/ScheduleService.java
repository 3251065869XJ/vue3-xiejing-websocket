// ====================== 核心业务服务 ======================
package com.example.schedule.service;

import com.example.schedule.dto.ScheduleItemDto;
import com.example.schedule.entity.ScheduleData;
import com.example.schedule.repository.ScheduleDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");  // 统一时区
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private ScheduleDataRepository scheduleDataRepository;

    @Autowired
    private MehrUploadService mehrUploadService;

    /**
     * 提交排班数据（核心业务逻辑）
     * @param classDateStr 班次日期字符串
     * @param shiftName 班次名称（白班/夜班）
     * @param dtoList 排班数据列表
     * @return 处理结果描述
     */
    @Transactional(rollbackFor = Exception.class)
    public String submitSchedule(String classDateStr, String shiftName, List<ScheduleItemDto> dtoList) {
        // 1. 参数转换和校验
        LocalDate classDate = LocalDate.parse(classDateStr, DATE_FORMATTER);
        LocalDate today = LocalDate.now(ZONE_ID);
        LocalTime nowTime = LocalTime.now(ZONE_ID);
        LocalDateTime nowDateTime = LocalDateTime.now(ZONE_ID);

        // 2. 根据白班/夜班分别处理
        if ("白班".equals(shiftName)) {
            return handleDayShift(classDate, today, nowTime, nowDateTime, dtoList);
        } else if ("夜班".equals(shiftName)) {
            return handleNightShift(classDate, today, nowTime, nowDateTime, dtoList);
        } else {
            throw new IllegalArgumentException("班次名称必须是'白班'或'夜班'");
        }
    }

    /**
     * 白班业务逻辑
     */
    private String handleDayShift(LocalDate classDate, LocalDate today, LocalTime nowTime,
                                  LocalDateTime nowDateTime, List<ScheduleItemDto> dtoList) {
        // 场景1：班次日期 = 当前日期 且 提交时间 > 20:30 → 拦截
        if (classDate.equals(today) && nowTime.isAfter(LocalTime.of(20, 30))) {
            throw new RuntimeException("白班班次日期为今天时，提交时间不能晚于20:30，已拦截");
        }

        // 场景2：班次日期 > 当前日期 → 暂存（不上传MEHR），is_upload_mehr=2
        if (classDate.isAfter(today)) {
            List<ScheduleData> entityList = convertToEntities(dtoList, classDate, "白班", nowDateTime);
            entityList.forEach(e -> e.setIsUploadMehr(2));  // 未上传
            scheduleDataRepository.saveAll(entityList);
            return "班次日期在未来，已暂存排班数据（未上传MEHR）";
        }

        // 场景3：班次日期 = 当前日期 且 提交时间 ≤ 20:30 → 保存并上传MEHR
        if (classDate.equals(today)) {
            List<ScheduleData> entityList = convertToEntities(dtoList, classDate, "白班", nowDateTime);
            // 先保存数据（临时状态）
            entityList.forEach(e -> e.setIsUploadMehr(2));
            List<ScheduleData> savedList = scheduleDataRepository.saveAll(entityList);

            // 调用MEHR上传
            boolean uploadSuccess = mehrUploadService.uploadToMehr(savedList, "白班", classDate);
            if (uploadSuccess) {
                // 上传成功 → 更新is_upload_mehr=1
                List<Long> ids = savedList.stream().map(ScheduleData::getId).collect(Collectors.toList());
                scheduleDataRepository.batchUpdateUploadStatus(ids, 1);
                return "排班数据已保存并成功上传至MEHR系统";
            } else {
                // 上传失败 → 标记为0（失败），重试次数0，等待定时任务重试
                List<Long> ids = savedList.stream().map(ScheduleData::getId).collect(Collectors.toList());
                scheduleDataRepository.incrementRetryAndSetStatus(ids, 0);
                return "排班数据已保存但上传MEHR失败，将自动重试";
            }
        }

        // 其他情况（理论上只有小于当前日期，白班不允许补录）
        throw new RuntimeException("白班不允许提交过去日期的排班数据");
    }

    /**
     * 夜班业务逻辑
     */
    private String handleNightShift(LocalDate classDate, LocalDate today, LocalTime nowTime,
                                    LocalDateTime nowDateTime, List<ScheduleItemDto> dtoList) {
        // 场景1：班次日期 < 当前日期
        if (classDate.isBefore(today)) {
            // 1.1 如果提交时间 > 01:00:00 → 拦截
            if (nowTime.isAfter(LocalTime.of(1, 0, 0))) {
                throw new RuntimeException("夜班班次日期在过去时，提交时间不能晚于01:00:00，已拦截");
            }
            // 1.2 提交时间 ≤ 01:00:00，且班次日期 = 昨天 → 保存并上传MEHR
            if (classDate.equals(today.minusDays(1))) {
                return saveAndUpload(dtoList, classDate, "夜班", nowDateTime);
            } else {
                // 更早的日期一律拦截
                throw new RuntimeException("夜班只允许提交昨天或今天及未来的排班数据，更早日期不允许");
            }
        }

        // 场景2：班次日期 > 当前日期 → 暂存，is_upload_mehr=2
        if (classDate.isAfter(today)) {
            List<ScheduleData> entityList = convertToEntities(dtoList, classDate, "夜班", nowDateTime);
            entityList.forEach(e -> e.setIsUploadMehr(2));
            scheduleDataRepository.saveAll(entityList);
            return "班次日期在未来，已暂存排班数据（未上传MEHR）";
        }

        // 场景3：班次日期 = 当前日期
        if (classDate.equals(today)) {
            // 提交时间必须在20:00:00 ~ 23:59:59之间
            LocalTime start = LocalTime.of(20, 0, 0);
            LocalTime end = LocalTime.of(23, 59, 59);
            if (nowTime.isAfter(start) && nowTime.isBefore(end) || nowTime.equals(start)) {
                return saveAndUpload(dtoList, classDate, "夜班", nowDateTime);
            } else {
                throw new RuntimeException("夜班班次日期为今天时，提交时间必须在20:00:00~23:59:59之间");
            }
        }

        throw new RuntimeException("未知错误");
    }

    /**
     * 保存数据并上传MEHR（公共方法）
     */
    private String saveAndUpload(List<ScheduleItemDto> dtoList, LocalDate classDate, String shiftName, LocalDateTime submitTime) {
        List<ScheduleData> entityList = convertToEntities(dtoList, classDate, shiftName, submitTime);
        entityList.forEach(e -> e.setIsUploadMehr(2));
        List<ScheduleData> savedList = scheduleDataRepository.saveAll(entityList);

        boolean uploadSuccess = mehrUploadService.uploadToMehr(savedList, shiftName, classDate);
        if (uploadSuccess) {
            List<Long> ids = savedList.stream().map(ScheduleData::getId).collect(Collectors.toList());
            scheduleDataRepository.batchUpdateUploadStatus(ids, 1);
            return "排班数据已保存并成功上传MEHR系统";
        } else {
            List<Long> ids = savedList.stream().map(ScheduleData::getId).collect(Collectors.toList());
            scheduleDataRepository.incrementRetryAndSetStatus(ids, 0);
            return "排班数据已保存但上传MEHR失败，将自动重试";
        }
    }

    /**
     * 将DTO转换为实体列表
     */
    private List<ScheduleData> convertToEntities(List<ScheduleItemDto> dtoList, LocalDate classDate,
                                                  String shiftName, LocalDateTime submitTime) {
        List<ScheduleData> list = new ArrayList<>();
        for (ScheduleItemDto dto : dtoList) {
            ScheduleData entity = new ScheduleData();
            entity.setClassDate(classDate);
            entity.setShiftName(shiftName);
            entity.setEmployeeId(dto.getEmployeeId());
            entity.setScheduleInfo(dto.getScheduleInfo());
            entity.setSubmitTime(submitTime);
            entity.setCreateTime(submitTime);
            entity.setRetryCount(0);   // 初始重试次数为0
            list.add(entity);
        }
        return list;
    }
}