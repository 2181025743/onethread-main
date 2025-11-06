<!--
 * @Descripttion: 
 * @version: 1.0.0
 * @Author: fulei🐰
 * @Date: 2025-06-07 09:41:08
 * @LastEditors: fulei🐰
 * @LastEditTime: 2025-06-09 22:01:06
-->
<script lang="ts" setup>
import type { VbenFormSchema } from '@vben/common-ui';
import { computed, ref } from 'vue';
import { AuthenticationLogin, z } from '@vben/common-ui';
import { $t } from '@vben/locales';
import { useAuthStore } from '#/store';
import type { Recordable } from '@vben/types';
import { ElButton, ElDialog, ElInput, ElNotification } from 'element-plus';

defineOptions({ name: 'Login' });

const authStore = useAuthStore();
const formSchema = computed((): VbenFormSchema[] => [
  {
    component: 'VbenInput',
    componentProps: {
      placeholder: $t('authentication.usernameTip'),
    },
    fieldName: 'username',
    defaultValue: 'admin', // 默认用户名
    label: $t('authentication.username'),
    rules: z.string().min(1, { message: $t('authentication.usernameTip') }),
  },
  {
    component: 'VbenInputPassword',
    componentProps: {
      placeholder: $t('authentication.password'),
    },
    fieldName: 'password',
    defaultValue: 'admin', // 默认密码
    label: $t('authentication.password'),
    rules: z.string().min(1, { message: $t('authentication.passwordTip') }),
  },
]);

const dialogVisible = ref(false);
const loginPayload = ref<Recordable<any>>({});
const input = ref('');

// 新增：存储原始密码
const originalPassword = ref('');

// 在组件初始化时检测域名
const isNageofferDomain = window.location.hostname.includes('nageoffer.com');

const loginFn = (values: Recordable<any>) => {
  loginPayload.value = values;
  
  if (isNageofferDomain) {
    // 存储原始密码
    originalPassword.value = values.password;
    // 目标域名：弹出验证码
    dialogVisible.value = true;
  } else {
    // 非目标域名：直接使用原始密码登录
    authStore.authLogin(loginPayload.value).then(success => {
      if (!success?.userInfo) {
        ElNotification({
          message: '登录失败，请重试',
          type: 'error',
          title: '错误',
        });
      }
    }).catch(error => {
      console.error('登录失败:', error);
      ElNotification({
        message: '登录失败，请稍后重试',
        type: 'error',
        title: '错误',
      });
    });
  }
};

const loginAuth = async () => {
  if (!input.value) {
    ElNotification({
      message: '请输入验证码',
      type: 'warning',
      title: '提示',
    });
    return;
  }

  try {
    // 关键修改：使用验证码替换密码值
    const loginData = {
      ...loginPayload.value,
      password: input.value // 验证码作为密码
    };
    
    const success = await authStore.authLogin(loginData);

    if (success?.userInfo) {
      dialogVisible.value = false;
      input.value = ''; // 清除验证码
      
      // 恢复原始密码值（为可能的后续操作）
      loginPayload.value.password = originalPassword.value;
    } else {
      ElNotification({
        message: '登录失败，请重试',
        type: 'error',
        title: '错误',
      });
    }
  } catch (error) {
    console.error('登录失败:', error);
    ElNotification({
      message: '登录失败，请稍后重试',
      type: 'error',
      title: '错误',
    });
    
    // 发生错误时同样恢复原始密码
    loginPayload.value.password = originalPassword.value;
  }
};

const handleClose = () => {
  dialogVisible.value = false;
  // 关闭弹窗时恢复原始密码
  if (originalPassword.value) {
    loginPayload.value.password = originalPassword.value;
  }
  input.value = '';
};

const getImgSrc = () => {
  return new URL('../../../asstes/imgs/AccountQRCode.png', import.meta.url).href;
};
</script>

<template>
  <div>
    <AuthenticationLogin
      :form-schema="formSchema"
      :loading="authStore.loginLoading"
      :showForgetPassword="false"
      :showRegister="false"
      :showRememberMe="false"
      @submit="loginFn"
    />

    <el-dialog
      v-model="dialogVisible"
      title=""
      width="600px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      append-to-body
      class="custom-dialog"
    >
      <template #header>
        <div class="dialog-header">
          <span class="dialog-title">人机验证</span>
          <el-button class="dialog-close-btn" type="text" icon="Close" @click="handleClose" />
        </div>
      </template>

      <!-- 修改对话框body部分 -->
      <div class="dialog-body">
        <p class="instruction-text">
          扫描下方二维码，关注后回复：<span class="code">thread</span>，获取动态线程池系统人机验证码
        </p>
        <img class="qrcode" :src="getImgSrc()" alt="二维码" />
        
        <!-- 使用弹性布局容器包裹整个输入区域 -->
        <div class="captcha-input-container">
          <!-- 标签与星号合并为单独容器 -->
          <div class="input-label">
            <span class="required-star">*</span>
            <span>验证码：</span>
          </div>
          
          <!-- 独立容器包裹输入框 -->
          <div class="input-field">
            <el-input
              placeholder="请输入验证码"
              v-model="input"
              class="code-input"
              @keyup.enter="loginAuth">
            </el-input>
          </div>
        </div>
      </div>


      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose">取 消</el-button>
          <el-button type="primary" @click="loginAuth">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.captcha-input-container {
  display: flex;
  align-items: center; /* 垂直居中 */
  justify-content: center; /* 水平居中 */
  margin-top: 20px;
  width: 95%;
  margin-left: auto;
  margin-right: auto;
}
.input-label {
  display: flex;
  align-items: center;
  white-space: nowrap;
  margin-right: 10px;
  font-size: 14px;
}
.required-star {
  color: #f56c6c;
  margin-right: 4px;
  font-weight: bold;
}
.input-field {
  flex: 1; /* 占据剩余空间 */
}
/* 调整原code样式 */
.code-input {
  /* 移除外边距设置，由容器控制 */
  margin-left: 0;
  margin-right: 0;
  margin-top: 0;
}

.code {
  color: #1a8cff;
  font-size: 16px;
  font-weight: bold;
  margin: 0 2px;
}

.qrcode {
  width: 260px;
  height: 260px;
  display: block;
  margin: 10px auto;
  border: 1px solid #eee;
  border-radius: 4px;
}


.custom-dialog :deep(.el-dialog__body) {
  padding: 24px 32px;
  text-align: center;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #eee;
}

.dialog-title {
  font-size: 18px;
  font-weight: 600;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.dialog-close-btn {
  font-size: 16px;
  color: #909399;
}

.instruction-text {
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 16px;
  color: #555;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #eee;
}

.dialog-footer .el-button {
  min-width: 80px;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
</style>