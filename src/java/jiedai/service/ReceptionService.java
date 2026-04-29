// ReceptionService.java
package com.example.visitor.service;
import com.example.visitor.dto.CheckinRequestDTO;
import com.example.visitor.dto.CheckoutRequestDTO;
import com.example.visitor.vo.InternalStaffVO;
import com.example.visitor.vo.ProductVO;
import com.example.visitor.vo.ReceptionistVO;
import com.example.visitor.vo.VisitorReceptionVO;
import java.util.List;

public interface ReceptionService {
    List<ProductVO> getProductList();
    ReceptionistVO getReceptionistByEmployeeId(String employeeId);
    InternalStaffVO getInternalStaffByEmployeeId(String employeeId);
    void checkin(CheckinRequestDTO request);
    List<VisitorReceptionVO> getUncheckedVisitorsByProduct(String product);
    void checkout(CheckoutRequestDTO request);
}