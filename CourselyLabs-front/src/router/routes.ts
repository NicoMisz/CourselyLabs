import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
    {
        path: '/',
        component: () => import('../layouts/MainLayout.vue'),
        children: [
        // Ruta por defecto que redirige a home
        //{ path: '', redirect: '/home' },
        
        { path: '', component: () => import('../views/HomeView.vue')},
        { path: 'cursos', component: () => import('../views/CoursesView.vue') },
        { path: 'login' , component: () => import('../views/formView.vue') },
        { path: 'iniSession', component: () => import('../components/formLogin.vue') },
        { path: 'register' , component: () => import('../components/formRegister.vue') },
        ],
    },

  // Rutas con AltLayout (sin sidebar) — login, registro, etc.
    // {
    //     path: '/auth',
    //     component: () => import('../layouts/AltLayout.vue'),
    //     children: [
    //         { path: 'login', component: () => import('../views/LoginView.vue') },
    //         { path: 'registro', component: () => import('../views/RegistroView.vue') },
    //     ],
    // },

  // Ruta 404
    {
        path: '/:catchAll(.*)*',
        component: () => import('../views/AboutView.vue'),
    },
];

export default routes