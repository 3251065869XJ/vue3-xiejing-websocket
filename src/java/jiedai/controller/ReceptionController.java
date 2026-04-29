// ReceptionController.java
package com.example.visitor.controller;
import com.example.visitor.dto.CheckinRequestDTO;
import com.example.visitor.dto.CheckoutRequestDTO;
import com.example.visitor.service.ReceptionService;
import com.example.visitor.vo.InternalStaffVO;
import com.example.visitor.vo.ProductVO;
import com.example.visitor.vo.ReceptionistVO;
import com.example.visitor.vo.VisitorReceptionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // 允许跨域
public class ReceptionController {

    @Autowired
    private ReceptionService receptionService;

    // 获取产品列表
    @GetMapping("/products")
    public ResponseEntity<?> getProducts() {
        List<ProductVO> list = receptionService.getProductList();
        return ResponseEntity.ok(list);
    }

    // 根据工号查询接待人信息
    @GetMapping("/receptionist/{employeeId}")
    public ResponseEntity<?> getReceptionist(@PathVariable String employeeId) {
        ReceptionistVO vo = receptionService.getReceptionistByEmployeeId(employeeId);
        if (vo == null) {
            return ResponseEntity.status(404).body("接待人工号不存在");
        }
        return ResponseEntity.ok(vo);
    }

    // 根据工号查询外部门员工信息
    @GetMapping("/internalStaff/{employeeId}")
    public ResponseEntity<?> getInternalStaff(@PathVariable String employeeId) {
        InternalStaffVO vo = receptionService.getInternalStaffByEmployeeId(employeeId);
        if (vo == null) {
            return ResponseEntity.status(404).body("外部门员工不存在");
        }
        return ResponseEntity.ok(vo);
    }

    // 提交接待
    @PostMapping("/reception/checkin")
    public ResponseEntity<?> checkin(@RequestBody CheckinRequestDTO request) {
        try {
            receptionService.checkin(request);
            return ResponseEntity.ok("接待成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 获取指定产品下未签离的访客列表
    @GetMapping("/reception/checkout/list")
    public ResponseEntity<?> getUncheckedVisitors(@RequestParam String product) {
        List<VisitorReceptionVO> list = receptionService.getUncheckedVisitorsByProduct(product);
        return ResponseEntity.ok(list);
    }

    // 签离确认
    @PostMapping("/reception/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequestDTO request) {
        try {
            receptionService.checkout(request);
            return ResponseEntity.ok("签离成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}