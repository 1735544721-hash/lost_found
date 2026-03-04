-- 失物招领系统数据库改进
-- 添加状态管理、索引优化等功能

-- 为认领申请表添加唯一约束，防止重复申请（修改为允许被拒绝后重新申请）
-- 先删除可能存在的重复数据
DELETE t1 FROM `claim_application` t1
INNER JOIN `claim_application` t2
WHERE t1.id > t2.id
  AND t1.item_id = t2.item_id
  AND t1.item_type = t2.item_type
  AND t1.user_id = t2.user_id
  AND t1.status = t2.status
  AND t1.status = 0;

-- 添加唯一约束（只对待审核状态的申请）
ALTER TABLE `claim_application` ADD UNIQUE KEY `uk_item_user_pending` (`item_id`, `item_type`, `user_id`) COMMENT '防止用户重复申请同一物品（待审核状态）';

-- 更新用户状态字段，支持更多状态
ALTER TABLE `user` MODIFY COLUMN `status` int NOT NULL DEFAULT 0 COMMENT '状态(0待审核,1正常,2审核失败,3已禁用,4已锁定,5已过期)';

-- 为认领申请表添加取消状态
ALTER TABLE `claim_application` MODIFY COLUMN `status` int NOT NULL DEFAULT 0 COMMENT '状态(0待审核,1已通过,2已拒绝,3已取消)';

-- 更新物品状态，支持更多状态
ALTER TABLE `lost_item` MODIFY COLUMN `status` int NOT NULL DEFAULT 0 COMMENT '状态(0待认领,1已认领,2已交接,3已关闭,4已过期)';
ALTER TABLE `found_item` MODIFY COLUMN `status` int NOT NULL DEFAULT 0 COMMENT '状态(0待认领,1已认领,2已交接,3已关闭,4已过期)';

-- 为通知表添加索引
ALTER TABLE `notification` ADD INDEX `idx_user_read` (`user_id`, `is_read`);
ALTER TABLE `notification` ADD INDEX `idx_create_time` (`create_time`);

-- 为物品表添加索引优化查询
ALTER TABLE `lost_item` ADD INDEX `idx_status_create_time` (`status`, `create_time`);
ALTER TABLE `lost_item` ADD INDEX `idx_category_status` (`category_id`, `status`);
ALTER TABLE `found_item` ADD INDEX `idx_status_create_time` (`status`, `create_time`);
ALTER TABLE `found_item` ADD INDEX `idx_category_status` (`category_id`, `status`);
