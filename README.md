# 🏫 校园志愿者管理系统 (Campus Volunteer Management System)

<p align="center">
  <img src="https://img.shields.io/badge/Vue-3.0-4FC08D?logo=vue.js&style=flat-square" alt="Vue 3" />
  <img src="https://img.shields.io/badge/Element%20Plus-2.3-409EFF?logo=element&style=flat-square" alt="Element Plus" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.0-6DB33F?logo=spring&style=flat-square" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&style=flat-square" alt="MySQL" />
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License" />
</p>

> 🚀 一个基于 Vue 3 + Spring Boot 的现代化校园志愿服务管理平台。连接学生、社团组织与学校管理者，实现活动全流程数字化管理。

---

## 📖 项目介绍 | Introduction

本项目旨在解决传统校园志愿活动管理中信息不透明、报名繁琐、工时统计困难等痛点。系统分为 **志愿者（学生）**、**组织者（社团/学院）**、**管理员（校方）** 三端，实现了从活动发布、报名审核、现场签到到工时结算、积分兑换的完整闭环。

✨ **核心亮点**：
- 🌙 **全站日夜间模式切换**：内置炫酷的日月切换动画与 Element Plus 全局暗黑模式适配。
- 🎓 **在线培训学院**：视频课程学习、在线考试考核、自动颁发证书。
- 🎁 **积分激励体系**：志愿时长自动折算积分，支持兑换虚拟/实物奖品。
- 📊 **可视化数据看板**：各角色专属的工作台与数据统计图表。

---

## 🖼️ 系统展示 | Screenshots

> *📷 截图预览 (建议将图片存放在 `docs/images/` 目录下)*

### 1. 首页与日夜模式
| 白天模式 | 黑夜模式 |
| :---: | :---: |
| ![Day Mode](docs/images/home_day.png) | ![Night Mode](docs/images/home_dark.png) |

### 2. 多角色工作台
| 志愿者个人中心 | 组织者管理后台 | 管理员驾驶舱 |
| :---: | :---: | :---: |
| ![Volunteer](docs/images/volunteer_center.png) | ![Organizer](docs/images/organizer_dashboard.png) | ![Admin](docs/images/admin_dashboard.png) |

### 3. 特色功能
| 活动详情页 | 积分商城兑换 | 在线培训考试 |
| :---: | :---: | :---: |
| ![Activity](docs/images/activity_detail.png) | ![Mall](docs/images/points_mall.png) | ![Training](docs/images/training_exam.png) |

---

## 🛠️ 技术栈 | Tech Stack

### 💻 前端 (Frontend)
- **核心框架**: Vue 3 (Composition API) + Vite
- **UI 组件库**: Element Plus (配合自定义主题色)
- **状态管理**: Pinia (持久化存储用户信息/主题配置)
- **路由管理**: Vue Router 4
- **网络请求**: Axios (封装拦截器，处理 Token/权限)
- **动画特效**: CSS3 + Web Components (日夜切换按钮)

### ☕ 后端 (Backend)
- **核心框架**: Spring Boot 3.x
- **ORM 框架**: MyBatis-Plus
- **数据库**: MySQL 8.0
- **缓存/中间件**: Redis (验证码/Token/热点数据)
- **工具库**: Hutool, Lombok, Swagger/Knife4j (接口文档)

---

## 🚀 快速开始 | Quick Start

### 1. 环境准备
请确保本地已安装以下环境：
- Node.js >= 16.0
- JDK >= 17
- MySQL >= 8.0
- Redis

### 2. 数据库配置
1. 创建数据库 `volunteer_db` (字符集 `utf8mb4`).
2. 导入项目根目录下的 `sql/volunteer_db.sql` 脚本文件。
3. 修改后端 `application.yml` 中的数据库连接信息：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/volunteer_db?useSSL=false
       username: root
       password: your_password
3. 启动后端服务
Bash
cd backend
mvn clean install
mvn spring-boot:run
后端服务默认端口: 8080

4. 启动前端服务
Bash
cd frontend
# 安装依赖
npm install 
# 启动开发服务器
npm run dev
访问地址：http://localhost:5173

📦 功能模块概览 | Feature Modules
👨‍🎓 志愿者端 (Student)
账号体系: 注册登录 / 个人信息修改 / 密码重置

活动大厅: 活动筛选 / 报名参加 / 扫码签到 / 评价反馈

培训学院: 课程列表 / 视频学习 / 在线考试 / 成绩查询

积分商城: 商品浏览 / 积分兑换 / 兑换记录

个人中心: 志愿履历 / 工时统计 / 消息通知

🚩 组织者端 (Organizer)
工作台: 待办事项 / 数据概览 / 快捷入口

活动运营: 发布活动 / 报名审核 / 现场管理 (补签)

评价结算: 工时批量录入 / 服务评价回复

通知管理: 定向群发通知 / 历史记录

组织设置: 基本信息 / Logo 上传

🔧 管理员端 (Admin)
用户管理: 志愿者/组织者账号管控 / 资质审核

内容管理: 轮播图配置 / 系统公告发布

商城后台: 商品上下架 / 订单核销

系统监控: 操作日志 / 权限分配

🤝 贡献与支持 | Contribution
欢迎提交 Issue 或 Pull Request 来改进本项目！ 如果你觉得这个项目对你有帮助，请给一个 ⭐️ Star 吧！

Fork 本仓库

新建 Feat_xxx 分支

提交代码

新建 Pull Request

📄 开源协议 | License
本项目采用 MIT 开源协议。
