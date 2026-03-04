# lost_found

失物招领系统相关材料与代码。

## 目录结构
- `失物招领系统/`：项目源码与数据库脚本
- `notes/`：数据库表与字段说明、写作素材
- `openclaw必读资料/`：写作与材料参考

## 快速开始（后端）
- 初始化数据库并导入 `失物招领系统/lost_found_db.sql`
- 按需更新 `失物招领系统/springboot/src/main/resources/application.properties`
- 进入 `失物招领系统/springboot`，运行 `./mvnw spring-boot:run`
- 接口文档：`http://localhost:1235/swagger-ui.html`
- 详细步骤见 `notes/运行清单.md`

## 数据库文档
- 表清单：`notes/数据库表清单.md`
- 字段说明索引：`notes/README.md`
