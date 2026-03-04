package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.Notification;

/**
 * 通知消息Mapper接口
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
