// ====================== Controller ======================
package com.example.schedule.controller;

import com.example.schedule.dto.ScheduleSubmitRequest;
import com.example.schedule.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 提交排班数据接口
     * @param request 请求参数（包含classDate, shiftName, scheduleData）
     * @return 处理结果
     */
    @PostMapping("/submit")
    public ResponseEntity<String> submitSchedule(@Valid @RequestBody ScheduleSubmitRequest request) {
        try {
            String result = scheduleService.submitSchedule(
                    request.getClassDate(),
                    request.getShiftName(),
                    request.getScheduleData()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // 拦截异常统一返回400
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}