// ReceptionistMapper.java
package com.example.visitor.mapper;
import com.example.visitor.entity.Receptionist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ReceptionistMapper {
    Receptionist findByEmployeeId(@Param("employeeId") String employeeId);
    List<String> findProductsByReceptionistId(@Param("receptionistId") Integer receptionistId);
}