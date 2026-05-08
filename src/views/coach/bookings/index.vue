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
        <el-table-column label="请假原因" min-width="140">
          <template #default="{ row }">
            <span
              v-if="row.status === 'leave' || row.status === 'leave_pending'"
            >
              <el-tooltip
                v-if="row.leaveReason"
                :content="row.leaveReason"
                placement="top"
              >
                <span
                  class="text-gray-500 text-sm cursor-pointer truncate inline-block max-w-[120px]"
                >
                  {{ row.leaveReason }}
                </span>
              </el-tooltip>
              <span v-else class="text-gray-400 text-sm">--</span>
            </span>
            <span v-else class="text-gray-400 text-sm">--</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'pending'"
              type="success"
              size="small"
              @click="handleConfirm(row)"
            >
              接收
            </el-button>
            <el-button
              v-if="row.status === 'pending'"
              type="danger"
              size="small"
              @click="handleReject(row)"
            >
              拒绝
            </el-button>
            <el-button
              v-if="row.status === 'leave_pending'"
              type="success"
              size="small"
              :loading="leaveLoading === row.id && leaveAction === 'approve'"
              @click="handleLeave(row.id, 'approve')"
            >
              同意请假
            </el-button>
            <el-button
              v-if="row.status === 'leave_pending'"
              type="danger"
              size="small"
              :loading="leaveLoading === row.id && leaveAction === 'reject'"
              @click="handleLeave(row.id, 'reject')"
            >
              拒绝请假
            </el-button>
            <span
              v-if="
                row.status === 'confirmed' ||
                row.status === 'completed' ||
                row.status === 'cancelled' ||
                row.status === 'rejected' ||
                row.status === 'leave' ||
                row.status === 'no_confirm'
              "
              class="text-gray-400 text-sm"
              >--</span
            >
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  getCoachBookingsApi,
  confirmBookingApi,
  rejectBookingApi,
  handleLeaveApi
} from "@/api/coach";
import type { BookingVO } from "@/api/coach";

defineOptions({ name: "CoachBookings" });

const loading = ref(false);
const bookingList = ref<BookingVO[]>([]);
const queryDate = ref("");
const leaveLoading = ref<number | null>(null);
const leaveAction = ref<"approve" | "reject">("approve");

const statusMap: Record<
  string,
  { label: string; type: "warning" | "success" | "info" | "danger" | "primary" }
> = {
  pending: { label: "待确认", type: "warning" },
  confirmed: { label: "已接收", type: "primary" },
  completed: { label: "已完成", type: "success" },
  cancelled: { label: "已取消", type: "info" },
  rejected: { label: "已拒绝", type: "danger" },
  no_confirm: { label: "未确认", type: "danger" },
  leave: { label: "已请假", type: "danger" },
  leave_pending: { label: "请假待处理", type: "warning" }
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
      `确认接收学生 ${row.studentName} 的预约？接收后将扣除 1 课时。`,
      "接收预约",
      {
        type: "warning",
        confirmButtonText: "确认接收",
        cancelButtonText: "返回"
      }
    );
    await confirmBookingApi(row.id);
    ElMessage.success("已接收预约，课时已扣除");
    fetchBookings();
  } catch (e: any) {
    if (e?.message) ElMessage.error(e.message);
  }
}

async function handleReject(row: BookingVO) {
  try {
    await ElMessageBox.confirm(
      `确定拒绝学生 ${row.studentName} 的预约吗？`,
      "拒绝预约",
      {
        type: "warning",
        confirmButtonText: "确定拒绝",
        cancelButtonText: "返回"
      }
    );
    await rejectBookingApi(row.id);
    ElMessage.success("已拒绝预约");
    fetchBookings();
  } catch (e: any) {
    if (e?.message) ElMessage.error(e.message);
  }
}

async function handleLeave(bookingId: number, action: "approve" | "reject") {
  const text =
    action === "approve" ? "同意请假并退还课时" : "拒绝请假，不退还课时";
  try {
    await ElMessageBox.confirm(`确定${text}吗？`, "处理请假", {
      type: "warning",
      confirmButtonText: "确定",
      cancelButtonText: "返回"
    });
    leaveLoading.value = bookingId;
    leaveAction.value = action;
    await handleLeaveApi(bookingId, action);
    ElMessage.success(
      action === "approve" ? "已同意请假，课时已退还" : "已拒绝请假"
    );
    fetchBookings();
  } catch (e: any) {
    if (e?.message) ElMessage.error(e.message);
  } finally {
    leaveLoading.value = null;
  }
}

onMounted(() => {
  fetchBookings();
});
</script>
