<template>
  <div class="p-6">
    <!-- 筛选栏 -->
    <el-card shadow="never" class="mb-4">
      <el-form :inline="true" :model="queryParams" size="default">
        <el-form-item label="日期">
          <el-date-picker
            v-model="queryParams.date"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        <el-form-item label="教练">
          <el-select
            v-model="queryParams.coachId"
            placeholder="全部教练"
            clearable
            style="width: 160px"
          >
            <el-option
              v-for="coach in coachList"
              :key="coach.id"
              :label="coach.nickname || coach.username"
              :value="coach.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 时段列表 -->
    <el-card shadow="never">
      <template #header>
        <span class="font-semibold">可预约时段</span>
      </template>

      <el-table
        v-loading="loading"
        :data="scheduleList"
        stripe
        empty-text="暂无可用时段"
      >
        <el-table-column prop="coachName" label="教练" min-width="100" />
        <el-table-column prop="venueName" label="场地" min-width="100" />
        <el-table-column prop="scheduleDate" label="日期" min-width="120" />
        <el-table-column prop="startTime" label="开始时间" min-width="100" />
        <el-table-column prop="endTime" label="结束时间" min-width="100" />
        <el-table-column label="状态" min-width="80">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 ? 'success' : 'danger'"
              size="small"
            >
              {{ row.status === 1 ? "可预约" : "已满" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              :disabled="row.status !== 1"
              @click="handleBook(row)"
            >
              预约
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 预约确认对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="确认预约"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="text-center py-4">
        <el-icon class="text-4xl text-primary mb-3" :size="40">
          <WarningFilled />
        </el-icon>
        <p class="text-base">确认预约以下时段？</p>
        <div class="mt-3 text-gray-500">
          <p>教练：{{ selectedSchedule?.coachName }}</p>
          <p>场地：{{ selectedSchedule?.venueName }}</p>
          <p>日期：{{ selectedSchedule?.scheduleDate }}</p>
          <p>
            时间：{{ selectedSchedule?.startTime }} -
            {{ selectedSchedule?.endTime }}
          </p>
        </div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="bookingLoading"
          @click="confirmBook"
        >
          确认预约
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { WarningFilled } from "@element-plus/icons-vue";
import { getSchedulesApi, bookApi, getCoachesApi } from "@/api/student";
import type { ScheduleVO, CoachInfo } from "@/api/student";

defineOptions({ name: "StudentSchedules" });

const loading = ref(false);
const scheduleList = ref<ScheduleVO[]>([]);
const coachList = ref<CoachInfo[]>([]);
const dialogVisible = ref(false);
const bookingLoading = ref(false);
const selectedSchedule = ref<ScheduleVO | null>(null);

const queryParams = reactive({
  date: "",
  coachId: undefined as number | undefined
});

// 默认今天
queryParams.date = new Date().toISOString().slice(0, 10);

function disabledDate(time: Date) {
  return time < new Date(new Date().toDateString());
}

async function fetchSchedules() {
  loading.value = true;
  try {
    const params: any = {};
    if (queryParams.date) params.date = queryParams.date;
    if (queryParams.coachId) params.coachId = queryParams.coachId;
    scheduleList.value = await getSchedulesApi(params);
  } catch (e: any) {
    ElMessage.error(e?.message || "获取时段列表失败");
  } finally {
    loading.value = false;
  }
}

async function fetchCoaches() {
  try {
    coachList.value = await getCoachesApi();
  } catch {
    // 静默失败
  }
}

function handleSearch() {
  fetchSchedules();
}

function handleReset() {
  queryParams.date = new Date().toISOString().slice(0, 10);
  queryParams.coachId = undefined;
  fetchSchedules();
}

function handleBook(row: ScheduleVO) {
  selectedSchedule.value = row;
  dialogVisible.value = true;
}

async function confirmBook() {
  if (!selectedSchedule.value) return;
  bookingLoading.value = true;
  try {
    await bookApi(selectedSchedule.value.id);
    ElMessage.success("预约成功，等待教练确认");
    dialogVisible.value = false;
    fetchSchedules();
  } catch (e: any) {
    ElMessage.error(e?.message || "预约失败");
  } finally {
    bookingLoading.value = false;
  }
}

onMounted(() => {
  fetchCoaches();
  fetchSchedules();
});
</script>
