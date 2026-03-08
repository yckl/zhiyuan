/**
 * 图片处理工具类
 */

/**
 * 获取图片完整URL
 * @param url 后端返回的路径
 * @param defaultImg 默认图片
 * @returns 完整的图片地址
 */
export const getImageUrl = (url: string | null | undefined, defaultImg: string = '/default-cover.jpg') => {
    if (!url) return defaultImg

    // 如果是完整路径，直接返回
    if (url.startsWith('http')) return url

    // 如果如果是 / 开头的路径，移除它
    const path = url.startsWith('/') ? url.substring(1) : url

    // 从 localStorage 获取 API 基础地址 (与 request.ts 逻辑保持一致)
    const getBaseAPI = () => {
        const savedUrl = localStorage.getItem('apiBaseUrl')
        if (savedUrl) {
            return savedUrl.endsWith('/') ? savedUrl : `${savedUrl}/`
        }
        // 生产环境兜底
        return 'http://192.168.0.101:8080/'
    }

    const host = import.meta.env.DEV ? 'http://localhost:8080/' : getBaseAPI()

    // 返回完整地址 (不包含 /api，因为静态资源通常在根目录下或特定文件服务下)
    // 如果后端配置了静态资源映射，通常是 http://host/uploads/xxx.jpg
    return `${host}${path}`
}
