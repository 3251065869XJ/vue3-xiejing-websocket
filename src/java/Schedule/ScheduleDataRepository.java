// ====================== Repository ======================
package com.example.schedule.repository;

import com.example.schedule.entity.ScheduleData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

public interface ScheduleDataRepository extends JpaRepository<ScheduleData, Long> {
    /**
     * 查询指定班次日期、班次名称且未上传成功的数据（用于定时任务重试）
     * @param classDate 班次日期
     * @param shiftName 班次名称
     * @param maxRetryCount 最大重试次数限制
     * @return 未上传成功且重试次数小于限制的数据
     */
    @Query("SELECT s FROM ScheduleData s WHERE s.classDate = :classDate AND s.shiftName = :shiftName " +
           "AND s.isUploadMehr != 1 AND s.retryCount < :maxRetryCount")
    List<ScheduleData> findRetryableData(@Param("classDate") LocalDate classDate,
                                         @Param("shiftName") String shiftName,
                                         @Param("maxRetryCount") int maxRetryCount);

    /**
     * 批量更新is_upload_mehr状态
     */
    @Modifying
    @Transactional
    @Query("UPDATE ScheduleData s SET s.isUploadMehr = :status, s.retryCount = 0 WHERE s.id IN :ids")
    void batchUpdateUploadStatus(@Param("ids") List<Long> ids, @Param("status") int status);

    /**
     * 批量增加重试次数并更新状态（失败重试）
     */
    @Modifying
    @Transactional
    @Query("UPDATE ScheduleData s SET s.retryCount = s.retryCount + 1, s.isUploadMehr = :status WHERE s.id IN :ids")
    void incrementRetryAndSetStatus(@Param("ids") List<Long> ids, @Param("status") int status);
}