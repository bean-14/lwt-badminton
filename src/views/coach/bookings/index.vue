<template>
  <div class="p-6">
    <!-- 筛选栏 -->
    <el-card shadow="never" class="mb-4">
      <el-form :inline="true" size="default">
        <el-form-item label="日期">
          <el-date-picker
            v-model="queryDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            placeholder="全部日期"
            clearable
            @change="fetchBookings"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchBookings">刷新</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 预约列表 -->
    <el-card shadow="never">
      <template #header>
        <span class="font-semibold">学生预约</span>
      </template>

      <el-table
        v-loading="loading"
        :data="bookingList"
        stripe
        empty-text="暂无预约"
      >
        <el-table-column prop="studentName" label="学生" min-width="100" />
        <el-table-column prop="venueName" label="场地" min-width="100" />
        <el-table-column prop="scheduleDate" label="日期" min-width="120" />
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type as any" size="small">
              {{ statusMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="预约时间" min-width="160">
          <template #default="{ row }">
            {{ row.createTime?.replace("T", " ") }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'pending'"
              type="success"
              size="small"
              @click="handleConfirm(row)"
            >
              确认完成
            </el-button>
            <span v-else class="text-gray-400 text-sm">--</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { getCoachBookingsApi, confirmBookingApi } from "@/api/coach";
import type { BookingVO } from "@/api/coach";

defineOptions({ name: "CoachBookings" });

const loading = ref(false);
const bookingList = ref<BookingVO[]>([]);
const queryDate = ref("");

const statusMap: Record<
  string,
  { label: string; type: "warning" | "success" | "info" | "danger" | "primary" }
> = {
  pending: { label: "待确认", type: "warning" },
  confirmed: { label: "已确认", type: "success" },
  cancelled: { label: "已取消", type: "info" }
};

async function fetchBookings() {
  loading.value = true;
  try {
    const params: any = {};
    if (queryDate.value) params.date = queryDate.value;
    bookingList.value = await getCoachBookingsApi(params);
  } catch (e: any) {
    ElMessage.error(e?.message || "获取预约列表失败");
  } finally {
    loading.value = false;
  }
}

async function handleConfirm(row: BookingVO) {
  try {
    await ElMessageBox.confirm(
      `确认学生 ${row.studentName} 已完成课程？确认后将扣除 1 课时。`,
      "确认预约",
      {
        type: "warning",
        confirmButtonText: "确认完成",
        cancelButtonText: "返回"
      }
    );
    await confirmBookingApi(row.id);
    ElMessage.success("已确认，课时已扣除");
    fetchBookings();
  } catch (e: any) {
    if (e?.message) ElMessage.error(e.message);
  }
}

onMounted(() => {
  fetchBookings();
});
</script>
