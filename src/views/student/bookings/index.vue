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
            placeholder="全部日期"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchBookings">查询</el-button>
          <el-button
            @click="
              queryParams.date = '';
              fetchBookings();
            "
            >重置</el-button
          >
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 预约列表 -->
    <el-card shadow="never">
      <template #header>
        <span class="font-semibold">我的预约</span>
      </template>

      <el-table
        v-loading="loading"
        :data="bookingList"
        stripe
        empty-text="暂无预约记录"
      >
        <el-table-column prop="coachName" label="教练" min-width="100" />
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
              type="danger"
              size="small"
              @click="handleCancel(row)"
            >
              取消预约
            </el-button>
            <span v-else class="text-gray-400 text-sm">--</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 取消确认对话框 -->
    <el-dialog v-model="dialogVisible" title="取消预约" width="380px">
      <div class="text-center py-4">
        <el-icon class="text-4xl text-warning mb-3" :size="40">
          <WarningFilled />
        </el-icon>
        <p>确定取消该预约吗？</p>
        <p class="text-gray-400 text-sm mt-2">取消后无法恢复</p>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">返回</el-button>
        <el-button
          type="danger"
          :loading="cancelLoading"
          @click="confirmCancel"
        >
          确认取消
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { WarningFilled } from "@element-plus/icons-vue";
import { getMyBookingsApi, cancelBookingApi } from "@/api/student";
import type { BookingVO } from "@/api/student";

defineOptions({ name: "StudentBookings" });

const loading = ref(false);
const bookingList = ref<BookingVO[]>([]);
const dialogVisible = ref(false);
const cancelLoading = ref(false);
const selectedBooking = ref<BookingVO | null>(null);

const queryParams = reactive({
  date: ""
});

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
    if (queryParams.date) params.date = queryParams.date;
    bookingList.value = await getMyBookingsApi(params);
  } catch (e: any) {
    ElMessage.error(e?.message || "获取预约记录失败");
  } finally {
    loading.value = false;
  }
}

function handleCancel(row: BookingVO) {
  selectedBooking.value = row;
  dialogVisible.value = true;
}

async function confirmCancel() {
  if (!selectedBooking.value) return;
  cancelLoading.value = true;
  try {
    await cancelBookingApi(selectedBooking.value.id);
    ElMessage.success("预约已取消");
    dialogVisible.value = false;
    fetchBookings();
  } catch (e: any) {
    ElMessage.error(e?.message || "取消失败");
  } finally {
    cancelLoading.value = false;
  }
}

onMounted(() => {
  fetchBookings();
});
</script>
