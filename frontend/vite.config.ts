import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
    base: './', // [重要] 适配 HBuilderX/Cordova 等打包环境，使用相对路径
    plugins: [
        vue({
            template: {
                compilerOptions: {
                    // 关键配置：告诉 Vue 编译器，所有 theme- 开头的标签（如 theme-button）
                    // 都是自定义 Web Component，不需要 Vue 组件解析，防止控制台报警告
                    isCustomElement: (tag) => tag.startsWith('theme-')
                }
            }
        })
    ],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, 'src')
        }
    },
    server: {
        port: 5173,
        host: true,
        open: true,
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true
            }
        }
    },
    css: {
        preprocessorOptions: {
            scss: {
                additionalData: ``
            }
        }
    }
})