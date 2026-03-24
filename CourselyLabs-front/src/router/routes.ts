import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        component: () => import('@/views/HomeView.vue'),
        meta: { title: 'Inicio — CourselyLabs' },
      },
      {
        path: 'cursos',
        component: () => import('@/views/CoursesView.vue'),
        meta: { title: 'Cursos — CourselyLabs' },
      },
      {
        path: 'cursos/:slug',
        component: () => import('@/views/CourseDetailView.vue'),
      },
      {
        path: 'login',
        component: () => import('@/views/formView.vue'),
        meta: { title: 'Iniciar sesion — CourselyLabs' },
      },
      {
        path: 'iniSession',
        component: () => import('@/components/formLogin.vue'),
        meta: { title: 'Iniciar sesion — CourselyLabs' },
      },
      {
        path: 'register',
        component: () => import('@/components/formRegister.vue'),
        meta: { title: 'Registro — CourselyLabs' },
      },
      {
        path: 'terminios',
        component: () => import('@/components/cardTerminosCondiciones.vue'),
        meta: { title: 'Terminos y condiciones — CourselyLabs' },
      },
      {
        path: 'profile',
        component: () => import('@/views/ProfileView.vue'),
        meta: { requiresAuth: true, title: 'Mi perfil — CourselyLabs' },
      },
      {
        path: 'mis-cursos',
        component: () => import('../views/MyCoursesView.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'verificar-email',
        component: () => import('@/views/VerifyEmailView.vue'),
        meta: { title: 'Verificar email — CourselyLabs' },
      },
    ],
  },
  {
    path: '/cursos/:slug/leccion/:lessonId',
    component: () => import('@/views/LessonView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/:catchAll(.*)*',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        component: () => import('@/views/NotFoundView.vue'),
        meta: { title: 'Pagina no encontrada — CourselyLabs' },
      },
    ],
  },
]

export default routes
