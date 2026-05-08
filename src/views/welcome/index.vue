<template>
  <div class="p-6">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-800">
        {{ welcomeText }}
      </h1>
      <p class="text-gray-400 mt-1">{{ roleText }}</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb-6">
      <el-col v-for="card in statsCards" :key="card.label" :span="6">
        <el-card shadow="never">
          <div class="flex items-center gap-4">
            <div
              class="w-12 h-12 rounded-lg flex items-center justify-center"
              :style="{ backgroundColor: card.bg }"
            >
              <el-icon :size="22" :color="card.color">
                <component :is="card.icon" />
              </el-icon>
            </div>
            <div>
              <div class="text-2xl font-bold">{{ card.value }}</div>
              <div class="text-gray-400 text-sm">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-card shadow="never">
      <template #header>
        <span class="font-semibold">快捷操作</span>
      </template>
      <el-row :gutter="20">
        <el-col v-for="action in quickActions" :key="action.title" :span="6">
          <el-card
            shadow="hover"
            class="cursor-pointer text-center py-4"
            @click="router.push(action.path)"
          >
            <el-icon :size="28" :color="action.color" class="mb-2">
              <component :is="action.icon" />
            </el-icon>
            <div class="font-medium">{{ action.title }}</div>
            <div class="text-gray-400 text-xs mt-1">{{ action.desc }}</div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 加载骨架 -->
    <div
      v-if="loading"
      class="absolute inset-0 bg-white/60 flex items-center justify-center"
    >
      <el-icon class="is-loading" :size="32" color="#409eff">
        <Loading />
      </el-icon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useUserStoreHook } from "@/store/modules/user";
import { getMyInfoApi } from "@/api/student";
import { getCoachBookingsApi } from "@/api/coach";
import {
  getVenuesApi,
  getStudentsApi,
  getCoachesApi,
  getDashboardApi
} from "@/api/admin";
import {
  Calendar,
  Tickets,
  Basketball,
  Setting,
  List,
  Plus,
  Checked,
  DataAnalysis,
  User,
  Medal,
  WarningFilled,
  Loading
} from "@element-plus/icons-vue";

defineOptions({ name: "Welcome" });

const router = useRouter();
const loading = ref(true);
const userStore = useUserStoreHook();

// 学生数据
const remainingHours = ref(0);
const upcomingBookings = ref(0);

// 教练数据
const todayBookings = ref(0);
const pendingConfirm = ref(0);

// 管理员数据
const totalVenues = ref(0);
const totalStudents = ref(0);
const totalCoaches = ref(0);

const userType = computed(
  () => userStore.userType || userStore.roles?.[0] || ""
);
const nickname = computed(() => userStore.nickname || "用户");

const welcomeText = computed(() => {
  const map: Record<string, string> = {
    student: `你好，${nickname.value} 🏸`,
    coach: `欢迎回来，${nickname.value} 👋`,
    admin: `${nickname.value}，下午好 ⚙️`
  };
  return map[userType.value] || "欢迎回来";
});

const roleText = computed(() => {
  const map: Record<string, string> = {
    student: "查看可预约时段，管理你的课程",
    coach: "管理排课，确认学生预约",
    admin: "管理系统数据，管理用户和场地"
  };
  return map[userType.value] || "";
});

const statsCards = computed(() => {
  const map: Record<
    string,
    Array<{
      label: string;
      value: number | string;
      icon: any;
      color: string;
      bg: string;
    }>
  > = {
    student: [
      {
        label: "剩余课时",
        value: remainingHours.value,
        icon: Tickets,
        color: "#409eff",
        bg: "#ecf5ff"
      },
      {
        label: "待确认预约",
        value: upcomingBookings.value,
        icon: Calendar,
        color: "#e6a23c",
        bg: "#fdf6ec"
      }
    ],
    coach: [
      {
        label: "今日预约",
        value: todayBookings.value,
        icon: Calendar,
        color: "#409eff",
        bg: "#ecf5ff"
      },
      {
        label: "待确认",
        value: pendingConfirm.value,
        icon: WarningFilled,
        color: "#e6a23c",
        bg: "#fdf6ec"
      }
    ],
    admin: [
      {
        label: "场地数",
        value: totalVenues.value,
        icon: Basketball,
        color: "#67c23a",
        bg: "#f0f9eb"
      },
      {
        label: "教练数",
        value: totalCoaches.value,
        icon: Medal,
        color: "#409eff",
        bg: "#ecf5ff"
      },
      {
        label: "学生数",
        value: totalStudents.value,
        icon: User,
        color: "#e6a23c",
        bg: "#fdf6ec"
      }
    ]
  };
  return map[userType.value] || [];
});

const quickActions = computed(() => {
  const map: Record<
    string,
    Array<{
      title: string;
      desc: string;
      path: string;
      icon: any;
      color: string;
    }>
  > = {
    student: [
      {
        title: "可预约时段",
        desc: "查看并预约课程",
        path: "/student/schedules",
        icon: List,
        color: "#409eff"
      },
      {
        title: "我的预约",
        desc: "查看预约记录",
        path: "/student/bookings",
        icon: Calendar,
        color: "#e6a23c"
      },
      {
        title: "上课历史",
        desc: "已完成课程",
        path: "/student/history",
        icon: Checked,
        color: "#67c23a"
      },
      {
        title: "个人信息",
        desc: "查看剩余课时",
        path: "/student/info",
        icon: User,
        color: "#909399"
      }
    ],
    coach: [
      {
        title: "排课管理",
        desc: "设置可预约时段",
        path: "/coach/schedules",
        icon: Plus,
        color: "#409eff"
      },
      {
        title: "预约列表",
        desc: "查看并确认预约",
        path: "/coach/bookings",
        icon: List,
        color: "#e6a23c"
      }
    ],
    admin: [
      {
        title: "数据看板",
        desc: "查看统计数据",
        path: "/admin/dashboard",
        icon: DataAnalysis,
        color: "#409eff"
      },
      {
        title: "场地管理",
        desc: "管理羽毛球场地",
        path: "/admin/venues",
        icon: Basketball,
        color: "#67c23a"
      },
      {
        title: "用户管理",
        desc: "管理教练和学生",
        path: "/admin/users",
        icon: User,
        color: "#e6a23c"
      },
      {
        title: "课时充值",
        desc: "为学生充值课时",
        path: "/admin/recharge",
        icon: Tickets,
        color: "#909399"
      }
    ]
  };
  return map[userType.value] || [];
});

async function fetchData() {
  loading.value = true;
  try {
    const type = userType.value;
    if (type === "student") {
      const info = await getMyInfoApi();
      remainingHours.value = info.remainingHours ?? 0;
    } else if (type === "coach") {
      const today = new Date().toISOString().slice(0, 10);
      const bookings = await getCoachBookingsApi({ date: today });
      todayBookings.value = bookings.length;
      pendingConfirm.value = bookings.filter(
        b => b.status === "pending"
      ).length;
    } else if (type === "admin") {
      const [venues, students, coaches] = await Promise.all([
        getVenuesApi(),
        getStudentsApi(),
        getCoachesApi()
      ]);
      totalVenues.value = venues.length;
      totalStudents.value = students.length;
      totalCoaches.value = coaches.length;
    }
  } catch {
    // 静默失败，不影响页面显示
  } finally {
    loading.value = false;
  }
}

onMounted(fetchData);
</script>
