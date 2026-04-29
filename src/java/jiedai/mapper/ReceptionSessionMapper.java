// ReceptionSessionMapper.java
package com.example.visitor.mapper;
import com.example.visitor.entity.ReceptionSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReceptionSessionMapper {
    void insert(ReceptionSession session);
    // 其他方法如按产品查询会话等可通过 VisitorReceptionMapper 关联
}