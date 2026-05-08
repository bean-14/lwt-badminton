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
        <el-table-column label="时间段" min-width="110">
          <template #default="{ row }">
            {{ row.startTime ? row.startTime.slice(0, 5) : "--" }} ~
            {{ row.endTime ? row.endTime.slice(0, 5) : "--" }}
          </template>
        </el-table-column>
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
        <el-table-column label="操作" min-width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'pending'"
              type="danger"
              size="small"
              @click="handleCancel(row)"
            >
              取消预约
            </el-button>
            <el-button
              v-if="row.status === 'confirmed'"
              type="warning"
              size="small"
              @click="handleLeave(row)"
            >
              请假
            </el-button>
            <span
              v-else-if="
                row.status === 'cancelled' ||
                row.status === 'rejected' ||
                row.status === 'leave' ||
                row.status === 'leave_pending' ||
                row.status === 'completed' ||
                row.status === 'no_confirm'
              "
              class="text-gray-400 text-sm"
              >--</span
            >
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

    <!-- 请假对话框 -->
    <el-dialog v-model="leaveDialogVisible" title="申请请假" width="420px">
      <div class="py-2">
        <div
          v-if="selectedBooking"
          class="text-sm text-gray-500 mb-3 space-y-1"
        >
          <div>
            预约日期：<span class="text-gray-700 font-medium">{{
              selectedBooking.scheduleDate
            }}</span>
          </div>
          <div>
            教练：<span class="text-gray-700 font-medium">{{
              selectedBooking.coachName
            }}</span>
          </div>
          <div>
            场地：<span class="text-gray-700 font-medium">{{
              selectedBooking.venueName
            }}</span>
          </div>
        </div>
        <el-alert
          v-if="isTodayLeave"
          title="当天请假需教练审批，同意后才退还课时"
          type="warning"
          :closable="false"
          show-icon
          class="mb-3"
        />
        <el-alert
          v-else
          title="提前请假，提交后将自动退还课时"
          type="success"
          :closable="false"
          show-icon
          class="mb-3"
        />
        <el-input
          v-model="leaveReason"
          type="textarea"
          placeholder="请输入请假原因"
          :rows="3"
          maxlength="200"
          show-word-limit
        />
      </div>
      <template #footer>
        <el-button @click="leaveDialogVisible = false">取消</el-button>
        <el-button
          type="warning"
          :loading="leaveLoading"
          :disabled="!leaveReason.trim()"
          @click="confirmLeave"
        >
          提交申请
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { WarningFilled } from "@element-plus/icons-vue";
import { getMyBookingsApi, cancelBookingApi, leaveApi } from "@/api/student";
import type { BookingVO } from "@/api/student";

defineOptions({ name: "StudentBookings" });

const loading = ref(false);
const bookingList = ref<BookingVO[]>([]);
const dialogVisible = ref(false);
const cancelLoading = ref(false);
const selectedBooking = ref<BookingVO | null>(null);
const leaveDialogVisible = ref(false);
const leaveLoading = ref(false);
const leaveReason = ref("");

const queryParams = reactive({
  date: ""
});

const isTodayLeave = computed(() => {
  if (!selectedBooking.value) return false;
  const today = new Date().toISOString().slice(0, 10);
  return selectedBooking.value.scheduleDate <= today;
});

const statusMap: Record<
  string,
  { label: string; type: "warning" | "success" | "info" | "danger" | "primary" }
> = {
  pending: { label: "待确认", type: "warning" },
  confirmed: { label: "已同意", type: "primary" },
  completed: { label: "已完成", type: "success" },
  cancelled: { label: "已取消", type: "info" },
  rejected: { label: "教练已拒绝", type: "danger" },
  no_confirm: { label: "教练未确认", type: "danger" },
  leave: { label: "已请假", type: "danger" },
  leave_pending: { label: "请假待审批", type: "warning" }
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

function handleLeave(row: BookingVO) {
  selectedBooking.value = row;
  leaveReason.value = "";
  leaveDialogVisible.value = true;
}

async function confirmLeave() {
  if (!selectedBooking.value || !leaveReason.value.trim()) return;
  leaveLoading.value = true;
  try {
    await leaveApi(selectedBooking.value.id, leaveReason.value.trim());
    ElMessage.success("请假申请已提交");
    leaveDialogVisible.value = false;
    fetchBookings();
  } catch (e: any) {
    ElMessage.error(e?.message || "请假失败");
  } finally {
    leaveLoading.value = false;
  }
}

onMounted(() => {
  fetchBookings();
});
</script>
