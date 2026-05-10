<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import dayjs from "dayjs";
import { noticesData } from "./data";
import { emitter } from "@/utils/mitt";
import NoticeList from "./components/NoticeList.vue";
import BellIcon from "@iconify-icons/ep/bell";

const notices = ref(noticesData);
const activeKey = ref(noticesData[0]?.key);

const noticesNum = computed(() => {
  return notices.value.reduce((sum, tab) => sum + tab.list.length, 0);
});

const getLabel = computed(
  () => item =>
    item.name + (item.list.length > 0 ? `(${item.list.length})` : "")
);

onMounted(() => {
  emitter.on("websocketMessage", onWebSocketMessage);
});

onBeforeUnmount(() => {
  emitter.off("websocketMessage", onWebSocketMessage);
});

function onWebSocketMessage(msg: {
  title: string;
  description: string;
  type: string;
}) {
  const noticeTab = notices.value.find(t => t.key === "1");
  if (!noticeTab) return;
  noticeTab.list.unshift({
    avatar: "",
    title: msg.title,
    description: msg.description,
    datetime: dayjs().format("HH:mm:ss"),
    type: msg.type,
    status: msg.type as any
  });
  // 最多保留 50 条
  if (noticeTab.list.length > 50) {
    noticeTab.list.length = 50;
  }
  // 切换到通知 tab
  activeKey.value = "1";
}
</script>

<template>
  <el-dropdown trigger="click" placement="bottom-end">
    <span
      :class="[
        'dropdown-badge',
        'navbar-bg-hover',
        'select-none',
        Number(noticesNum) !== 0 && 'mr-[10px]'
      ]"
    >
      <el-badge :value="Number(noticesNum) === 0 ? '' : noticesNum" :max="99">
        <span class="header-notice-icon">
          <IconifyIconOffline :icon="BellIcon" />
        </span>
      </el-badge>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-tabs
          v-model="activeKey"
          :stretch="true"
          class="dropdown-tabs"
          :style="{ width: notices.length === 0 ? '200px' : '330px' }"
        >
          <el-empty
            v-if="notices.length === 0"
            description="暂无消息"
            :image-size="60"
          />
          <span v-else>
            <template v-for="item in notices" :key="item.key">
              <el-tab-pane :label="getLabel(item)" :name="`${item.key}`">
                <el-scrollbar max-height="330px">
                  <div class="noticeList-container">
                    <NoticeList :list="item.list" :emptyText="item.emptyText" />
                  </div>
                </el-scrollbar>
              </el-tab-pane>
            </template>
          </span>
        </el-tabs>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style lang="scss" scoped>
.dropdown-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 48px;
  cursor: pointer;

  .header-notice-icon {
    font-size: 18px;
  }
}

.dropdown-tabs {
  .noticeList-container {
    padding: 15px 24px 0;
  }

  :deep(.el-tabs__header) {
    margin: 0;
  }

  :deep(.el-tabs__nav-wrap)::after {
    height: 1px;
  }

  :deep(.el-tabs__nav-wrap) {
    padding: 0 36px;
  }
}
</style>
