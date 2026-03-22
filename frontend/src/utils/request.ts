import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 Axios 实例
// 判断环境：开发环境(npm run dev)用代理，生产环境(打包APK)用真实IP
// 获取基础地址：优先从 localStorage 获取 (手动配置)，否则使用硬编码兜底
const getBaseURL = () => {
    if (import.meta.env.DEV) return '/api'

    const savedUrl = localStorage.getItem('apiBaseUrl')
    if (savedUrl) {
        // 确保以 /api 结尾
        return savedUrl.endsWith('/api') ? savedUrl : `${savedUrl}/api`
    }
    return import.meta.env.VITE_API_BASE_URL || '/api'
}

const baseURL = getBaseURL()

const service: AxiosInstance = axios.create({
    baseURL,
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 请求拦截器
service.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        // [调试] 打印请求的完整 URL，方便在手机控制台查看（如果是真机调试）
        console.log(`[发送请求] ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`)

        // 从 localStorage 获取 token
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        console.error('请求错误:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    (response: AxiosResponse) => {
        const res = response.data

        // 如果返回的 code 不是 200，说明有错误
        if (res.code && res.code !== 200) {
            // [优化] 如果是背景静默请求（如查询未读数），即便报错也不干扰用户
            const silentEndpoints = ['/message/unreadCount']
            const isSilent = silentEndpoints.some(url => response.config.url?.includes(url))

            // 401: Token 过期或未登录
            if (res.code === 401) {
                const hadToken = !!localStorage.getItem('token')

                if (hadToken && !isSilent) {
                    ElMessage.error(res.message || '登录已过期，请重新登录')
                    localStorage.removeItem('token')
                    localStorage.removeItem('userInfo')
                    router.push('/login')
                }
                return Promise.reject(new Error(res.message || '登录过期'))
            }

            // 403: 权限不足
            if (res.code === 403) {
                if (!isSilent) {
                    ElMessage.error(res.message || '权限不足')
                }
                return Promise.reject(new Error(res.message || '权限不足'))
            }

            // 其他业务错误直接显示后端返回的 message
            ElMessage.error(res.message || '操作失败')
            return Promise.reject(new Error(res.message || '操作失败'))
        }

        return res
    },
    (error) => {
        // [调试] 手机上详细报错打印
        console.error('[响应错误详情]:', {
            message: error.message,
            code: error.code,
            status: error.response?.status,
            data: error.response?.data,
            url: error.config?.url,
            fullURL: error.config?.baseURL + error.config?.url
        })

        if (error.response) {
            const status = error.response.status

            if (status === 401) {
                const hadToken = localStorage.getItem('token')

                // 清除本地过期或无效信息
                localStorage.removeItem('token')
                localStorage.removeItem('userInfo')

                // 仅当之前处于登录状态（有 token）时才弹出友好提示
                // 游客浏览触发的 401 不弹出警告
                if (hadToken) {
                    ElMessage.warning('您的登录已过期，请重新登录')
                }

                router.push('/login')
                return Promise.reject(new Error('登录已过期'))
            }

            if (status === 403) {
                const hadToken = localStorage.getItem('token')
                const isGet = error.config?.method?.toUpperCase() === 'GET'

                // 仅当用户已登录，或者是 POST/PUT/DELETE 等操作时才弹出权限报错
                if (hadToken || !isGet) {
                    const userInfoStr = localStorage.getItem('userInfo')
                    let roleHint = ''
                    const url = error.config?.url || ''
                    const isVolunteerApi = url.includes('/mall/') || url.includes('/experience/') || url.includes('/training/') || url.includes('/notice/')

                    if (userInfoStr && isVolunteerApi) {
                        const userInfo = JSON.parse(userInfoStr)
                        if (userInfo.role === 'ADMIN') roleHint = '（管理员账号无法访问此功能）'
                        else if (userInfo.role === 'ORGANIZER') roleHint = '（组织者账号无法访问此功能）'
                    }

                    const backendMsg = error.response.data?.message
                    ElMessage.error(backendMsg || `权限不足${roleHint}`)
                }

                return Promise.reject(new Error('权限不足'))
            }

            switch (status) {
                case 404:
                    ElMessage.error('请求的资源不存在')
                    break
                case 500:
                    ElMessage.error('服务器内部错误')
                    break
                default:
                    ElMessage.error(error.response.data?.message || '请求失败')
            }
        } else if (error.message.includes('timeout')) {
            ElMessage.error('请求超时，请稍后重试')
        } else {
            ElMessage.error('网络错误，请检查网络连接')
        }

        return Promise.reject(error)
    }
)

// 封装请求方法
export const request = {
    get<T = any>(url: string, params?: object, config?: AxiosRequestConfig): Promise<T> {
        return service.get(url, { params, ...config })
    },

    post<T = any>(url: string, data?: object, config?: AxiosRequestConfig): Promise<T> {
        return service.post(url, data, config)
    },

    put<T = any>(url: string, data?: object, config?: AxiosRequestConfig): Promise<T> {
        return service.put(url, data, config)
    },

    delete<T = any>(url: string, params?: object, config?: AxiosRequestConfig): Promise<T> {
        return service.delete(url, { params, ...config })
    },

    // 上传文件
    upload<T = any>(url: string, file: File, fieldName: string = 'file'): Promise<T> {
        const formData = new FormData()
        formData.append(fieldName, file)
        return service.post(url, formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        })
    },

    // 解析图片 / 资源 URL
    resolveUrl(url: string | undefined | null): string {
        if (!url) return ''
        if (url.startsWith('http') || url.startsWith('blob:') || url.startsWith('data:')) {
            return url
        }

        // 处理基础路径
        // 如果 url 以 /api 开始且 baseURL 也是以 /api 结尾的绝对路径，需要处理重叠
        const base = getBaseURL()

        if (base.startsWith('http')) {
            // 如果 base 是 http://.../api
            // 如果 url 是 /api/files/xxx
            if (url.startsWith('/api')) {
                return base.replace(/\/api$/, '') + url
            }
            return base.endsWith('/') ? base + url.replace(/^\//, '') : base + '/' + url.replace(/^\//, '')
        }

        // Web 开发环境 (base = '/api')
        return url.startsWith('/') ? url : '/' + url
    }
}

export default service
