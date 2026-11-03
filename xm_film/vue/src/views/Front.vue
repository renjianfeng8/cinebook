<template>
  <div class="front-container">

    <div class="front-header">
      <div class="front-header-left">
        <img src="@/assets/imgs/xm_logo.jpg" :alt="SITE_NAME" class="logo">
        <h1 class="title">{{ SITE_NAME }}</h1>
      </div>

      <div class="front-header-center">
        <!-- 导航项由 NAV_ITEMS 驱动：高亮态按「当前路由归属哪个导航段」算，
             不靠一个手工同步的 activePath 字符串（那种写法漏掉一段就会点错项） -->
        <nav class="main-nav">
          <router-link
              v-for="item in visibleNavItems"
              :key="item.path"
              :to="item.path"
              class="nav-item"
              :class="{ 'active': isNavActive(item, route.path) }"
              :aria-current="isNavActive(item, route.path) ? 'page' : undefined"
          >
            {{ item.label }}
          </router-link>
        </nav>
      </div>

      <div class="front-header-right">
        <!-- 搜索框带 aria-label：placeholder 一输入就消失，不构成可访问名称 -->
        <el-input v-model="searchKeyword" placeholder="请输入电影名称" aria-label="搜索电影名称"
                  class="search-input" @keyup.enter="handleSearch">
          <template #append>
            <el-button type="info" @click="handleSearch">搜 索</el-button>
          </template>
        </el-input>

        <!-- 未登录：显示登录/注册（包一层 flex 容器，用 gap 统一「·」两侧留白，替代原来的 margin） -->
        <div v-if="!isLoggedIn" class="auth-links">
          <router-link to="/login" class="header-link">登录</router-link>
          <span class="header-divider" aria-hidden="true">·</span>
          <router-link to="/register" class="header-link">注册</router-link>
        </div>

        <!-- 已登录：显示用户信息 -->
        <template v-else>
          <el-dropdown trigger="click">
            <div class="user-info">
              <img :src="userAvatar" alt="用户头像" class="avatar" @error="onAvatarError">
              <span class="username">{{ userName }}</span>
              <el-icon><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <!-- 后台入口收进头像菜单：它足够低频，不值得在页头单独占一个按钮 -->
                <el-dropdown-item v-if="isCinema || isAdmin" @click="goAdmin">管理后台</el-dropdown-item>
                <template v-if="showUserEntries">
                  <el-dropdown-item @click="router.push('/front/person')">个人中心</el-dropdown-item>
                  <el-dropdown-item @click="router.push('/front/account')">我的账户</el-dropdown-item>
                  <el-dropdown-item @click="router.push('/front/password')">修改密码</el-dropdown-item>
                  <el-dropdown-item @click="router.push('/front/payPassword')">支付密码</el-dropdown-item>
                </template>
                <el-dropdown-item @click="confirmLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </div>
    </div>

    <div class="front-content">
      <RouterView />
    </div>

    <div class="front-footer">
      <div class="footer-disclaimer">
        <p>本系统为 <strong>个人学习项目</strong>，不反映真实市场情况。</p>
        <p class="footer-disclaimer__note">严禁将本系统用于任何商业用途。使用本系统即表示您已了解并同意上述条款。</p>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CaretBottom } from '@element-plus/icons-vue'
import { useAuth } from '@/composables/useAuth'
import { useLogout } from '@/composables/useLogout'
import { navItemsOf, isNavActive } from '@/constants/nav'
import { SITE_NAME } from '@/utils/navigation'
import { avatarSrc, onAvatarError } from '@/utils/avatar'

const router = useRouter()
const route = useRoute()
const { user, isLoggedIn, isAdmin, isCinema } = useAuth()
const { confirmLogout } = useLogout()

const searchKeyword = ref('')

// 导航项由路由表的 meta.nav 派生（constants/nav.js）：路由表是唯一出处，
// 不再维护第二份手工清单。高亮按 sections 的路径边界匹配，见 isNavActive。
const navItems = navItemsOf(router)

const visibleNavItems = computed(() =>
    navItems.filter(item => !item.requiresUser || showUserEntries.value)
)

const goAdmin = () => {
  if (isAdmin.value) {
    router.push('/manage/home')
  } else if (isCinema.value) {
    router.push('/back/home')
  }
}

const userName = computed(() => user.value?.username || '')

// 游客也要看得到（点击走登录引导），只对后台角色隐藏 —— 这些路由的 meta.roles 只认 USER
const showUserEntries = computed(() => !isAdmin.value && !isCinema.value)

// 头像与其余视图同为直接绑定：库里存的是 `/files/...` 相对路径，开发经 vite 的 /files 代理，
// 与同源情形都能命中。**不要在这里拼 API_BASE_URL** —— 同源（`/`）时会拼出 `//files/...`，
// 浏览器按协议相对 URL 解析，反而指向不存在的 host（Back.vue / Manage.vue 亦不拼）。
const userAvatar = computed(() => {
  return avatarSrc(user.value?.avatar)
})

const handleSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    ElMessage.warning('请输入电影名称')
    return
  }
  // 走 router.push 而非 window.location.href：整页重载会丢掉 SPA 状态，
  // 并把应用重新下载一遍（此前这里是一次完整的页面刷新）
  router.push({ path: '/front/search', query: { title: keyword } })
}
</script>

<style scoped>
/* 前台外壳：消费端页面底色是白（「前台页面背景 #ffffff」），
 * 而 body 默认取的是 --el-bg-color-page（灰）。所以在这里铺白，并让内容区撑满，
 * 短页面时页脚仍贴底。 */
.front-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--el-bg-color);
}

.front-content {
  flex: 1;
}

/* 页脚只剩底部两行免责声明：深底浅字，与页脚时期的观感一致 */
.front-footer {
  margin-top: var(--space-64);
  background-color: var(--dark-bg);
  color: var(--dark-text-faint);
}

.footer-disclaimer {
  max-width: 1000px;
  margin: 0 auto;
  padding: var(--space-16);
  font-size: var(--fs-xs);
  line-height: var(--lh-base);
}

.footer-disclaimer__note {
  margin-top: var(--space-4);
}

/* ============================================================
 * 顶部导航栏（Header）—— Flex 弹性自适应
 * 收缩优先级：左组(不缩) > 导航项(不缩、不换行) > 右组(可缩，搜索框是唯一泄压阀)
 * ============================================================ */
.front-header {
  /* 字号令牌已上移为全局令牌（tokens.scss 的 :root），组件不再重复声明。
   * 组件级只保留本组件专有的尺寸令牌，命名不得与全局令牌冲突。 */
  --header-height: 60px;
  --header-padding-x: 20px;
  --group-gap: 16px;
  --nav-gap: 24px;
  --search-width: 200px;
  --header-min-width: 1120px;     /* 临界阈值：低于此宽度停止压缩，改为整页横向滚动 */

  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--group-gap);          /* 三组之间弹性留白 */
  height: var(--header-height);
  padding: 0 var(--header-padding-x);
  background: var(--el-bg-color);
  box-shadow: var(--shadow-edge);
  /* 最小宽度保护：低于该阈值不再继续压缩，改为整页横向滚动。
     刻意不用 overflow: hidden —— 裁切会让按钮/输入框不可见不可点 */
  min-width: var(--header-min-width);
  box-sizing: border-box;
}

/* 三组容器公共布局（原 `.front-header > div` 改为显式类名，避免误伤新增子元素） */
.front-header-left,
.front-header-center,
.front-header-right {
  display: flex;
  align-items: center;
  min-width: 0;
}

/* 左组：Logo + 标题，永不压缩 */
.front-header-left {
  flex: 0 0 auto;
  gap: 10px;
}

/* 中组：导航，吸收富余空间；min-width:0 允许自身收缩而不撑破父容器 */
.front-header-center {
  flex: 1 1 auto;
  min-width: 0;
  justify-content: center;
}

.main-nav {
  display: flex;
  align-items: center;
  gap: var(--nav-gap);
  padding: 0;                     /* 去掉原 15px 纵向内边距，垂直居中交给 align-items */
}

.nav-item {
  flex: 0 0 auto;                 /* 导航项不参与压缩 */
  white-space: nowrap;            /* 菜单文字禁止换行，杜绝折行堆叠 */
  color: var(--el-text-color-primary);
  text-decoration: none;
  font-size: var(--fs-md);        /* 16px，与 1920 原观感一致 */
  line-height: 1.2;
  padding: 8px 12px;
  transition: color 100ms ease-out, transform 200ms ease-in-out;
  position: relative;
}

.nav-item:hover {
  color: var(--el-color-primary);
  transform: translateY(-2px);
}

/* 激活态：写成 .nav-item.active 这个复合选择器，特异性与 .nav-item:hover 相同
 * （都是 0-2-0），而它在本文件里位置更靠后，因此自然胜出 —— 不需要 !important。
 * 单写 .active（0-1-0）会被 .nav-item:hover 压掉，这才是原代码加 !important 的原因。 */
.nav-item.active {
  color: var(--el-color-primary);
  font-weight: var(--fw-bold);
}

/* 下划线是装饰（不承载文字），取 --color-brand 的亮档 #ef4238 —— 与
 * 区块标题的短线（front-pages.scss .section-head::after）用同一条品牌强调语言。
 * 文字仍用 --el-color-primary：那是承载文字的档，两者不可互换。 */
.nav-item.active::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 2px;
  background-color: var(--color-brand);
  border-radius: var(--el-border-radius-small);
}

/* 键盘焦点必须可见（Focus） */
.nav-item:focus-visible {
  outline: 2px solid var(--el-color-primary);
  outline-offset: 2px;
  border-radius: var(--el-border-radius-base);
}

/* 右组：搜索 + 用户区；唯一的泄压阀（flex-shrink: 1） */
.front-header-right {
  flex: 0 1 auto;
  min-width: 0;
  gap: 15px;
}

.logo {
  height: 30px;
  flex: 0 0 auto;
  display: block;
}

.title {
  font-size: var(--fs-md);
  font-weight: var(--fw-bold);
  line-height: 1.2;
  margin: 0;                      /* 抵消 h1 默认外边距，避免在 60px 栏内撑高错位 */
  white-space: nowrap;            /* 标题禁止折行 */
}

.search-input {
  /* 定宽改为弹性基准：空间充足时 200px（与原来一致），不足时优先收窄搜索框 */
  flex-grow: 0;
  flex-shrink: 1;
  flex-basis: var(--search-width);
  min-width: 150px;
  height: 34px;
}

/* Element Plus 输入框内部同样要放开收缩限制，否则外层缩了内层仍撑破 */
.search-input :deep(.el-input__wrapper) {
  min-width: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 0 1 auto;
  min-width: 0;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: var(--el-border-radius-base);
}

.user-info:hover {
  background-color: var(--el-fill-color-light);
}

.avatar {
  height: 32px;
  width: 32px;
  flex: 0 0 auto;
  border-radius: var(--el-border-radius-circle);
  object-fit: cover;
}

.username {
  font-size: var(--fs-base);
  /* 超长用户名截断，避免撑爆右组引发元素互挤 */
  max-width: 6em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 未登录：登录 / 注册（间距由 gap 统一，替代原来的 margin） */
.auth-links {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 0 0 auto;
}
.header-link {
  color: var(--el-text-color-primary);
  text-decoration: none;
  font-size: var(--fs-base);
  white-space: nowrap;
  transition: color 100ms ease-out;
}
.header-link:hover {
  color: var(--el-color-primary);
}
/* 登录 / 注册之间的分隔点：纯装饰，不承载信息。
 * 已加 aria-hidden 使其成为真正的"非文字装饰"，从而适用装饰豁免；
 * 原用 --el-text-color-disabled 属语义误用（该元素并未禁用）。 */
.header-divider {
  color: var(--el-text-color-placeholder);
}

/* ---------- 窄屏逐级收窄间距与搜索框基准宽，给导航让出空间 ----------
   媒体查询按视口宽度匹配：窗口 <1120px 时头部元素仍保持 1120px 宽，
   但内部走最紧的一档，因此不会溢出也不会互相挤压 */
@media (max-width: 1400px) {
  .front-header {
    --nav-gap: 14px;
    --search-width: 185px;   /* 留够「请输入电影名称」占位符完整显示 */
  }
}

@media (max-width: 1200px) {
  .front-header {
    --nav-gap: 8px;
    --group-gap: 12px;
    --search-width: 185px;   /* 同上：保住占位符可读，压缩空间从导航间距里出 */
  }

  .nav-item {
    padding: 8px 8px;
  }
}
</style>
