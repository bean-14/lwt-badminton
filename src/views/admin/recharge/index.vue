<template>
  <div class="p-6">
    <el-row :gutter="20">
      <!-- 学生列表 -->
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="flex items-center justify-between">
              <span class="font-semibold">学生列表</span>
              <el-button type="primary" @click="fetchStudents">刷新</el-button>
            </div>
          </template>

          <el-table
            v-loading="loading"
            :data="studentList"
            stripe
            empty-text="暂无学生"
            highlight-current-row
            @row-click="handleRowClick"
          >
            <el-table-column prop="username" label="用户名" min-width="100" />
            <el-table-column prop="nickname" label="昵称" min-width="100" />
            <el-table-column prop="phone" label="手机号" min-width="130" />
            <el-table-column
              prop="remainingHours"
              label="剩余课时"
              min-width="90"
            >
              <template #default="{ row }">
                <el-tag
                  :type="row.remainingHours > 0 ? 'success' : 'danger'"
                  size="small"
                >
                  {{ row.remainingHours }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 充值面板 -->
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <span class="font-semibold">充值操作</span>
          </template>

          <div v-if="!selectedStudent" class="text-center text-gray-400 py-8">
            请先选择一名学生
          </div>

          <div v-else>
            <div class="mb-4 p-3 bg-gray-50 rounded">
              <div class="text-sm text-gray-500">选中学生</div>
              <div class="font-semibold text-base mt-1">
                {{ selectedStudent.nickname || selectedStudent.username }}
              </div>
              <div class="text-sm text-gray-400">
                当前课时：{{ selectedStudent.remainingHours }}
              </div>
            </div>

            <el-form :model="form" label-width="0" size="default">
              <el-form-item>
                <el-input-number
                  v-model="form.hours"
                  :min="1"
                  :max="999"
                  :precision="0"
                  style="width: 100%"
                  placeholder="充值课时数"
                />
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  :loading="submitLoading"
                  style="width: 100%"
                  @click="handleRecharge"
                >
                  确认充值 {{ form.hours }} 课时
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { getStudentsApi, rechargeApi } from "@/api/admin";
import type { UserInfo } from "@/api/admin";

defineOptions({ name: "AdminRecharge" });

const loading = ref(false);
const studentList = ref<UserInfo[]>([]);
const selectedStudent = ref<UserInfo | null>(null);
const submitLoading = ref(false);

const form = reactive({
  hours: 10
});

async function fetchStudents() {
  loading.value = true;
  try {
    studentList.value = await getStudentsApi();
  } catch (e: any) {
    ElMessage.error(e?.message || "获取学生列表失败");
  } finally {
    loading.value = false;
  }
}

function handleRowClick(row: UserInfo) {
  selectedStudent.value = row;
  form.hours = 10;
}

async function handleRecharge() {
  if (!selectedStudent.value) return;
  submitLoading.value = true;
  try {
    await rechargeApi({
      userId: selectedStudent.value.id,
      hours: form.hours
    });
    ElMessage.success(
      `已为 ${selectedStudent.value.nickname || selectedStudent.value.username} 充值 ${form.hours} 课时`
    );
    selectedStudent.value.remainingHours += form.hours;
    fetchStudents();
  } catch (e: any) {
    ElMessage.error(e?.message || "充值失败");
  } finally {
    submitLoading.value = false;
  }
}

onMounted(() => {
  fetchStudents();
});
</script>
