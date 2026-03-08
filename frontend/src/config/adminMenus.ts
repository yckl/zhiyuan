import {
    DataLine, User, Flag, Document, Goods, Setting
} from '@element-plus/icons-vue'

export interface MenuItem {
    name: string
    path: string
    icon?: any
    children?: MenuItem[]
}

export const adminMenus: MenuItem[] = [
    {
        name: '数据看板',
        path: '/admin/dashboard',
        icon: DataLine,
        children: [
            { name: '首页概览', path: '/admin/dashboard/overview' },
            { name: '数据统计', path: '/admin/dashboard/statistics' }
        ]
    },
    {
        name: '用户中心',
        path: '/admin/user',
        icon: User,
        children: [
            { name: '志愿者管理', path: '/admin/user/volunteer' },
            { name: '组织管理', path: '/admin/user/organizer' }
        ]
    },
    {
        name: '活动运营',
        path: '/admin/activity',
        icon: Flag,
        children: [
            { name: '活动审核', path: '/admin/activity/audit' },
            { name: '活动管理', path: '/admin/activity/list' },
            { name: '类型管理', path: '/admin/activity/category' }
        ]
    },
    {
        name: '内容管理',
        path: '/admin/content',
        icon: Document,
        children: [
            { name: '公告管理', path: '/admin/content/notice' },
            { name: '轮播图管理', path: '/admin/content/banner' },
            { name: '心得管理', path: '/admin/content/experience' },
            { name: '反馈处理', path: '/admin/content/feedback' }
        ]
    },
    {
        name: '积分商城',
        path: '/admin/mall',
        icon: Goods,
        children: [
            { name: '商品管理', path: '/admin/mall/product' },
            { name: '兑换记录', path: '/admin/mall/exchange' }
        ]
    },
    {
        name: '系统设置',
        path: '/admin/system',
        icon: Setting,
        children: [
            { name: '系统配置', path: '/admin/system/config' },
            { name: '操作日志', path: '/admin/system/logs' }
        ]
    }
]
