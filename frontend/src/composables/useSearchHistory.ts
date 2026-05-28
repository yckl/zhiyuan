import { ref } from 'vue'

const MAX_HISTORY_LENGTH = 10
const STORAGE_KEY = 'volunteer_search_history'

// 全局响应式状态，确保多个组件同步更新
const searchHistory = ref<string[]>([])
const initialized = ref(false)

const initHistory = () => {
    if (typeof window === 'undefined') return
    try {
        const stored = localStorage.getItem(STORAGE_KEY)
        if (stored) {
            searchHistory.value = JSON.parse(stored)
        }
    } catch (e) {
        console.error('Failed to parse search history', e)
        searchHistory.value = []
    }
}

const saveHistory = () => {
    if (typeof window === 'undefined') return
    try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(searchHistory.value))
    } catch (e) {
        console.error('Failed to save search history', e)
    }
}

export function useSearchHistory() {
    if (!initialized.value) {
        initHistory()
        initialized.value = true
    }

    // 添加搜索记录
    const addHistory = (keyword: string) => {
        if (!keyword) return
        const kw = keyword.trim()
        if (!kw) return

        const list = [...searchHistory.value]
        const index = list.indexOf(kw)
        if (index > -1) {
            list.splice(index, 1)
        }
        list.unshift(kw)
        if (list.length > MAX_HISTORY_LENGTH) {
            list.pop()
        }
        searchHistory.value = list
        saveHistory()
    }

    // 移除单条记录
    const removeHistory = (keyword: string) => {
        const list = [...searchHistory.value]
        const index = list.indexOf(keyword)
        if (index > -1) {
            list.splice(index, 1)
            searchHistory.value = list
            saveHistory()
            return true
        }
        return false
    }

    // 清空所有记录
    const clearHistory = () => {
        searchHistory.value = []
        localStorage.removeItem(STORAGE_KEY)
    }

    // 提供给 el-autocomplete 使用的选择器格式
    const getSuggestionList = (queryString: string) => {
        const list = searchHistory.value
        const results = queryString
            ? list.filter(item => item.toLowerCase().includes(queryString.toLowerCase()))
            : list

        return results.map(item => ({ value: item }))
    }

    return {
        searchHistory,
        addHistory,
        removeHistory,
        clearHistory,
        getSuggestionList
    }
}
