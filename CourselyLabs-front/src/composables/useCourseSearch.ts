import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchCourses, type SortBy, type ViewMode } from '@/api/courseSearch'
import type { Course } from '@/types/course'
import { isAxiosError } from 'axios'

type Level = 'PRINCIPIANTE' | 'INTERMEDIO' | 'AVANZADO'

const SEARCH_HISTORY_KEY = 'course-search-history'
const VIEW_MODE_KEY = 'course-search-view-mode'

function debounce<TArgs extends unknown[]>(fn: (...args: TArgs) => void, wait: number) {
    let timer: ReturnType<typeof setTimeout> | null = null
    return (...args: TArgs) => {
        if (timer) clearTimeout(timer)
        timer = setTimeout(() => fn(...args), wait)
    }
}

export function useCourseSearch() {
    const route = useRoute()
    const router = useRouter()

    const courses = ref<Course[]>([])
    const loading = ref(false)
    const loadingMore = ref(false)
    const error = ref('')

    const keyword = ref<string>((route.query.q as string) || '')
    const categoryId = ref<number | null>(route.query.cat ? Number(route.query.cat) : null)
    const level = ref<Level | null>((route.query.lvl as Level) || null)
    const isFree = ref<boolean | null>(route.query.free === undefined ? null : route.query.free === 'true')
    const minRating = ref<number | null>(route.query.min ? Number(route.query.min) : null)
    const sortBy = ref<SortBy>((route.query.sort as SortBy) || 'recent')
    const viewMode = ref<ViewMode>((localStorage.getItem(VIEW_MODE_KEY) as ViewMode) || (route.query.view as ViewMode) || 'grid')

    const page = ref(0)
    const size = ref(12)
    const totalElements = ref(0)
    const hasNext = ref(true)

    const searchHistory = ref<string[]>(JSON.parse(localStorage.getItem(SEARCH_HISTORY_KEY) || '[]'))

    const hasActiveFilters = computed(() =>
        !!keyword.value ||
        categoryId.value !== null ||
        level.value !== null ||
        isFree.value !== null ||
        minRating.value !== null
    )

    async function fetchPage(reset = false) {
        if (reset) {
            page.value = 0
            hasNext.value = true
            courses.value = []
        }

        if (!hasNext.value) return

        if (page.value === 0) loading.value = true
        else loadingMore.value = true

        error.value = ''

        try {
            const response = await searchCourses({
                keyword: keyword.value || undefined,
                categoryId: categoryId.value,
                level: level.value,
                isFree: isFree.value,
                minRating: minRating.value,
                sortBy: sortBy.value,
                page: page.value,
                size: size.value
            })

            if (reset) courses.value = response.content
            else courses.value.push(...response.content)

            totalElements.value = response.totalElements
            hasNext.value = !response.last

            if (!response.last) {
                page.value += 1
            }

            persistSearchHistory()
        } catch (err: unknown) {
            if (isAxiosError<{ message?: string }>(err)) {
                error.value = err.response?.data?.message || 'Error al cargar los cursos.'
            } else {
                error.value = 'Error al cargar los cursos.'
            }
        } finally {
            loading.value = false
            loadingMore.value = false
        }
    }

    function persistSearchHistory() {
        const value = keyword.value.trim()
        if (!value) return

        const merged = [value, ...searchHistory.value.filter(item => item !== value)].slice(0, 5)
        searchHistory.value = merged
        localStorage.setItem(SEARCH_HISTORY_KEY, JSON.stringify(merged))
    }

    function syncUrl() {
        router.replace({
            query: {
                ...route.query,
                q: keyword.value || undefined,
                cat: categoryId.value ?? undefined,
                lvl: level.value ?? undefined,
                free: isFree.value === null ? undefined : String(isFree.value),
                min: minRating.value ?? undefined,
                sort: sortBy.value,
                view: viewMode.value
            }
        })
    }

    function clearAllFilters() {
        keyword.value = ''
        categoryId.value = null
        level.value = null
        isFree.value = null
        minRating.value = null
    }

    function removeFilterChip(key: 'keyword' | 'categoryId' | 'level' | 'isFree' | 'minRating') {
        if (key === 'keyword') keyword.value = ''
        if (key === 'categoryId') categoryId.value = null
        if (key === 'level') level.value = null
        if (key === 'isFree') isFree.value = null
        if (key === 'minRating') minRating.value = null
    }

    function setViewMode(mode: ViewMode) {
        viewMode.value = mode
        localStorage.setItem(VIEW_MODE_KEY, mode)
    }

    const debouncedKeywordSearch = debounce(() => {
        syncUrl()
        fetchPage(true)
    }, 300)

    watch(keyword, () => debouncedKeywordSearch())

    watch([categoryId, level, isFree, minRating, sortBy], () => {
        syncUrl()
        fetchPage(true)
    })

    return {
        courses,
        loading,
        loadingMore,
        error,
        totalElements,
        hasNext,
        keyword,
        categoryId,
        level,
        isFree,
        minRating,
        sortBy,
        viewMode,
        searchHistory,
        hasActiveFilters,
        fetchPage,
        clearAllFilters,
        removeFilterChip,
        setViewMode
    }
}