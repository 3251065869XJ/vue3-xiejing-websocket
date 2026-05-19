package com.example.attendance.mapper;

import com.example.attendance.entity.Dept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DeptMapper {
    List<Dept> selectAll();
    Dept selectById(@Param("id") Long id);
    List<Dept> selectByParentId(@Param("parentId") Long parentId);
}