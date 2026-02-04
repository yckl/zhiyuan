import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 Axios 实例
const service: AxiosInstance = axios.create({
    baseURL: '/api',
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 请求拦截器
service.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
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
            // 401: Token 过期或未登录
            if (res.code === 401) {
                ElMessage.error(res.message || '登录已过期，请重新登录')
                localStorage.removeItem('token')
                localStorage.removeItem('userInfo')
                router.push('/login')
                return Promise.reject(new Error(res.message || '登录过期'))
            }

            // 其他业务错误直接显示后端返回的 message
            ElMessage.error(res.message || '操作失败')
            return Promise.reject(new Error(res.message || '操作失败'))
        }

        return res
    },
    (error) => {
        console.error('响应错误:', error)

        if (error.response) {
            const status = error.response.status

            switch (status) {
                case 401:
                    ElMessage.error('登录已过期，请重新登录')
                    localStorage.removeItem('token')
                    localStorage.removeItem('userInfo')
                    router.push('/login')
                    break
                case 403:
                    ElMessage.error('没有权限访问')
                    break
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
    }
}

export default service
