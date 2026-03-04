package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.springboot.entity.ItemCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ItemCategoryMapper extends BaseMapper<ItemCategory> {
} 