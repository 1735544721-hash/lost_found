package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.ItemCategory;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.ItemCategoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemCategoryService {
    @Resource
    private ItemCategoryMapper itemCategoryMapper;

    /**
     * 添加分类
     */
    public void addCategory(ItemCategory category) {
        // 检查分类名是否已存在
        if (categoryNameExists(category.getName())) {
            throw new ServiceException("分类名称已存在");
        }
        
        if (itemCategoryMapper.insert(category) <= 0) {
            throw new ServiceException("添加分类失败");
        }
    }

    /**
     * 更新分类
     */
    public void updateCategory(Long id, ItemCategory category) {
        ItemCategory existCategory = getCategoryById(id);
        
        // 如果修改了分类名，则检查是否已存在
        if (!existCategory.getName().equals(category.getName()) && categoryNameExists(category.getName())) {
            throw new ServiceException("分类名称已存在");
        }
        
        category.setId(id);
        if (itemCategoryMapper.updateById(category) <= 0) {
            throw new ServiceException("更新分类失败");
        }
    }

    /**
     * 删除分类
     */
    public void deleteCategory(Long id) {
        if (itemCategoryMapper.deleteById(id) <= 0) {
            throw new ServiceException("删除分类失败");
        }
    }

    /**
     * 根据ID获取分类
     */
    public ItemCategory getCategoryById(Long id) {
        ItemCategory category = itemCategoryMapper.selectById(id);
        if (category == null) {
            throw new ServiceException("分类不存在");
        }
        return category;
    }

    /**
     * 分页查询分类
     */
    public Page<ItemCategory> getCategoryByPage(String name, Integer currentPage, Integer size) {
        LambdaQueryWrapper<ItemCategory> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like(ItemCategory::getName, name);
        }
        
        // 按排序号升序、创建时间降序排列
        queryWrapper.orderByAsc(ItemCategory::getSort)
                   .orderByDesc(ItemCategory::getCreateTime);
        
        return itemCategoryMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
    }

    /**
     * 获取所有分类列表
     */
    public List<ItemCategory> getAllCategories() {
        // 按排序号升序、创建时间降序排列
        LambdaQueryWrapper<ItemCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(ItemCategory::getSort)
                   .orderByDesc(ItemCategory::getCreateTime);
        
        return itemCategoryMapper.selectList(queryWrapper);
    }

    /**
     * 检查分类名是否存在
     */
    private boolean categoryNameExists(String name) {
        LambdaQueryWrapper<ItemCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ItemCategory::getName, name);
        return itemCategoryMapper.selectCount(queryWrapper) > 0;
    }
} 