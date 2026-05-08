<template>
  <div class="p-6">
    <el-card shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-semibold">用户管理</span>
          <el-button type="primary" size="default" @click="showAddDialog">
            <template #icon><Plus /></template>
            新增用户
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="fetchUsers">
        <el-tab-pane label="教练列表" name="coach">
          <el-table
            v-loading="loading"
            :data="userList"
            stripe
            empty-text="暂无教练"
          >
            <el-table-column prop="username" label="用户名" min-width="100" />
            <el-table-column prop="nickname" label="昵称" min-width="100" />
            <el-table-column prop="phone" label="手机号" min-width="130" />
            <el-table-column label="状态" min-width="80">
              <template #default="{ row }">
                <el-tag
                  :type="row.status === 1 ? 'success' : 'danger'"
                  size="small"
                >
                  {{ row.status === 1 ? "正常" : "禁用" }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="学生列表" name="student">
          <el-table
            v-loading="loading"
            :data="userList"
            stripe
            empty-text="暂无学生"
          >
            <el-table-column prop="username" label="用户名" min-width="100" />
            <el-table-column prop="nickname" label="昵称" min-width="100" />
            <el-table-column prop="phone" label="手机号" min-width="130" />
            <el-table-column
              prop="remainingHours"
              label="剩余课时"
              min-width="90"
            />
            <el-table-column label="状态" min-width="80">
              <template #default="{ row }">
                <el-tag
                  :type="row.status === 1 ? 'success' : 'danger'"
                  size="small"
                >
                  {{ row.status === 1 ? "正常" : "禁用" }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 新增用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="新增用户"
      width="450px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户类型" prop="userType">
          <el-radio-group v-model="form.userType">
            <el-radio value="coach">教练</el-radio>
            <el-radio value="student">学生</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="登录用户名"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="登录密码"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input
            v-model="form.nickname"
            placeholder="显示名称"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="手机号码"
            maxlength="20"
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
import { ElMessage } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import { getCoachesApi, getStudentsApi, addUserApi } from "@/api/admin";
import type { UserInfo } from "@/api/admin";
import type { FormInstance } from "element-plus";

defineOptions({ name: "AdminUsers" });

const loading = ref(false);
const activeTab = ref("coach");
const userList = ref<UserInfo[]>([]);
const dialogVisible = ref(false);
const submitLoading = ref(false);
const formRef = ref<FormInstance>();

const form = reactive({
  userType: "student" as "student" | "coach",
  username: "",
  password: "",
  nickname: "",
  phone: ""
});

const rules = {
  userType: [{ required: true, message: "请选择用户类型", trigger: "change" }],
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }]
};

async function fetchUsers() {
  loading.value = true;
  try {
    if (activeTab.value === "coach") {
      userList.value = await getCoachesApi();
    } else {
      userList.value = await getStudentsApi();
    }
  } catch (e: any) {
    ElMessage.error(e?.message || "获取用户列表失败");
  } finally {
    loading.value = false;
  }
}

function showAddDialog() {
  form.userType = "student";
  form.username = "";
  form.password = "";
  form.nickname = "";
  form.phone = "";
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!formRef.value) return;
  await formRef.value.validate(async valid => {
    if (!valid) return;
    submitLoading.value = true;
    try {
      await addUserApi({
        username: form.username,
        password: form.password,
        nickname: form.nickname || undefined,
        phone: form.phone || undefined,
        userType: form.userType
      });
      ElMessage.success("新增成功");
      dialogVisible.value = false;
      fetchUsers();
    } catch (e: any) {
      ElMessage.error(e?.message || "新增失败");
    } finally {
      submitLoading.value = false;
    }
  });
}

onMounted(() => {
  fetchUsers();
});
</script>
