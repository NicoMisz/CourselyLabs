import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
    {
        path: '/',
        component: () => import('../layouts/MainLayout.vue'),
        children: [
        // Ruta por defecto que redirige a home
        //{ path: '', redirect: '/home' },
        
        { path: '', component: () => import('../views/HomeView.vue')},
        { path: 'carrito', component: () => import('../views/CoursesView.vue') },
        ],
    },

  // Ruta 404
    {
        path: '/:catchAll(.*)*',
        component: () => import('../views/AboutView.vue'),
    },
];

export default routes