<template>
  <div class="p-6">
    <!-- 头像卡片 -->
    <el-card shadow="never" class="mb-4">
      <div class="flex items-center gap-6 py-4">
        <div class="relative group cursor-pointer" @click="triggerUpload">
          <el-avatar :size="80" :src="avatarUrl">
            <el-icon :size="36"><User /></el-icon>
          </el-avatar>
          <div
            class="absolute inset-0 bg-black/40 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
          >
            <el-icon color="#fff" :size="20"><Camera /></el-icon>
          </div>
        </div>
        <div>
          <div class="text-lg font-semibold">
            {{ userInfo.nickname || userInfo.username }}
          </div>
          <div class="text-gray-400 text-sm mt-1">教练</div>
        </div>
      </div>
    </el-card>

    <!-- 个人信息卡片 -->
    <el-card shadow="never">
      <template #header>
        <span class="font-semibold">个人信息</span>
      </template>

      <el-descriptions v-loading="loading" :column="1" border>
        <el-descriptions-item label="用户名">
          {{ userInfo.username || "--" }}
        </el-descriptions-item>
        <el-descriptions-item label="昵称">
          {{ userInfo.nickname || "--" }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号">
          {{ userInfo.phone || "--" }}
        </el-descriptions-item>
        <el-descriptions-item label="账号类型">
          <el-tag type="warning" size="small">教练</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 隐藏的文件上传 -->
    <input
      ref="fileInputRef"
      type="file"
      accept="image/*"
      class="hidden"
      @change="handleFileChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { User, Camera } from "@element-plus/icons-vue";
import { getUserInfoApi } from "@/api/user";
import { useUserStoreHook } from "@/store/modules/user";
import { setAvatar, getAvatar } from "@/utils/auth";

defineOptions({ name: "CoachInfo" });

const loading = ref(false);
const fileInputRef = ref<HTMLInputElement>();
const userStore = useUserStoreHook();

const userInfo = reactive({
  username: "",
  nickname: "",
  phone: ""
});

const avatarUrl = ref(getAvatar(userStore.userId) || "");

async function fetchInfo() {
  loading.value = true;
  try {
    const data = await getUserInfoApi();
    Object.assign(userInfo, data);
  } catch (e: any) {
    ElMessage.error(e?.message || "获取个人信息失败");
  } finally {
    loading.value = false;
  }
}

function triggerUpload() {
  fileInputRef.value?.click();
}

function handleFileChange(e: Event) {
  const input = e.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;

  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning("图片大小不能超过 2MB");
    return;
  }

  const reader = new FileReader();
  reader.onload = () => {
    const base64 = reader.result as string;
    avatarUrl.value = base64;
    setAvatar(userStore.userId, base64);
    ElMessage.success("头像已更新");
  };
  reader.readAsDataURL(file);
  input.value = "";
}

onMounted(() => {
  fetchInfo();
});
</script>
