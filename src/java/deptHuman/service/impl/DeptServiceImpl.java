package com.example.attendance.service.impl;

import com.example.attendance.entity.Dept;
import com.example.attendance.mapper.DeptMapper;
import com.example.attendance.service.DeptService;
import com.example.attendance.vo.DeptTreeNodeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DeptServiceImpl implements DeptService {

    @Autowired
    private DeptMapper deptMapper;

    @Override
    public List<DeptTreeNodeVO> getDeptTree() {
        List<Dept> allDepts = deptMapper.selectAll();
        Map<Long, List<Dept>> parentMap = allDepts.stream()
                .filter(d -> d.getParentId() != null)
                .collect(Collectors.groupingBy(Dept::getParentId));
        // 构建树
        return allDepts.stream()
                .filter(d -> d.getParentId() == null)
                .map(root -> buildTreeNode(root, parentMap))
                .collect(Collectors.toList());
    }

    private DeptTreeNodeVO buildTreeNode(Dept dept, Map<Long, List<Dept>> parentMap) {
        DeptTreeNodeVO node = new DeptTreeNodeVO();
        node.setId(dept.getId());
        node.setLabel(dept.getName());
        List<Dept> children = parentMap.getOrDefault(dept.getId(), new ArrayList<>());
        if (!children.isEmpty()) {
            List<DeptTreeNodeVO> childNodes = children.stream()
                    .map(child -> buildTreeNode(child, parentMap))
                    .collect(Collectors.toList());
            node.setChildren(childNodes);
        }
        return node;
    }
}