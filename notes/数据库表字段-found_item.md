# 招领信息表（found_item）字段清单

来源：`lost_found/失物招领系统/lost_found_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 招领ID |
| 2 | title | varchar | 100 | 是 | 否 | 标题 |
| 3 | description | text | — | 否 | 否 | 描述 |
| 4 | category_id | bigint | — | 否 | 否 | 分类ID |
| 5 | found_place | varchar | 255 | 否 | 否 | 拾取地点 |
| 6 | found_time | datetime | — | 否 | 否 | 拾取时间 |
| 7 | contact_name | varchar | 50 | 否 | 否 | 联系人姓名 |
| 8 | contact_phone | varchar | 20 | 否 | 否 | 联系电话 |
| 9 | images | varchar | 500 | 否 | 否 | 图片(多张用逗号分隔) |
| 10 | user_id | bigint | — | 是 | 否 | 发布用户ID |
| 11 | status | int | — | 是 | 否 | 状态(0待认领,1已认领,2已交接,3已关闭,4已过期) |
| 12 | create_time | datetime | — | 是 | 否 | 创建时间 |
| 13 | update_time | datetime | — | 是 | 否 | 更新时间 |
