import { ElMessage } from 'element-plus'

/**
 * 跨浏览器复制文本到剪贴板
 * 优先使用 modern navigator.clipboard API
 * 在非安全上下文 (HTTP) 降级使用 document.execCommand('copy')
 */
export const copyToClipboard = async (text: string, successMsg: string = '已复制到剪贴板') => {
    if (!text) return false

    try {
        // 1. 尝试使用现代 API (需要 HTTPS 或 localhost)
        if (navigator.clipboard && window.isSecureContext) {
            await navigator.clipboard.writeText(text)
            if (successMsg) ElMessage.success(successMsg)
            return true
        }

        // 2. 降级方案 (适用于 HTTP 的 IP 访问)
        const textArea = document.createElement('textarea')
        textArea.value = text

        // 将 textarea 移出可视区域
        textArea.style.position = 'fixed'
        textArea.style.left = '-999999px'
        textArea.style.top = '-999999px'
        document.body.appendChild(textArea)

        // 选中文本
        textArea.focus()
        textArea.select()

        // 执行复制逻辑
        const successful = document.execCommand('copy')
        textArea.remove()

        if (successful) {
            if (successMsg) ElMessage.success(successMsg)
            return true
        } else {
            ElMessage.warning('您的浏览器不支持自动复制，请手动复制')
            return false
        }
    } catch (err) {
        console.error('复制失败:', err)
        ElMessage.warning('您的浏览器不支持自动复制，请手动复制')
        return false
    }
}
