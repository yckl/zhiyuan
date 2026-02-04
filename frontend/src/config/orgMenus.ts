import {
    Odometer, List, UserFilled, Bell, ChatLineSquare, Setting
} from '@element-plus/icons-vue'

export const orgMenus = [
    {
        name: '工作台',
        path: '/organizer/dashboard',
        icon: Odometer
    },
    {
        name: '活动管理',
        path: '/organizer/activity',
        icon: List,
        children: [
            { name: '发布活动', path: '/organizer/activity/create' },
            { name: '我的活动', path: '/organizer/activity/list' }
        ]
    },
    {
        name: '人员管理',
        path: '/organizer/personnel',
        icon: UserFilled,
        children: [
            { name: '报名审核', path: '/organizer/personnel/audit' },
            { name: '现场签到', path: '/organizer/personnel/checkin' }
        ]
    },
    {
        name: '通知管理',
        path: '/organizer/notification',
        icon: Bell,
        children: [
            { name: '发送通知', path: '/organizer/notification/send' },
            { name: '通知历史', path: '/organizer/notification/history' }
        ]
    },
    {
        name: '结算与评价',
        path: '/organizer/feedback',
        icon: ChatLineSquare,
        children: [
            { name: '工时录入', path: '/organizer/feedback/hours' },
            { name: '服务评价', path: '/organizer/feedback/reviews' }
        ]
    },
    {
        name: '组织设置',
        path: '/organizer/setting',
        icon: Setting
    }
]
