<template>
  <div class="p-6">
    <!-- 概览卡片 -->
    <el-row :gutter="16" class="mb-4">
      <el-col :span="6">
        <el-card shadow="never">
          <div class="text-center py-3">
            <div class="text-gray-400 text-xs">总预约数</div>
            <div class="text-2xl font-bold mt-1">
              {{ dashboardData.totalBookings }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="text-center py-3">
            <div class="text-gray-400 text-xs">场地数</div>
            <div class="text-2xl font-bold mt-1">
              {{ Object.keys(dashboardData.venueStats || {}).length }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="text-center py-3">
            <div class="text-gray-400 text-xs">教练数</div>
            <div class="text-2xl font-bold mt-1">
              {{ Object.keys(dashboardData.coachStats || {}).length }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="text-center py-3">
            <div class="text-gray-400 text-xs">学生数</div>
            <div class="text-2xl font-bold mt-1">{{ studentStats.length }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选栏 -->
    <el-card shadow="never" class="mb-4">
      <div class="flex flex-wrap items-center gap-3">
        <el-radio-group
          v-model="quickRange"
          size="default"
          @change="onQuickRangeChange"
        >
          <el-radio-button value="today">当日</el-radio-button>
          <el-radio-button value="week">本周</el-radio-button>
          <el-radio-button value="month">本月</el-radio-button>
          <el-radio-button value="quarter">本季</el-radio-button>
          <el-radio-button value="">自定义</el-radio-button>
        </el-radio-group>

        <el-date-picker
          v-model="dateRange"
          type="daterange"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          clearable
          :disabled="quickRange !== ''"
          @change="onDatePickerChange"
        />
      </div>
    </el-card>

    <!-- 图表区域 -->
    <el-row :gutter="16">
      <el-col :span="12" class="mb-4">
        <el-card shadow="never">
          <template #header
            ><span class="font-semibold text-sm">场地使用统计</span></template
          >
          <div
            ref="venueChartRef"
            v-loading="venueLoading"
            style="height: 300px"
          />
        </el-card>
      </el-col>
      <el-col :span="12" class="mb-4">
        <el-card shadow="never">
          <template #header
            ><span class="font-semibold text-sm">教练上课统计</span></template
          >
          <div
            ref="coachChartRef"
            v-loading="coachLoading"
            style="height: 300px"
          />
        </el-card>
      </el-col>
      <el-col :span="24">
        <el-card shadow="never">
          <template #header
            ><span class="font-semibold text-sm">学生上课统计</span></template
          >
          <div
            ref="studentChartRef"
            v-loading="studentLoading"
            style="height: 350px"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from "vue";
import { ElMessage } from "element-plus";
import * as echarts from "echarts";
import {
  getDashboardApi,
  getVenueStatsApi,
  getCoachStatsApi,
  getStudentStatsApi
} from "@/api/admin";
import type {
  DashboardData,
  VenueStatsItem,
  CoachStatsItem,
  StudentStatsItem
} from "@/api/admin";

defineOptions({ name: "AdminDashboard" });

const dateRange = ref<[string, string] | null>(null);
const quickRange = ref("");

function getQuickDateRange(range: string): [string, string] {
  const now = new Date();
  const y = now.getFullYear();
  const m = now.getMonth();
  const d = now.getDate();

  function fmt(date: Date) {
    return date.toISOString().slice(0, 10);
  }

  switch (range) {
    case "today": {
      const today = fmt(now);
      return [today, today];
    }
    case "week": {
      const dayOfWeek = now.getDay();
      const diff = dayOfWeek === 0 ? 6 : dayOfWeek - 1;
      const monday = new Date(y, m, d - diff);
      const sunday = new Date(y, m, d - diff + 6);
      return [fmt(monday), fmt(sunday)];
    }
    case "month": {
      const first = new Date(y, m, 1);
      const last = new Date(y, m + 1, 0);
      return [fmt(first), fmt(last)];
    }
    case "quarter": {
      const q = Math.floor(m / 3);
      const first = new Date(y, q * 3, 1);
      const last = new Date(y, (q + 1) * 3, 0);
      return [fmt(first), fmt(last)];
    }
    default:
      return [fmt(now), fmt(now)];
  }
}

function onQuickRangeChange(val: string) {
  if (val === "") {
    dateRange.value = null;
    return;
  }
  dateRange.value = getQuickDateRange(val);
  fetchAll();
}

function onDatePickerChange(val: [string, string] | null) {
  if (val) {
    quickRange.value = "";
    fetchAll();
  }
}

const dashboardData = reactive<DashboardData>({
  totalBookings: 0,
  venueStats: {},
  coachStats: {}
});

const venueLoading = ref(false);
const coachLoading = ref(false);
const studentLoading = ref(false);
const venueStats = ref<VenueStatsItem[]>([]);
const coachStats = ref<CoachStatsItem[]>([]);
const studentStats = ref<StudentStatsItem[]>([]);

const venueChartRef = ref<HTMLElement>();
const coachChartRef = ref<HTMLElement>();
const studentChartRef = ref<HTMLElement>();

let venueChart: echarts.ECharts | null = null;
let coachChart: echarts.ECharts | null = null;
let studentChart: echarts.ECharts | null = null;

function getParams() {
  const params: { startDate?: string; endDate?: string } = {};
  if (dateRange.value) {
    params.startDate = dateRange.value[0];
    params.endDate = dateRange.value[1];
  }
  return params;
}

async function fetchDashboard() {
  try {
    const res = await getDashboardApi(getParams());
    Object.assign(dashboardData, res);
  } catch (e: any) {
    ElMessage.error(e?.message || "获取看板数据失败");
  }
}

async function fetchVenueStats() {
  venueLoading.value = true;
  try {
    venueStats.value = await getVenueStatsApi(getParams());
    nextTick(() => renderVenueChart());
  } catch (e: any) {
    ElMessage.error(e?.message || "获取场地统计失败");
  } finally {
    venueLoading.value = false;
  }
}

async function fetchCoachStats() {
  coachLoading.value = true;
  try {
    coachStats.value = await getCoachStatsApi(getParams());
    nextTick(() => renderCoachChart());
  } catch (e: any) {
    ElMessage.error(e?.message || "获取教练统计失败");
  } finally {
    coachLoading.value = false;
  }
}

async function fetchStudentStats() {
  studentLoading.value = true;
  try {
    studentStats.value = await getStudentStatsApi(getParams());
    nextTick(() => renderStudentChart());
  } catch (e: any) {
    ElMessage.error(e?.message || "获取学生统计失败");
  } finally {
    studentLoading.value = false;
  }
}

function renderVenueChart() {
  if (!venueChartRef.value) return;
  if (!venueChart) {
    venueChart = echarts.init(venueChartRef.value);
  }
  venueChart.setOption({
    tooltip: { trigger: "axis" },
    grid: { left: "3%", right: "4%", bottom: "10%", containLabel: true },
    xAxis: {
      type: "category",
      data: venueStats.value.map(v => v.venueName),
      axisLabel: { fontSize: 12 }
    },
    yAxis: { type: "value", minInterval: 1 },
    series: [
      {
        type: "bar",
        data: venueStats.value.map(v => v.usageCount),
        itemStyle: { borderRadius: [4, 4, 0, 0], color: "#409EFF" },
        barWidth: "50%"
      }
    ]
  });
  venueChart.resize();
}

function renderCoachChart() {
  if (!coachChartRef.value) return;
  if (!coachChart) {
    coachChart = echarts.init(coachChartRef.value);
  }
  coachChart.setOption({
    tooltip: { trigger: "axis" },
    grid: { left: "3%", right: "4%", bottom: "10%", containLabel: true },
    xAxis: {
      type: "category",
      data: coachStats.value.map(v => v.coachName),
      axisLabel: { fontSize: 12 }
    },
    yAxis: { type: "value", minInterval: 1 },
    series: [
      {
        type: "bar",
        data: coachStats.value.map(v => v.classCount),
        itemStyle: { borderRadius: [4, 4, 0, 0], color: "#67C23A" },
        barWidth: "50%"
      }
    ]
  });
  coachChart.resize();
}

function renderStudentChart() {
  if (!studentChartRef.value) return;
  if (!studentChart) {
    studentChart = echarts.init(studentChartRef.value);
  }
  studentChart.setOption({
    tooltip: { trigger: "axis" },
    grid: { left: "3%", right: "4%", bottom: "15%", containLabel: true },
    xAxis: {
      type: "category",
      data: studentStats.value.map(v => v.studentName),
      axisLabel: {
        fontSize: 12,
        rotate: studentStats.value.length > 8 ? 30 : 0
      }
    },
    yAxis: { type: "value", minInterval: 1 },
    series: [
      {
        type: "bar",
        data: studentStats.value.map(v => v.classCount),
        itemStyle: { borderRadius: [4, 4, 0, 0], color: "#E6A23C" },
        barWidth: "40%"
      }
    ]
  });
  studentChart.resize();
}

function disposeCharts() {
  venueChart?.dispose();
  coachChart?.dispose();
  studentChart?.dispose();
  venueChart = null;
  coachChart = null;
  studentChart = null;
}

async function fetchAll() {
  disposeCharts();
  fetchDashboard();
  fetchVenueStats();
  fetchCoachStats();
  fetchStudentStats();
}

onMounted(() => {
  fetchAll();
  window.addEventListener("resize", () => {
    venueChart?.resize();
    coachChart?.resize();
    studentChart?.resize();
  });
});
</script>
