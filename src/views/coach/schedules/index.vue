<template>
  <div class="p-6">
    <!-- 操作栏 -->
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
            @change="fetchSchedules"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="showAddDialog">
            <template #icon><Plus /></template>
            新增排课
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 排课列表 -->
    <el-card shadow="never">
      <template #header>
        <span class="font-semibold">我的排课</span>
      </template>

      <el-table
        v-loading="loading"
        :data="scheduleList"
        stripe
        empty-text="暂无排课"
      >
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
              {{ row.status === 1 ? "可用" : "不可用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增排课对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="新增排课"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="90px"
        size="default"
      >
        <el-form-item label="场地" prop="venueId">
          <el-select
            v-model="form.venueId"
            placeholder="选择场地"
            style="width: 100%"
          >
            <el-option
              v-for="venue in venueList"
              :key="venue.id"
              :label="venue.name"
              :value="venue.id!"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="scheduleDate">
          <el-date-picker
            v-model="form.scheduleDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            style="width: 100%"
            :disabled-date="d => d < new Date(new Date().toDateString())"
          />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker
            v-model="form.startTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker
            v-model="form.endTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          @click="handleSubmit"
        >
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import {
  addScheduleApi,
  getMySchedulesApi,
  deleteScheduleApi
} from "@/api/coach";
import { getVenuesApi } from "@/api/admin";
import { useUserStoreHook } from "@/store/modules/user";
import type { ScheduleVO } from "@/api/coach";
import type { Venue } from "@/api/admin";
import type { FormInstance } from "element-plus";

defineOptions({ name: "CoachSchedules" });

const loading = ref(false);
const scheduleList = ref<ScheduleVO[]>([]);
const venueList = ref<Venue[]>([]);
const queryDate = ref("");
const dialogVisible = ref(false);
const submitLoading = ref(false);
const formRef = ref<FormInstance>();

const form = reactive({
  venueId: null as number | null,
  scheduleDate: "",
  startTime: "",
  endTime: ""
});

const rules = {
  venueId: [{ required: true, message: "请选择场地", trigger: "change" }],
  scheduleDate: [{ required: true, message: "请选择日期", trigger: "change" }],
  startTime: [{ required: true, message: "请选择开始时间", trigger: "change" }],
  endTime: [{ required: true, message: "请选择结束时间", trigger: "change" }]
};

async function fetchSchedules() {
  loading.value = true;
  try {
    const params: any = {};
    if (queryDate.value) params.date = queryDate.value;
    scheduleList.value = await getMySchedulesApi(params);
  } catch (e: any) {
    ElMessage.error(e?.message || "获取排课失败");
  } finally {
    loading.value = false;
  }
}

async function fetchVenues() {
  try {
    venueList.value = await getVenuesApi();
  } catch {
    // 静默失败
  }
}

function showAddDialog() {
  form.venueId = null;
  form.scheduleDate = "";
  form.startTime = "";
  form.endTime = "";
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!formRef.value) return;
  await formRef.value.validate(async valid => {
    if (!valid) return;
    submitLoading.value = true;
    try {
      const userStore = useUserStoreHook();
      await addScheduleApi({
        coachId: userStore.userId,
        venueId: form.venueId!,
        scheduleDate: form.scheduleDate,
        startTime: form.startTime,
        endTime: form.endTime
      });
      ElMessage.success("排课成功");
      dialogVisible.value = false;
      fetchSchedules();
    } catch (e: any) {
      ElMessage.error(e?.message || "排课失败");
    } finally {
      submitLoading.value = false;
    }
  });
}

async function handleDelete(row: ScheduleVO) {
  try {
    await ElMessageBox.confirm(
      "确定删除该排课？已有预约的时段无法删除。",
      "提示",
      {
        type: "warning"
      }
    );
    await deleteScheduleApi(row.id);
    ElMessage.success("已删除");
    fetchSchedules();
  } catch (e: any) {
    if (e?.message) ElMessage.error(e.message);
  }
}

onMounted(() => {
  fetchVenues();
  fetchSchedules();
});
</script>
