export default {
  path: "/admin",
  redirect: "/admin/dashboard",
  meta: {
    icon: "ep:setting",
    title: "管理端",
    rank: 3,
    roles: ["admin"]
  },
  children: [
    {
      path: "/admin/dashboard",
      name: "AdminDashboard",
      component: () => import("@/views/admin/dashboard/index.vue"),
      meta: {
        title: "数据看板",
        roles: ["admin"]
      }
    },
    {
      path: "/admin/venues",
      name: "AdminVenues",
      component: () => import("@/views/admin/venues/index.vue"),
      meta: {
        title: "场地管理",
        roles: ["admin"]
      }
    },
    {
      path: "/admin/users",
      name: "AdminUsers",
      component: () => import("@/views/admin/users/index.vue"),
      meta: {
        title: "用户管理",
        roles: ["admin"]
      }
    },
    {
      path: "/admin/recharge",
      name: "AdminRecharge",
      component: () => import("@/views/admin/recharge/index.vue"),
      meta: {
        title: "课时充值",
        roles: ["admin"]
      }
    }
  ]
} satisfies RouteConfigsTable;
