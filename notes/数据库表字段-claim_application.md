# 认领申请表（claim_application）字段清单

来源：`lost_found/失物招领系统/lost_found_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 申请ID |
| 2 | item_id | bigint | — | 是 | 否 | 物品ID |
| 3 | item_type | tinyint | — | 是 | 否 | 物品类型(0招领信息,1失物信息) |
| 4 | user_id | bigint | — | 是 | 否 | 申请人ID |
| 5 | description | text | — | 否 | 否 | 申请说明 |
| 6 | status | int | — | 是 | 否 | 状态(0待审核,1已通过,2已拒绝,3已取消) |
| 7 | create_time | datetime | — | 是 | 否 | 创建时间 |
| 8 | update_time | datetime | — | 是 | 否 | 更新时间 |
| 9 | audit_user_id | bigint | — | 否 | 否 | 审核人ID |
| 10 | audit_time | datetime | — | 否 | 否 | 审核时间 |
| 11 | audit_remark | varchar | 255 | 否 | 否 | 审核备注 |
