<template>
  <div class="p-6">
    <el-card shadow="never">
      <template #header>
        <span class="font-semibold">上课历史</span>
      </template>

      <el-table
        v-loading="loading"
        :data="historyList"
        stripe
        empty-text="暂无上课记录"
      >
        <el-table-column prop="coachName" label="教练" min-width="100" />
        <el-table-column prop="venueName" label="场地" min-width="100" />
        <el-table-column prop="scheduleDate" label="上课日期" min-width="120" />
        <el-table-column label="状态" min-width="80">
          <template #default>
            <el-tag type="success" size="small">已完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="confirmTime" label="确认时间" min-width="160">
          <template #default="{ row }">
            {{ row.confirmTime?.replace("T", " ") || "--" }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { getHistoryApi } from "@/api/student";
import type { BookingVO } from "@/api/student";

defineOptions({ name: "StudentHistory" });

const loading = ref(false);
const historyList = ref<BookingVO[]>([]);

async function fetchHistory() {
  loading.value = true;
  try {
    historyList.value = await getHistoryApi();
  } catch (e: any) {
    ElMessage.error(e?.message || "获取上课历史失败");
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetchHistory();
});
</script>
