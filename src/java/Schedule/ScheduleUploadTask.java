// ====================== 定时任务 ======================
package com.example.schedule.task;

import com.example.schedule.entity.ScheduleData;
import com.example.schedule.repository.ScheduleDataRepository;
import com.example.schedule.service.MehrUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@EnableScheduling
public class ScheduleUploadTask {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleUploadTask.class);
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final int MAX_RETRY_COUNT = 3;  // 最大重试次数

    @Autowired
    private ScheduleDataRepository scheduleDataRepository;

    @Autowired
    private MehrUploadService mehrUploadService;

    /**
     * 白班定时任务：每天08:00:00执行
     * 查询当前系统日期对应的白班未上传成功的数据，进行重试上传
     */
    @Scheduled(cron = "0 0 8 * * ?", zone = "Asia/Shanghai")
    @Transactional(rollbackFor = Exception.class)
    public void retryDayShiftUpload() {
        logger.info("【白班定时任务】开始执行，时间：{}", LocalDate.now(ZONE_ID));
        LocalDate today = LocalDate.now(ZONE_ID);
        processRetry(today, "白班");
    }

    /**
     * 夜班定时任务：每天20:00:00执行
     * 查询当前系统日期对应的夜班未上传成功的数据，进行重试上传
     */
    @Scheduled(cron = "0 0 20 * * ?", zone = "Asia/Shanghai")
    @Transactional(rollbackFor = Exception.class)
    public void retryNightShiftUpload() {
        logger.info("【夜班定时任务】开始执行，时间：{}", LocalDate.now(ZONE_ID));
        LocalDate today = LocalDate.now(ZONE_ID);
        processRetry(today, "夜班");
    }

    /**
     * 通用重试处理逻辑
     * @param classDate 班次日期（当前系统日期）
     * @param shiftName 班次名称
     */
    private void processRetry(LocalDate classDate, String shiftName) {
        // 查询需要重试的数据（未上传成功且重试次数<3）
        List<ScheduleData> retryList = scheduleDataRepository.findRetryableData(classDate, shiftName, MAX_RETRY_COUNT);
        if (retryList.isEmpty()) {
            logger.info("【{}】无需要重试的数据", shiftName);
            return;
        }

        // 按上传批次分组（同一班次日期+班次名称的数据可整体上传，实际可按更细粒度批次号，这里直接整体处理）
        // 注意：如果数据量很大，建议分页处理，这里简化按全部一起处理
        logger.info("【{}】找到{}条待重试数据", shiftName, retryList.size());

        // 调用MEHR上传接口
        boolean uploadSuccess = mehrUploadService.uploadToMehr(retryList, shiftName, classDate);
        List<Long> ids = retryList.stream().map(ScheduleData::getId).collect(Collectors.toList());

        if (uploadSuccess) {
            // 上传成功 → 更新状态为已上传(1)，重置重试次数
            scheduleDataRepository.batchUpdateUploadStatus(ids, 1);
            logger.info("【{}】重试上传成功，已更新{}条数据状态为1", shiftName, ids.size());
        } else {
            // 上传失败 → 增加重试次数，判断是否达到最大重试次数
            scheduleDataRepository.incrementRetryAndSetStatus(ids, 0);
            // 查询更新后重试次数达到3次的记录，将其标记为最终失败(0)并停止重试（已是0状态，无需额外操作）
            List<ScheduleData> failedList = scheduleDataRepository.findAllById(ids);
            List<Long> finalFailedIds = failedList.stream()
                    .filter(s -> s.getRetryCount() >= MAX_RETRY_COUNT)
                    .map(ScheduleData::getId)
                    .collect(Collectors.toList());
            if (!finalFailedIds.isEmpty()) {
                logger.warn("【{}】有{}条数据重试已达{}次，将停止重试并标记为上传失败", shiftName, finalFailedIds.size(), MAX_RETRY_COUNT);
                // 此处状态已经是0，无需再次更新，仅记录日志
            }
            logger.info("【{}】重试上传失败，已增加重试次数", shiftName);
        }
    }
}