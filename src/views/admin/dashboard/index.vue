<template>
  <div class="p-6">
    <!-- 筛选栏 -->
    <el-card shadow="never" class="mb-4">
      <el-form :inline="true" size="default">
        <el-form-item label="统计范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="fetchDashboard"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchDashboard">刷新</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 总预约数 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="text-center py-6">
            <div class="text-gray-400 text-sm mb-2">已完成预约</div>
            <div class="text-5xl font-bold text-primary">
              <ReNormalCountTo :endVal="dashboard.totalBookings" />
            </div>
            <div class="text-gray-400 text-xs mt-1">总计</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 场地统计 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="font-semibold">场地使用统计</span>
          </template>
          <el-table :data="venueStatsList" stripe empty-text="暂无数据">
            <el-table-column prop="name" label="场地" min-width="120" />
            <el-table-column prop="count" label="预约次数" min-width="80" />
            <el-table-column label="占比" min-width="120">
              <template #default="{ row }">
                <el-progress
                  :percentage="venuePercentage(row.count)"
                  :stroke-width="16"
                  striped
                  striped-flow
                />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 教练统计 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="font-semibold">教练授课统计</span>
          </template>
          <el-table :data="coachStatsList" stripe empty-text="暂无数据">
            <el-table-column prop="name" label="教练" min-width="120" />
            <el-table-column prop="count" label="授课次数" min-width="80" />
            <el-table-column label="占比" min-width="120">
              <template #default="{ row }">
                <el-progress
                  :percentage="coachPercentage(row.count)"
                  :stroke-width="16"
                  color="#67c23a"
                  striped
                  striped-flow
                />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { getDashboardApi } from "@/api/admin";
import { ReNormalCountTo } from "@/components/ReCountTo";

defineOptions({ name: "AdminDashboard" });

const dateRange = ref<[string, string] | null>(null);
const dashboard = reactive({
  totalBookings: 0,
  venueStats: {} as Record<string, number>,
  coachStats: {} as Record<string, number>
});

const venueStatsList = computed(() =>
  Object.entries(dashboard.venueStats).map(([name, count]) => ({ name, count }))
);

const coachStatsList = computed(() =>
  Object.entries(dashboard.coachStats).map(([name, count]) => ({ name, count }))
);

const maxVenueCount = computed(() =>
  Math.max(1, ...Object.values(dashboard.venueStats))
);

const maxCoachCount = computed(() =>
  Math.max(1, ...Object.values(dashboard.coachStats))
);

function venuePercentage(count: number) {
  return Math.round((count / maxVenueCount.value) * 100);
}

function coachPercentage(count: number) {
  return Math.round((count / maxCoachCount.value) * 100);
}

async function fetchDashboard() {
  try {
    const params: any = {};
    if (dateRange.value?.[0]) params.startDate = dateRange.value[0];
    if (dateRange.value?.[1]) params.endDate = dateRange.value[1];
    const data = await getDashboardApi(params);
    dashboard.totalBookings = data.totalBookings;
    dashboard.venueStats = data.venueStats || {};
    dashboard.coachStats = data.coachStats || {};
  } catch (e: any) {
    ElMessage.error(e?.message || "获取数据失败");
  }
}

onMounted(() => {
  fetchDashboard();
});
</script>
