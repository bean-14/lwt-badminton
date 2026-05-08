export default {
  path: "/student",
  redirect: "/student/schedules",
  meta: {
    icon: "ep:user",
    title: "学生端",
    rank: 1,
    roles: ["student"]
  },
  children: [
    {
      path: "/student/schedules",
      name: "StudentSchedules",
      component: () => import("@/views/student/schedules/index.vue"),
      meta: {
        title: "可预约时段",
        roles: ["student"]
      }
    },
    {
      path: "/student/bookings",
      name: "StudentBookings",
      component: () => import("@/views/student/bookings/index.vue"),
      meta: {
        title: "我的预约",
        roles: ["student"]
      }
    },
    {
      path: "/student/history",
      name: "StudentHistory",
      component: () => import("@/views/student/history/index.vue"),
      meta: {
        title: "上课历史",
        roles: ["student"]
      }
    },
    {
      path: "/student/info",
      name: "StudentInfo",
      component: () => import("@/views/student/info/index.vue"),
      meta: {
        title: "个人信息",
        roles: ["student"]
      }
    }
  ]
} satisfies RouteConfigsTable;
