import { ref, onMounted, onUnmounted } from 'vue'

// 定义全局响应式变量，避免每个组件都重新计算
const isMobile = ref(false)
const windowWidth = ref(0)

// ⚠️ 核心阈值：将判断标准从 768 提升到 992
// 意味着：iPad 竖屏 (768px)、大屏手机都会被强制算作 Mobile
const MOBILE_BREAKPOINT = 992

export function useMobile() {
    const checkDevice = () => {
        const width = window.innerWidth
        windowWidth.value = width
        // 如果宽度小于 992，就认为是手机/平板模式
        isMobile.value = width < MOBILE_BREAKPOINT
    }

    onMounted(() => {
        checkDevice()
        window.addEventListener('resize', checkDevice)
    })

    onUnmounted(() => {
        window.removeEventListener('resize', checkDevice)
    })

    return {
        isMobile,
        windowWidth
    }
}
