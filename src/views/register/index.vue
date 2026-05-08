<script setup lang="ts">
import { useRouter } from "vue-router";
import { message } from "@/utils/message";
import type { FormInstance } from "element-plus";
import { ref, reactive } from "vue";
import { registerApi } from "@/api/user";
import { bg, illustration } from "./../login/utils/static";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import { useDataThemeChange } from "@/layout/hooks/useDataThemeChange";
import dayIcon from "@/assets/svg/day.svg?component";
import darkIcon from "@/assets/svg/dark.svg?component";
import User from "@iconify-icons/ri/user-3-fill";
import Lock from "@iconify-icons/ri/lock-fill";
import Phone from "@iconify-icons/ri/phone-fill";
import UserSmile from "@iconify-icons/ri/user-smile-fill";

defineOptions({ name: "Register" });

const router = useRouter();
const loading = ref(false);
const formRef = ref<FormInstance>();
const { dataTheme, dataThemeChange } = useDataThemeChange();
dataThemeChange(localStorage.getItem("dataTheme") || "light");

const form = reactive({
  username: "",
  password: "",
  confirmPassword: "",
  nickname: "",
  phone: "",
  userType: "student"
});

const validateConfirm = (
  rule: any,
  value: string,
  callback: (e?: Error) => void
) => {
  if (value !== form.password) {
    callback(new Error("两次输入的密码不一致"));
  } else {
    callback();
  }
};

const rules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 2, max: 50, message: "用户名长度2-50个字符", trigger: "blur" }
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码至少6位", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    { validator: validateConfirm, trigger: "blur" }
  ],
  userType: [{ required: true, message: "请选择用户类型", trigger: "change" }]
};

async function handleRegister() {
  if (!formRef.value) return;
  await formRef.value.validate(async valid => {
    if (!valid) return;
    loading.value = true;
    try {
      await registerApi({
        username: form.username,
        password: form.password,
        nickname: form.nickname || undefined,
        phone: form.phone || undefined,
        userType: form.userType
      });
      message("注册成功，请登录", { type: "success" });
      router.push("/login");
    } catch (err: any) {
      message(err?.message || "注册失败", { type: "error" });
    } finally {
      loading.value = false;
    }
  });
}
</script>

<template>
  <div class="select-none">
    <img :src="bg" class="wave" />
    <div class="flex-c absolute right-5 top-3">
      <el-switch
        v-model="dataTheme"
        inline-prompt
        :active-icon="dayIcon"
        :inactive-icon="darkIcon"
        @change="dataThemeChange"
      />
    </div>
    <div class="login-container">
      <div class="img">
        <component :is="illustration" />
      </div>
      <div class="login-box">
        <div class="login-form">
          <h2 class="text-center text-2xl font-bold mb-6 outline-none">
            注册账号
          </h2>

          <el-form ref="formRef" :model="form" :rules="rules" size="large">
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                clearable
                placeholder="用户名"
                :prefix-icon="useRenderIcon(User)"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                clearable
                show-password
                placeholder="密码（至少6位）"
                :prefix-icon="useRenderIcon(Lock)"
              />
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                clearable
                show-password
                placeholder="确认密码"
                :prefix-icon="useRenderIcon(Lock)"
              />
            </el-form-item>

            <el-form-item prop="nickname">
              <el-input
                v-model="form.nickname"
                clearable
                placeholder="昵称（选填）"
                :prefix-icon="useRenderIcon(UserSmile)"
              />
            </el-form-item>

            <el-form-item prop="phone">
              <el-input
                v-model="form.phone"
                clearable
                placeholder="手机号（选填）"
                :prefix-icon="useRenderIcon(Phone)"
              />
            </el-form-item>

            <el-form-item prop="userType">
              <el-radio-group v-model="form.userType" class="w-full">
                <el-radio value="student" class="mr-8">学生</el-radio>
                <el-radio value="coach">教练</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-button
              class="w-full"
              size="default"
              type="primary"
              :loading="loading"
              @click="handleRegister"
            >
              注册
            </el-button>

            <div class="text-center mt-4">
              <span class="text-gray-400 text-sm">已有账号？</span>
              <el-button text type="primary" @click="router.push('/login')">
                去登录
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url("@/style/login.css");
</style>
