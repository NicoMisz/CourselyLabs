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
        meta: { title: 'Iniciar sesión — CourselyLabs' },
      },
      {
        path: 'iniSession',
        component: () => import('@/components/formLogin.vue'),
        meta: { title: 'Iniciar sesión — CourselyLabs' },
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
      {
        path: 'premium',
        component: () => import('@/views/PremiumPage.vue'),
        meta: { title: 'Premium — CourselyLabs' },
      },
      {
        path: 'pago/exito',
        component: () => import('@/views/PaymentSuccessView.vue'),
        meta: { title: 'Pago exitoso — CourselyLabs' },
      },
      {
        path: 'pago/cancelado',
        component: () => import('@/views/PaymentCancelledView.vue'),
        meta: { title: 'Pago cancelado — CourselyLabs' },
      },
    ],
  },
  {
    path: '/cursos/:slug/leccion/:lessonId',
    component: () => import('@/views/LessonView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/instructor',
    component: () => import('@/layouts/InstructorLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/instructor/cursos',
      },
      {
        path: 'cursos',
        component: () => import('@/views/instructor/InstructorCourseList.vue'),
        meta: { title: 'Mis cursos — Instructor' },
      },
      {
        path: 'calificar',
        component: () => import('@/views/instructor/GradingDashboard.vue'),
        meta: { title: 'Calificar — Instructor' },
      },
      {
        path: 'cursos/nuevo',
        redirect: '/instructor/cursos',
      },
      {
        path: 'cursos/:id/editar',
        component: () => import('@/views/instructor/CourseWizard.vue'),
        meta: { title: 'Editar curso — Instructor' },
      },
      {
        path: 'cursos/:id/contenido',
        redirect: to => `/instructor/cursos/${to.params.id}/editar`,
      },
    ],
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresRole: 'admin' },
    children: [
      {
        path: '',
        component: () => import('@/views/admin/AdminDashboard.vue'),
        meta: { title: 'Panel admin — CourselyLabs' },
      },
      {
        path: 'cursos',
        component: () => import('@/views/admin/AdminCourseQueue.vue'),
        meta: { title: 'Cursos pendientes — Admin' },
      },
      {
        path: 'usuarios',
        component: () => import('@/views/admin/AdminUserTable.vue'),
        meta: { title: 'Usuarios — Admin' },
      },
    ],
  },
  {
    path: '/:catchAll(.*)*',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        component: () => import('@/views/NotFoundView.vue'),
        meta: { title: 'Página no encontrada — CourselyLabs' },
      },
    ],
  },
]

export default routes
