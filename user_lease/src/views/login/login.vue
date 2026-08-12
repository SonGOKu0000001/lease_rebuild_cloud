<template>
  <div class="main-container h-[100vh]">
    <div class="h-[30vh] flex flex-col justify-center items-center">
      <van-image round width="30vw" height="30vw" :src="defaultAvatarUrl" />
    </div>
    <div>
      <van-form ref="formRef">
        <van-cell-group inset>
          <!-- 邮箱 -->
          <van-field
            v-model.trim="loginInfo.email"
            border
            name="email"
            required
            type="text"
            autocomplete="off"
            placeholder="请输入邮箱"
            :rules="[
              {
                required: true,
                pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
                message: '请正确填写邮箱'
              }
            ]"
          />

          <van-field
            v-model.trim="loginInfo.code"
            name="code"
            placeholder="请输入验证码"
            clearable
            type="digit"
            maxlength="6"
            autocomplete="off"
            :rules="[
              {
                required: true,
                pattern: /^\d{6}$/,
                message: '请正确填写验证码'
              }
            ]"
          >
            <template #button>
              <van-button @click="getCodeHandle" size="small" type="primary">
                <div class="flex justify-center items-center">
                  <span class="--van-gray-1">{{
                    codeSendStatus ? "已发送" : "发送验证码"
                  }}</span>
                  <van-count-down
                    v-show="codeSendStatus"
                    ref="countDownRef"
                    @finish="countDownFinishHandle"
                    :time="countDown"
                    :auto-start="false"
                    format="ss"
                  >
                    <template #default="{ seconds }">
                      <span class="--van-gray-1">{{ `(${seconds}s)` }}</span>
                    </template>
                  </van-count-down>
                </div>
              </van-button>
            </template>
          </van-field>
        </van-cell-group>
        <div class="mt-[50px]">
          <loading-button
            round
            block
            type="primary"
            native-type="submit"
            :loadingClick="onSubmitHandle"
          >
            登录
          </loading-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import defaultAvatarUrl from "../../../public/favicon.ico";
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getSmsCode } from "@/api/user"; // 注意：如果后端接口改为 getEmailCode，此处需同步修改
import type { CountDownInstance, FormInstance } from "vant";
import { useUserStore } from "@/store/modules/user";
import LoadingButton from "@/components/LoadingButton/LoadingButton.vue";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 登录信息
const loginInfo = ref({
  email: "test@example.com",  // 改为邮箱示例
  code: "123456"
});

// 表单实例
const formRef = ref<FormInstance>();

// 验证码发送状态
const codeSendStatus = ref(false);

// 倒计时相关
const countDown = ref(60 * 1000);
const countDownRef = ref<CountDownInstance>();

const countDownStartHandle = () => {
  countDownRef.value?.start();
  codeSendStatus.value = true;
};
const countDownResetHandle = () => {
  countDownRef.value?.reset();
  codeSendStatus.value = false;
};
const countDownFinishHandle = () => {
  countDownResetHandle();
};

// 获取验证码
const getCodeHandle = async () => {
  // 验证邮箱字段
  await formRef.value?.validate("email");
  // 开始倒计时
  countDownStartHandle();
  // 调用接口，参数名改为 email
  getSmsCode({ email: loginInfo.value.email });
};

const onSubmitHandle = async () => {
  console.log("onSubmit");
  await formRef.value?.validate();
  // 登录时 store 的 LoginAction 也需改为接收 email
  await userStore.LoginAction(loginInfo.value);
  await router.replace(
    route.query?.redirect
      ? decodeURIComponent(route.query?.redirect as string)
      : "/"
  );
};

onMounted(() => {
  console.log("route", route);
  console.log("router-onMounted", router);
});
</script>

<style scoped lang="less"></style>