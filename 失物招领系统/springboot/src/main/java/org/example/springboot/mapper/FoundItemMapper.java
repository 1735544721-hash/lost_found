package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.FoundItem;

@Mapper
public interface FoundItemMapper extends BaseMapper<FoundItem> {
} 