<template>
  <div class="p-6">
    <el-card shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-semibold">场地管理</span>
          <el-button type="primary" size="default" @click="showAddDialog">
            <template #icon><Plus /></template>
            新增场地
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="venueList"
        stripe
        empty-text="暂无场地"
      >
        <el-table-column prop="name" label="场地名称" min-width="120" />
        <el-table-column prop="location" label="位置" min-width="160" />
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              :loading="row._switchLoading"
              @change="
                (val: boolean | string | number) => toggleStatus(row, val)
              "
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)"
              >编辑</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑场地' : '新增场地'"
      width="450px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="场地名称" prop="name">
          <el-input
            v-model="form.name"
            placeholder="如：1号场"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="位置描述" prop="location">
          <el-input
            v-model="form.location"
            placeholder="如：A栋1楼"
            maxlength="100"
          />
        </el-form-item>
        <el-form-item v-if="isEdit" label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">可用</el-radio>
            <el-radio :value="0">不可用</el-radio>
          </el-radio-group>
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
import { getVenuesApi, addVenueApi, updateVenueApi } from "@/api/admin";
import type { Venue } from "@/api/admin";
import type { FormInstance } from "element-plus";

defineOptions({ name: "AdminVenues" });

const loading = ref(false);
const venueList = ref<(Venue & { _switchLoading?: boolean })[]>([]);
const dialogVisible = ref(false);
const submitLoading = ref(false);
const isEdit = ref(false);
const formRef = ref<FormInstance>();

const form = reactive({
  id: undefined as number | undefined,
  name: "",
  location: "",
  status: 1 as number | undefined
});

const rules = {
  name: [{ required: true, message: "请输入场地名称", trigger: "blur" }]
};

async function fetchVenues() {
  loading.value = true;
  try {
    venueList.value = await getVenuesApi();
  } catch (e: any) {
    ElMessage.error(e?.message || "获取场地列表失败");
  } finally {
    loading.value = false;
  }
}

function showAddDialog() {
  isEdit.value = false;
  form.id = undefined;
  form.name = "";
  form.location = "";
  form.status = 1;
  dialogVisible.value = true;
}

function showEditDialog(row: Venue) {
  isEdit.value = true;
  form.id = row.id;
  form.name = row.name;
  form.location = row.location || "";
  form.status = row.status ?? 1;
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!formRef.value) return;
  await formRef.value.validate(async valid => {
    if (!valid) return;
    submitLoading.value = true;
    try {
      if (isEdit.value) {
        await updateVenueApi({
          id: form.id,
          name: form.name,
          location: form.location,
          status: form.status
        });
        ElMessage.success("更新成功");
      } else {
        await addVenueApi({ name: form.name, location: form.location });
        ElMessage.success("新增成功");
      }
      dialogVisible.value = false;
      fetchVenues();
    } catch (e: any) {
      ElMessage.error(e?.message || "操作失败");
    } finally {
      submitLoading.value = false;
    }
  });
}

function toggleStatus(
  row: Venue & { _switchLoading?: boolean },
  raw: boolean | string | number
) {
  handleStatusChange(row, !!raw);
}

async function handleStatusChange(
  row: Venue & { _switchLoading?: boolean },
  val: boolean
) {
  row._switchLoading = true;
  try {
    await updateVenueApi({
      id: row.id,
      name: row.name,
      location: row.location || "",
      status: val ? 1 : 0
    });
    row.status = val ? 1 : 0;
    ElMessage.success(val ? "已启用" : "已禁用");
  } catch (e: any) {
    ElMessage.error(e?.message || "操作失败");
  } finally {
    row._switchLoading = false;
  }
}

onMounted(() => {
  fetchVenues();
});
</script>
