import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
    // 从 localStorage 初始化
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
    const token = ref(localStorage.getItem('token') || '')

    // 计算属性
    const isLoggedIn = computed(() => !!token.value)
    const role = computed(() => userInfo.value?.role || '')

    // 操作
    function setUser(info: any, t: string) {
        userInfo.value = info
        token.value = t
        localStorage.setItem('userInfo', JSON.stringify(info))
        localStorage.setItem('token', t)
    }

    function clearUser() {
        userInfo.value = null
        token.value = ''
        localStorage.removeItem('userInfo')
        localStorage.removeItem('token')
    }

    // 单独更新头像【关键：解决右上角头像不同步问题】
    function setAvatar(avatarUrl: string) {
        if (userInfo.value) {
            userInfo.value.avatar = avatarUrl
            localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        }
    }

    // 消息未读计数
    const unreadCount = ref(0) // 新增

    const fetchUnreadCount = async () => {
        try {
            // 这里为了避免循环引用，我们需要按需引入 request
            // 或者假设 request 可以在这里使用，通常 store 中可以直接引入 utils/request
            const { request } = await import('@/utils/request')
            const res = await request.get('/message/unreadCount')
            if (res && res.data !== undefined) {
                unreadCount.value = res.data
            }
        } catch (e) {
            console.error('获取未读消息数失败', e)
        }
    }

    return {
        userInfo,
        token,
        isLoggedIn,
        role,
        unreadCount, // 导出
        setUser,
        clearUser,
        setAvatar,
        fetchUnreadCount // 导出
    }
})
