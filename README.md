# 🌟 校园志愿服务管理系统 (Campus Volunteer System)

![Vue.js](https://img.shields.io/badge/Vue%203-4FC08D?style=for-the-badge&logo=vuedotjs&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Element Plus](https://img.shields.io/badge/Element%20Plus-409EFF?style=for-the-badge&logo=elementplus&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)

本项目是一个以**用户体验**和**高互动性**为核心驱动的现代化校园志愿服务闭环生态系统。系统打破了传统后台管理界面的死板设计，采用真正的 **“一套代码，双端（PC+Mobile）差异化渲染”** 架构，并融入大量高级 UI 质感与微交互动效，为师生提供媲美原生 App 和现代 SaaS 的极致体验。

---

## ✨ 核心创新点与亮点 (Innovations & Highlights)

### 🎨 1. 极致的视觉与交互美学 (UI/UX)
- **Telegram Premium 级高定质感**：大面积运用拟玻璃化（Glassmorphism），底部 TabBar 采用悬浮药丸设计，配合滚动液态毛玻璃模糊（Liquid Glass Overlay），视觉极其通透。
- **GSAP 情感化交互吉祥物**：登录与表单页内置动态 SVG 吉祥物。当用户输入账号时，吉祥物眼球会跟随光标转动；输入密码时，拥有 Q 弹的“捂眼/偷看”连贯动画。
- **纯 CSS 拟物风昼夜切换**：重写了主题切换按钮，点击时不仅有顺滑的滑动，内部还会触发日月星辰视差沉降的微缩场景动效。
- **贝塞尔物理级抽奖转盘**：纯 CSS 绘制的积分大转盘，基于 `cubic-bezier` 实现先加速后缓停的真实物理阻尼减速效果。

### 💻 2. 物理级双端适配架构 (App-Shell & SaaS Layout)
有别于简单的 `@media` CSS 媒体查询，本项目在 DOM 层面进行了深度拦截与渲染隔离：
- **📱 移动端 (App Shell)**：渲染为原生 App 体验，拥有顶部沉浸式 Header、底部悬浮导航栏，使用 `Push/Pop` 和 `Tab-fade` 原生级页面转场动效。列表卡片（如活动列表）自动重构为左图右文结构，适合单手滑动浏览。
- **🖥️ 电脑端 (SaaS Layout)**：渲染为现代化宽屏 SaaS 面板，拥有全局 Sticky 导航、悬浮毛玻璃菜单、流式栅格布局。列表卡片自动转为上图下文的悬浮交互卡片。

### ⚙️ 3. 硬核后端与安全设计
- **Aho-Corasick 算法级敏感词过滤**：利用前缀树实现社区心得、评论、反馈的毫秒级敏感词检测拦截（`@CheckSensitive` 结合 `SensitiveWordFilter`），保障校园社区内容健康。
- **AOP 全局操作溯源**：使用 Spring AOP (`OperationLogAspect`) 无侵入式切面拦截，精准记录核心 API 的调用日志（IP、耗时、操作人），保障系统数据安全。

---

## 🧩 详尽功能模块全景 (Functional Modules)

系统按角色权限严格划分为三大工作台，形成完整业务闭环：

### 🧑‍🎓 1. 志愿者端 (C端 - 面向全校师生)
- **⛺ 活动中心**：支持按分类、时间、状态的瀑布流活动大厅，展示实时报名进度条；一键报名、取消报名、**基于位置/二维码的现场扫码签到与签退**。
- **🎓 培训学院**：部分高门槛活动的先决条件。提供在线图文/视频课程学习，学完后可参与**在线考试答题**，系统自动判卷并记录成绩。
- **🛍️ 积分商城**：
  - **每日打卡**：签到与参与活动获取志愿积分。
  - **物资兑换**：消耗积分兑换文创周边或志愿服务时长，支持查看“我的背包”与兑换记录。
- **💬 心得广场 (社区)**：发布志愿服务动态、照片，支持社区内的**点赞、收藏、多级嵌套评论**，构建校内专属公益社交圈。
- **📊 个人数据大盘**：可视化展示个人累计服务时长、信用评分、积分明细、**电子荣誉证书**及完整的活动履历。

### 🏢 2. 组织者端 (B端 - 面向社团/院系青协)
- **📈 组织者看板**：统计本组织发起的活动总数、招募总人次、累计发放时长等核心运营数据。
- **📝 活动生命周期管理**：发起活动申请（设定招募人数、限制条件、预估工时）、提交管理员审核、上线招募、跟进状态流转。
- **👥 考勤与人员管理**：对报名人员进行资质审核（通过/拒绝）；活动现场执行**手机端扫码核销**，管理实到/缺勤人员名单。
- **💰 工时与结算系统**：活动圆满结束后，项目负责人为到场人员**统一录入实际服务时长**，系统自动结算并发放时长与积分奖励。
- **📢 定向通知下发**：向特定活动的已报名成员精准推送站内信通知（如活动改期、集合地点变更等）。

### 🛡️ 3. 系统管理员端 (Admin端 - 面向校团委总控)
- **🌍 全局大屏总览**：多维数据统计可视化大屏，包含全校志愿服务总时长、各学院/组织活跃度排行、实时系统日活数据。
- **⚖️ 审核治理中心**：
  - **活动审核**：严格把控全校活动质量，违规活动一键驳回或下架。
  - **资质审批**：审核新注册的组织者（B端）账号，分配发活权限。
  - **内容安全**：管理被敏感词系统拦截的违规内容，处理用户的问题反馈（Feedback）。
- **⚙️ 商城与系统运营**：
  - **商品管理**：积分商城礼品上下架、库存动态调整、处理用户兑换订单并发货。
  - **全局配置**：动态配置首页 Banner 轮播图、发布全局系统公告、灵活调整积分兑换比例与签到奖励数值。
  - **日志审计**：查看完整的系统操作日志与登录日志，排查异常接口调用行为。

---

## 🛠️ 技术架构体系 (Tech Stack)

### 前端生态 (Frontend)
- **核心框架**：Vue 3 (Composition API / Setup 语法糖)
- **类型系统**：TypeScript
- **UI 组件库**：Element Plus (深度覆盖 SCSS 变量，重构卡片、弹窗与按钮质感)
- **状态管理**：Pinia
- **路由控制**：Vue Router 4
- **动画引擎**：GSAP (复杂 SVG 连贯动画)
- **样式预处理器**：Sass / SCSS

### 后端生态 (Backend)
- **核心框架**：Spring Boot 3.x
- **数据持久化**：MyBatis Plus
- **安全与权限**：Spring Security + JWT (JSON Web Token) 无状态鉴权机制
- **缓存引擎**：Redis (提速首页大盘查询、接口防刷、缓存敏感词前缀树)
- **数据库**：MySQL 8.0
- **任务调度**：Spring Task / Quartz (活动状态定时扭转)
- **API 文档**：Knife4j (Swagger 增强版)

---

## 📂 核心目录结构

```text
├── src/                          # 前端 Vue 3 代码
│   ├── components/               # 核心复用组件 (ActivityCard, LuckyWheel, 吉祥物等)
│   ├── composables/              # 组合式函数 (useMobile 响应式内核等)
│   ├── layouts/                  # 骨架布局 (MainLayout - DOM双端拆分逻辑核心)
│   ├── views/                    # 视图页面
│   │   ├── admin/                # Admin 系统管理端
│   │   ├── organizer/            # B端 组织者工作台
│   │   ├── activity/             # C端 活动大厅
│   │   ├── mall/                 # C端 积分商城与抽奖
│   │   ├── training/             # C端 在线培训与考试
│   │   └── experience/           # C端 社区心得广场
│   └── styles/                   # 全局样式系统 (index.scss 包含大量质感重写)
│
├── volunteer-backend/            # 后端 Spring Boot 代码
│   ├── aspect/                   # AOP 切面 (操作日志抓取、敏感词拦截)
│   ├── controller/               # API 控制层 (严格按 admin/org/user 划定接口界限)
│   ├── entity/                   # 数据库实体类
│   ├── mapper/                   # MyBatis Plus Mapper 接口
│   ├── security/                 # JWT 令牌校验与安全上下文
│   ├── service/                  # 业务逻辑实现层
│   └── util/                     # 工具类 (FileUploadValidator, SensitiveWordFilter 等)
🚀 快速启动 (Quick Start)
1. 环境准备
Node.js >= 18.x
JDK >= 17
MySQL >= 8.0
Redis >= 6.0
2. 后端服务启动
创建 MySQL 数据库 volunteer_db。
运行后端 src/main/resources/ 目录下的 SQL 初始化脚本导入表结构。
修改 application.yml 中的 MySQL 和 Redis 连接配置（账号、密码）。
运行 VolunteerApplication.java 启动服务（默认运行在 8080 端口）。
3. 前端项目运行
bash
运行
# 进入前端目录
cd src

# 安装依赖
npm install

# 启动本地开发服务器
npm run dev
启动后，访问 http://localhost:5173 即可体验系统。
💡 提示：强烈建议在浏览器按 F12 切换至移动端设备模式（Mobile View），然后刷新页面，体验专为移动端打造的沉浸式 App-Shell 布局与交互！
📄 许可证 (License)
本项目基于 MIT License 开源。欢迎点亮 ⭐️ Star，提交 PR 或 Issue 交流探讨！