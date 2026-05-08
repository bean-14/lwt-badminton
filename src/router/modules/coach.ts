export default {
  path: "/coach",
  redirect: "/coach/schedules",
  meta: {
    icon: "ep:basketball",
    title: "教练端",
    rank: 2,
    roles: ["coach"]
  },
  children: [
    {
      path: "/coach/schedules",
      name: "CoachSchedules",
      component: () => import("@/views/coach/schedules/index.vue"),
      meta: {
        title: "排课管理",
        roles: ["coach"]
      }
    },
    {
      path: "/coach/bookings",
      name: "CoachBookings",
      component: () => import("@/views/coach/bookings/index.vue"),
      meta: {
        title: "预约列表",
        roles: ["coach"]
      }
    },
    {
      path: "/coach/info",
      name: "CoachInfo",
      component: () => import("@/views/coach/info/index.vue"),
      meta: {
        title: "个人信息",
        roles: ["coach"]
      }
    }
  ]
} satisfies RouteConfigsTable;
