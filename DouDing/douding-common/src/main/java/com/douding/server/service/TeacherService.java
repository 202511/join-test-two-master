package com.douding.server.service;

import com.douding.server.domain.*;
import com.douding.server.dto.CategoryDto;
import com.douding.server.dto.TeacherDto;
import com.douding.server.dto.PageDto;
import com.douding.server.mapper.TeacherMapper;
import com.douding.server.util.CopyUtil;
import com.douding.server.util.UuidUtil;
import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Service
public class TeacherService {

    @Resource
    private TeacherMapper teacherMapper;


    /**
     * 列表查询
     */
    public void list(PageDto pageDto) {
        PageHelper.startPage(pageDto.getPage(), pageDto.getSize());
        TeacherExample teacherExample = new TeacherExample();
        System.out.println("can can");
        List<Teacher> teacherList = teacherMapper.selectByExample(teacherExample);
        System.out.println("no n o n o");
        PageInfo<Teacher> pageInfo = new PageInfo<>(teacherList);
        pageDto.setTotal(pageInfo.getTotal());
        List<TeacherDto> teacherDtoList = CopyUtil.copyList(teacherList, TeacherDto.class);
        pageDto.setList(teacherDtoList);
    }

    public void save(TeacherDto teacherDto) {
        Teacher copy = CopyUtil.copy(teacherDto, Teacher.class);
        if (findById(teacherDto.getId())!=null) {
            update(copy);
        }
        else
        {
             insert(copy);
        }

    }

    //新增数据
    private void insert(Teacher teacher) {
        teacher.setId(UuidUtil.getShortUuid());
          teacherMapper.insert(teacher);

    }

    //更新数据
    private void update(Teacher teacher) {

        teacherMapper.updateByPrimaryKey(teacher);
    }

    public void delete(String id) {
        if (findById(id)!=null) {
            teacherMapper.deleteByPrimaryKey(id);
        }
    }

    public List<TeacherDto> all() {
       return null;
    }


    /**
     * 查找
     * @param id
     */
    public TeacherDto findById(String id) {
        Teacher teacher = teacherMapper.selectByPrimaryKey(id);
        TeacherDto copy = CopyUtil.copy(teacher, TeacherDto.class);
        return copy;
    }
}//end class
