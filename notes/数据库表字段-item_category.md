# 物品分类表（item_category）字段清单

来源：`lost_found/失物招领系统/lost_found_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 分类ID |
| 2 | name | varchar | 50 | 是 | 否 | 分类名称 |
| 3 | sort | int | — | 否 | 否 | 排序号 |
| 4 | create_time | datetime | — | 是 | 否 | 创建时间 |
| 5 | update_time | datetime | — | 是 | 否 | 更新时间 |
