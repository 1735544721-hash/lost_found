# 用户表（user）字段清单

来源：`lost_found/失物招领系统/lost_found_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 用户ID |
| 2 | username | varchar | 50 | 是 | 否 | 用户名 |
| 3 | password | varchar | 100 | 是 | 否 | 密码 |
| 4 | name | varchar | 100 | 否 | 否 | 真实姓名 |
| 5 | phone | varchar | 20 | 否 | 否 | 手机号 |
| 6 | email | varchar | 100 | 否 | 否 | 邮箱 |
| 7 | avatar | varchar | 255 | 否 | 否 | 头像 |
| 8 | role_code | varchar | 20 | 是 | 否 | 角色编码(ADMIN/USER) |
| 9 | create_time | datetime | — | 是 | 否 | 创建时间 |
| 10 | update_time | datetime | — | 是 | 否 | 更新时间 |
| 11 | status | int | — | 是 | 否 | 状态(0待审核,1正常,2审核失败,3已禁用,4已锁定,5已过期) |
| 12 | sex | varchar | 255 | 否 | 否 | 性别 |
