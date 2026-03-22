import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { ElMessage } from 'element-plus'

// 路由配置
const routes: RouteRecordRaw[] = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/LoginPage.vue'),
        meta: { title: '登录', requiresAuth: false }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/login/RegisterPage.vue'),
        meta: { title: '注册', requiresAuth: false }
    },
    {
        path: '/',
        name: 'Layout',
        component: () => import('@/layouts/MainLayout.vue'),
        redirect: '/home',
        children: [
            {
                path: 'home',
                name: 'Home',
                component: () => import('@/views/home/HomePage.vue'),
                meta: { title: '首页', requiresAuth: false, roles: ['VOLUNTEER'] }
            },

            // ================== 新增：通知公告 ==================
            {
                path: 'notice',
                name: 'Notice',
                meta: { title: '通知公告' },
                children: [
                    {
                        path: 'list',
                        name: 'NoticeList',
                        component: () => import('@/views/notice/NoticeList.vue'),
                        meta: { title: '公告列表', requiresAuth: false, roles: ['VOLUNTEER'] }
                    },
                    {
                        path: 'my',
                        name: 'MessageCenter',
                        component: () => import('@/views/notice/MessageCenter.vue'),
                        meta: { title: '我的消息', requiresAuth: true, roles: ['VOLUNTEER'] }
                    },
                    {
                        path: 'detail/:id',
                        name: 'NoticeDetail',
                        component: () => import('@/views/notice/NoticeDetail.vue'),
                        meta: { title: '公告详情', requiresAuth: false, roles: ['VOLUNTEER'], hideTabBar: true }
                    }
                ]
            },

            // ================== 新增：积分商城 ==================
            {
                path: 'mall',
                name: 'Mall',
                meta: { title: '积分商城' },
                redirect: '/mall/index', // Fix: Redirect to index
                children: [
                    {
                        path: 'index',
                        name: 'MallIndex',
                        component: () => import('@/views/mall/MallIndex.vue'),
                        meta: { title: '商城首页', requiresAuth: false, roles: ['VOLUNTEER'] }
                    },
                    {
                        path: 'backpack',
                        name: 'Backpack',
                        component: () => import('@/views/mall/Backpack.vue'),
                        meta: { title: '我的背包', requiresAuth: true, roles: ['VOLUNTEER'] }
                    },

                    {
                        path: 'checkin',
                        name: 'CheckIn',
                        component: () => import('@/views/mall/CheckIn.vue'),
                        meta: { title: '每日签到', requiresAuth: false, roles: ['VOLUNTEER'] }
                    },
                    {
                        path: 'item/:id',
                        name: 'MallDetail',
                        component: () => import('@/views/mall/MallDetail.vue'),
                        meta: { title: '商品详情', requiresAuth: false, roles: ['VOLUNTEER'], hideTabBar: true, hideHeader: true }
                    }
                ]
            },

            // ================== 扫一扫（独立路由） ==================
            {
                path: 'scan',
                name: 'Scan',
                component: () => import('@/views/scan/ScanView.vue'),
                meta: { title: '扫一扫', hideMenu: true, requiresAuth: true, roles: ['VOLUNTEER'] }
            },

            // ================== 新增：活动中心 ==================
            {
                path: 'activity',
                name: 'Activity',
                meta: { title: '活动中心' },
                children: [
                    {
                        path: '',
                        name: 'ActivityList',
                        component: () => import('@/views/activity/ActivityList.vue'),
                        meta: { title: '活动列表', requiresAuth: false, roles: ['VOLUNTEER'] }
                    },
                    {
                        path: ':id',
                        name: 'ActivityDetail',
                        component: () => import('@/views/activity/ActivityDetail.vue'),
                        meta: { title: '活动详情', requiresAuth: false, roles: ['VOLUNTEER'], hideTabBar: true, hideHeader: true }
                    }
                ]
            },

            // ================== 新增：心得分享 ==================
            {
                path: 'experience',
                name: 'Experience',
                meta: { title: '心得分享' },
                children: [
                    {
                        path: '',
                        name: 'ExperienceList',
                        component: () => import('@/views/experience/ExperienceList.vue'),
                        meta: { title: '心得列表', requiresAuth: false, roles: ['VOLUNTEER'] }
                    },
                    {
                        path: 'plaza',
                        name: 'ExperiencePlaza',
                        component: () => import('@/views/experience/ExperiencePlaza.vue'),
                        meta: { title: '心得广场', requiresAuth: false, roles: ['VOLUNTEER'] }
                    },
                    {
                        path: 'create',
                        name: 'ExperienceCreate',
                        component: () => import('@/views/experience/ExperiencePublish.vue'),
                        meta: { title: '发布心得', requiresAuth: true, roles: ['VOLUNTEER'] }
                    },
                    {
                        path: 'edit/:id',
                        name: 'ExperienceEditor',
                        component: () => import('@/views/experience/ExperiencePublish.vue'),
                        meta: { title: '编辑心得', requiresAuth: true, roles: ['VOLUNTEER'] }
                    },
                    {
                        path: ':id',
                        name: 'ExperienceDetail',
                        component: () => import('@/views/experience/ExperienceDetail.vue'),
                        meta: { title: '心得详情', requiresAuth: false, roles: ['VOLUNTEER'], hideTabBar: true }
                    }
                ]
            },

            // ================== 新增：问题反馈 ==================
            {
                path: 'feedback',
                name: 'Feedback',
                component: () => import('@/views/feedback/FeedbackPage.vue'),
                meta: { title: '问题反馈', requiresAuth: true, roles: ['VOLUNTEER'] }
            },

            // ================== 新增：培训学院 ==================
            {
                path: 'training',
                name: 'Training',
                meta: { title: '培训学院' },
                redirect: '/training/index',
                children: [
                    {
                        path: 'index',
                        name: 'TrainingIndex',
                        component: () => import('@/views/training/TrainingIndex.vue'),
                        meta: { title: '学院首页', requiresAuth: false, roles: ['VOLUNTEER'] }
                    },
                    {
                        path: 'list',
                        name: 'CourseList',
                        component: () => import('@/views/training/CourseList.vue'),
                        meta: { title: '课程列表', requiresAuth: false, roles: ['VOLUNTEER'] }
                    },
                    {
                        path: 'detail/:id',
                        name: 'CourseDetail',
                        component: () => import('@/views/training/CourseDetail.vue'),
                        meta: { title: '课程详情', requiresAuth: false, roles: ['VOLUNTEER'], hideTabBar: true }
                    },
                    {
                        path: 'my',
                        name: 'MyExam',
                        component: () => import('@/views/training/MyExam.vue'),
                        meta: { title: '我的考试', requiresAuth: true, roles: ['VOLUNTEER'] }
                    }
                ]
            },


            {
                path: 'profile',
                meta: { title: '个人中心', requiresAuth: true, roles: ['VOLUNTEER'] },
                children: [
                    {
                        path: '',
                        name: 'Profile',
                        component: () => import('@/views/profile/ProfilePage.vue'),
                        meta: { title: '我的资料', roles: ['VOLUNTEER'] }
                    },
                    {
                        path: 'info',
                        name: 'ProfileInfo',
                        component: () => import('@/views/profile/ProfilePage.vue'),
                        meta: { title: '我的资料', roles: ['VOLUNTEER'] }
                    },
                    {
                        path: 'history',
                        name: 'ProfileHistory',
                        component: () => import('@/views/profile/VolunteerRecord.vue'),
                        meta: { title: '志愿记录', roles: ['VOLUNTEER'] }
                    },
                    {
                        path: 'favorite',
                        name: 'ProfileFavorite',
                        component: () => import('@/views/profile/MyFavorite.vue'),
                        meta: { title: '我的收藏', roles: ['VOLUNTEER'] }
                    },
                    {
                        path: 'honor',
                        name: 'ProfileHonor',
                        component: () => import('@/views/profile/MyHonor.vue'),
                        meta: { title: '荣誉证书', roles: ['VOLUNTEER'] }
                    },
                    {
                        path: 'stats',
                        name: 'ProfileStats',
                        component: () => import('@/views/profile/DataCenter.vue'),
                        meta: { title: '数据中心', roles: ['VOLUNTEER'] }
                    },
                    {
                        path: 'reviews',
                        name: 'ProfileReviews',
                        component: () => import('@/views/profile/MyReviews.vue'),
                        meta: { title: '我的评价', roles: ['VOLUNTEER'] }
                    }
                ]
            },

            {
                path: 'my-activities',
                name: 'MyActivities',
                component: () => import('@/views/profile/MyActivities.vue'),
                meta: { title: '我的活动', requiresAuth: true, roles: ['VOLUNTEER'] }
            },
            {
                path: 'user/volunteer-record',
                name: 'VolunteerRecord',
                component: () => import('@/views/profile/VolunteerRecord.vue'),
                meta: { title: '志愿记录', requiresAuth: true, roles: ['VOLUNTEER'] }
            },
            {
                path: 'my-experiences',
                name: 'MyExperiences',
                component: () => import('@/views/profile/MyExperiences.vue'),
                meta: { title: '我的心得', requiresAuth: true, roles: ['VOLUNTEER'] }
            },

            // 组织者管理路由 (模块化重构)
            {
                path: 'organizer',
                name: 'Organizer',
                meta: { title: '组织者工作台', requiresAuth: true, roles: ['ORGANIZER'] },
                redirect: '/organizer/dashboard',
                children: [
                    // 1. 工作台
                    {
                        path: 'dashboard',
                        name: 'OrgDashboard',
                        component: () => import('@/views/organizer/Dashboard.vue'),
                        meta: { title: '工作台首页' }
                    },
                    // 2. 活动管理
                    {
                        path: 'activity/create',
                        name: 'OrgActivityCreate',
                        component: () => import('@/views/organizer/ActivityEditor.vue'),
                        meta: { title: '发布活动' }
                    },
                    {
                        path: 'activity/list',
                        name: 'OrgActivityList',
                        component: () => import('@/views/organizer/activity/ActivityList.vue'),
                        meta: { title: '活动列表' }
                    },
                    {
                        path: 'activity/edit/:id',
                        name: 'OrgActivityEdit',
                        component: () => import('@/views/organizer/ActivityEditor.vue'),
                        meta: { title: '编辑活动' }
                    },
                    // 3. 人员管理
                    {
                        path: 'personnel/audit',
                        name: 'OrgPersonnelAudit',
                        component: () => import('@/views/organizer/personnel/PersonnelAudit.vue'),
                        meta: { title: '报名审核' }
                    },
                    {
                        path: 'personnel/checkin',
                        name: 'OrgPersonnelCheckin',
                        component: () => import('@/views/organizer/personnel/PersonnelCheckin.vue'),
                        meta: { title: '现场签到' }
                    },
                    // 4. 通知管理
                    {
                        path: 'notification/send',
                        name: 'OrgNotificationSend',
                        component: () => import('@/views/organizer/notification/NotificationSend.vue'),
                        meta: { title: '发送通知' }
                    },
                    {
                        path: 'notification/history',
                        name: 'OrgNotificationHistory',
                        component: () => import('@/views/organizer/notification/NotificationHistory.vue'),
                        meta: { title: '通知历史' }
                    },
                    // 5. 结算与评价
                    {
                        path: 'feedback/hours',
                        name: 'OrgHoursEntry',
                        component: () => import('@/views/organizer/feedback/HoursEntry.vue'),
                        meta: { title: '工时录入' }
                    },
                    {
                        path: 'feedback/reviews',
                        name: 'OrgServiceReviews',
                        component: () => import('@/views/organizer/feedback/ServiceReviews.vue'),
                        meta: { title: '服务评价' }
                    },
                    // 6. 组织设置
                    {
                        path: 'setting',
                        name: 'OrgSetting',
                        component: () => import('@/views/organizer/OrgSetting.vue'),
                        meta: { title: '组织设置' }
                    }
                ]
            },

            // 管理员路由
            // 管理员后台路由 (模块化重构)
            {
                path: 'admin',
                name: 'Admin',
                meta: { title: '管理后台', requiresAuth: true, roles: ['ADMIN'] },
                redirect: '/admin/dashboard/overview',
                children: [
                    // 1. 数据看板
                    {
                        path: 'dashboard/overview',
                        name: 'AdminOverview',
                        component: () => import('@/views/admin/dashboard/Overview.vue'),
                        meta: { title: '首页概览' }
                    },
                    {
                        path: 'profile',
                        name: 'AdminProfile',
                        component: () => import('@/views/admin/setting/AdminProfile.vue'),
                        meta: { title: '个人资料' }
                    },
                    {
                        path: 'dashboard/statistics',
                        name: 'AdminStatistics',
                        component: () => import('@/views/admin/DataStatistics.vue'),
                        meta: { title: '数据统计' }
                    },
                    // 2. 用户中心
                    {
                        path: 'user/volunteer',
                        name: 'AdminVolunteer',
                        component: () => import('@/views/admin/UserManagement.vue'),
                        meta: { title: '志愿者管理' }
                    },
                    {
                        path: 'user/organizer',
                        name: 'AdminOrganizer',
                        component: () => import('@/views/admin/user/OrganizerMgmt.vue'),
                        meta: { title: '组织者管理' }
                    },
                    // 3. 活动运营
                    {
                        path: 'activity/audit',
                        name: 'AdminActivityAudit',
                        component: () => import('@/views/admin/ActivityAudit.vue'),
                        meta: { title: '活动审核' }
                    },
                    {
                        path: 'activity/list',
                        name: 'AdminActivityList',
                        component: () => import('@/views/admin/activity/ActivityList.vue'),
                        meta: { title: '活动管理' }
                    },
                    {
                        path: 'activity/category',
                        name: 'AdminCategory',
                        component: () => import('@/views/admin/activity/CategoryMgmt.vue'),
                        meta: { title: '类型管理' }
                    },
                    // 4. 内容管理
                    {
                        path: 'content/notice',
                        name: 'AdminNotice',
                        component: () => import('@/views/admin/content/NoticeMgmt.vue'),
                        meta: { title: '公告管理' }
                    },
                    {
                        path: 'content/banner',
                        name: 'AdminBanner',
                        component: () => import('@/views/admin/content/BannerMgmt.vue'),
                        meta: { title: '轮播图管理' }
                    },
                    {
                        path: 'content/experience',
                        name: 'AdminExperience',
                        component: () => import('@/views/admin/content/ExperienceMgmt.vue'),
                        meta: { title: '心得管理' }
                    },
                    {
                        path: 'content/feedback',
                        name: 'AdminFeedback',
                        component: () => import('@/views/admin/content/FeedbackMgmt.vue'),
                        meta: { title: '反馈处理' }
                    },
                    // 5. 积分商城
                    {
                        path: 'mall/product',
                        name: 'AdminProduct',
                        component: () => import('@/views/admin/mall/ProductMgmt.vue'),
                        meta: { title: '商品管理' }
                    },
                    {
                        path: 'mall/exchange',
                        name: 'AdminExchange',
                        component: () => import('@/views/admin/mall/ExchangeRecord.vue'),
                        meta: { title: '兑换记录' }
                    },
                    // 6. 系统设置
                    {
                        path: 'system/config',
                        name: 'AdminConfig',
                        component: () => import('@/views/admin/system/Config.vue'),
                        meta: { title: '系统配置' }
                    },
                    {
                        path: 'system/logs',
                        name: 'AdminLogs',
                        component: () => import('@/views/admin/system/Logs.vue'),
                        meta: { title: '操作日志' }
                    }
                ]
            }
        ]
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/views/error/NotFound.vue'),
        meta: { title: '404', requiresAuth: false }
    }
]

const router = createRouter({
    history: createWebHashHistory(), // [重要] 改为 Hash 模式，解决 App 打开 404 问题
    routes,
    scrollBehavior() {
        return { top: 0 }
    }
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const appStore = useAppStore()
    const siteName = appStore.systemConfig.site_name || '校园志愿者系统'
    document.title = `${to.meta.title || '校园志愿者'} - ${siteName}`
    const token = localStorage.getItem('token')
    const userInfoStr = localStorage.getItem('userInfo')
    const requiresAuth = to.meta.requiresAuth !== false

    // ========== 路由过渡动画 ==========
    const isMobile = window.innerWidth < 768

    if (from.path && from.path !== to.path) {
        // 计算路由深度
        const toDepth = to.path.split('/').filter(Boolean).length
        const fromDepth = from.path.split('/').filter(Boolean).length

        if (isMobile) {
            // 手机端：根据深度决定左滑/右滑
            const name = toDepth >= fromDepth ? 'slide-left' : 'slide-right'
            to.meta._transition = name
        } else {
            // PC端：统一淡入上浮
            to.meta._transition = (to.meta.transition as string) || 'page-fade'
        }

        // 手机端触感震动（轻触 10ms），仅在路由切换时触发（非初始化）
        if (isMobile && navigator.vibrate && from.name) {
            try { navigator.vibrate(10) } catch (_) { /* ignore */ }
        }
    } else {
        to.meta._transition = 'page-fade'
    }

    // 权限校验逻辑
    if (requiresAuth && !token) {
        next({ path: '/login', query: { redirect: to.fullPath } })
        return
    }

    if (token && to.path === '/login') {
        next({ path: '/' })
        return
    }

    // ========== 新增：角色权限严格检查 ==========
    if (token && userInfoStr) {
        try {
            const userInfo = JSON.parse(userInfoStr)
            const userRole = userInfo.role // 'VOLUNTEER', 'ORGANIZER', 'ADMIN'
            const requiredRoles = to.meta.roles as string[]

            // 如果路由定义了需要特定角色，且当前用户角色不在其中
            if (requiredRoles && requiredRoles.length > 0 && !requiredRoles.includes(userRole)) {
                ElMessage.warning('权限不足，已为您跳转到首页')
                if (userRole === 'ADMIN') {
                    next({ path: '/admin/dashboard/overview' })
                } else if (userRole === 'ORGANIZER') {
                    next({ path: '/organizer/dashboard' })
                } else {
                    next({ path: '/' })
                }
                return
            }

            // 处理根路径和首页的智能跳转（保持原有逻辑，但代码结构更清晰）
            if (to.path === '/' || to.path === '/home') {
                if (userRole === 'ORGANIZER') {
                    next({ path: '/organizer/dashboard' })
                    return
                } else if (userRole === 'ADMIN') {
                    next({ path: '/admin/dashboard/overview' })
                    return
                }
            }
        } catch (e) {
            console.error('权限校验解析失败:', e)
        }
    }

    next()
})

export default router