// ReceptionServiceImpl.java
package com.example.visitor.service.impl;
import com.example.visitor.dto.CheckinRequestDTO;
import com.example.visitor.dto.CheckoutRequestDTO;
import com.example.visitor.dto.VisitorItemDTO;
import com.example.visitor.entity.*;
import com.example.visitor.mapper.*;
import com.example.visitor.service.ReceptionService;
import com.example.visitor.vo.InternalStaffVO;
import com.example.visitor.vo.ProductVO;
import com.example.visitor.vo.ReceptionistVO;
import com.example.visitor.vo.VisitorReceptionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReceptionServiceImpl implements ReceptionService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ReceptionistMapper receptionistMapper;
    @Autowired
    private InternalStaffMapper internalStaffMapper;
    @Autowired
    private ReceptionSessionMapper receptionSessionMapper;
    @Autowired
    private VisitorReceptionMapper visitorReceptionMapper;

    @Override
    public List<ProductVO> getProductList() {
        List<Product> products = productMapper.findAll();
        return products.stream().map(p -> {
            ProductVO vo = new ProductVO();
            vo.setName(p.getName());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public ReceptionistVO getReceptionistByEmployeeId(String employeeId) {
        Receptionist receptionist = receptionistMapper.findByEmployeeId(employeeId);
        if (receptionist == null) return null;
        ReceptionistVO vo = new ReceptionistVO();
        BeanUtils.copyProperties(receptionist, vo);
        return vo;
    }

    @Override
    public InternalStaffVO getInternalStaffByEmployeeId(String employeeId) {
        InternalStaff staff = internalStaffMapper.findByEmployeeId(employeeId);
        if (staff == null) return null;
        InternalStaffVO vo = new InternalStaffVO();
        BeanUtils.copyProperties(staff, vo);
        return vo;
    }

    @Override
    @Transactional
    public void checkin(CheckinRequestDTO request) {
        // 1. 校验接待人是否存在并验证产品权限
        Receptionist receptionist = receptionistMapper.findByEmployeeId(request.getReceptionistEmployeeId());
        if (receptionist == null) {
            throw new RuntimeException("接待人工号不存在");
        }
        List<String> allowedProducts = receptionistMapper.findProductsByReceptionistId(receptionist.getId());
        if (!allowedProducts.contains(request.getProduct())) {
            throw new RuntimeException("该接待人没有所选产品的接待权限");
        }

        // 2. 创建接待会话
        ReceptionSession session = new ReceptionSession();
        session.setReceptionistId(receptionist.getId());
        session.setProduct(request.getProduct());
        session.setReceptionTime(LocalDateTime.now());
        session.setCreateTime(LocalDateTime.now());
        receptionSessionMapper.insert(session);

        // 3. 批量插入访客记录
        for (VisitorItemDTO visitor : request.getVisitors()) {
            VisitorReception vr = new VisitorReception();
            vr.setSessionId(session.getId());
            vr.setVisitorName(visitor.getName());
            vr.setCheckoutStatus(0); // 未签离
            vr.setCreateTime(LocalDateTime.now());

            if ("internal".equals(visitor.getType())) {
                vr.setVisitorType(0);
                vr.setEmployeeId(visitor.getEmployeeId());
                vr.setDepartment(visitor.getDepartment());
            } else {
                vr.setVisitorType(1);
                vr.setCompany(visitor.getCompany());
                vr.setIdNumber(visitor.getIdNumber());
            }
            visitorReceptionMapper.insert(vr);
        }
    }

    @Override
    public List<VisitorReceptionVO> getUncheckedVisitorsByProduct(String product) {
        return visitorReceptionMapper.findUncheckedByProduct(product);
    }

    @Override
    @Transactional
    public void checkout(CheckoutRequestDTO request) {
        VisitorReception vr = visitorReceptionMapper.selectById(request.getVisitorReceptionId());
        if (vr == null) {
            throw new RuntimeException("访客记录不存在");
        }
        if (vr.getCheckoutStatus() == 1) {
            throw new RuntimeException("该访客已经签离");
        }

        boolean match = false;
        if (vr.getVisitorType() == 0) { // 外部门员工
            if (vr.getEmployeeId() != null && vr.getEmployeeId().equals(request.getVerifyCode())) {
                match = true;
            }
        } else { // 外公司人员
            if (vr.getIdNumber() != null && vr.getIdNumber().equals(request.getVerifyCode())) {
                match = true;
            }
        }

        if (!match) {
            throw new RuntimeException("验证失败：工号或身份证号不匹配");
        }

        // 更新签离状态
        visitorReceptionMapper.updateCheckoutStatus(vr.getId(), LocalDateTime.now());
    }
}