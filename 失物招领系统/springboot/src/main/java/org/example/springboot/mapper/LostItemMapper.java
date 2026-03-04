package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.springboot.entity.LostItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LostItemMapper extends BaseMapper<LostItem> {
} 