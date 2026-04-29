// InternalStaffMapper.java
package com.example.visitor.mapper;
import com.example.visitor.entity.InternalStaff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InternalStaffMapper {
    InternalStaff findByEmployeeId(@Param("employeeId") String employeeId);
}