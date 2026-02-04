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

    return {
        userInfo,
        token,
        isLoggedIn,
        role,
        setUser,
        clearUser
    }
})
