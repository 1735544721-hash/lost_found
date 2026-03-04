# 通知消息表（notification）字段清单

来源：`lost_found/失物招领系统/lost_found_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 通知ID |
| 2 | user_id | bigint | — | 是 | 否 | 接收用户ID |
| 3 | title | varchar | 100 | 是 | 否 | 标题 |
| 4 | content | text | — | 是 | 否 | 内容 |
| 5 | type | tinyint | — | 是 | 否 | 类型(0系统消息,1申请消息,2审核消息) |
| 6 | related_id | bigint | — | 否 | 否 | 关联ID |
| 7 | is_read | tinyint | — | 是 | 否 | 是否已读(0未读,1已读) |
| 8 | create_time | datetime | — | 是 | 否 | 创建时间 |
