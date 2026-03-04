package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.ClaimApplication;

/**
 * 认领申请Mapper接口
 */
@Mapper
public interface ClaimApplicationMapper extends BaseMapper<ClaimApplication> {
} 