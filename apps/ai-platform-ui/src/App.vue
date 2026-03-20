<template>
  <div class="app-shell">
    <header class="topbar">
      <RouterLink to="/catalog" class="brand">
        <span class="brand-icon">⚡</span>
        <span class="brand-name">InsightFlow</span>
      </RouterLink>
      <nav class="nav-links">
        <RouterLink to="/catalog">서비스 카탈로그</RouterLink>
        <RouterLink to="/workflows/new" class="nav-cta">+ 워크플로우</RouterLink>
      </nav>
    </header>

    <main class="main-content">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<style scoped>
:global(*) {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

:global(body) {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Inter', Arial, sans-serif;
  background: #f8fafc;
  color: #0f172a;
  line-height: 1.5;
  -webkit-font-smoothing: antialiased;
}

:global(a) {
  color: inherit;
  text-decoration: none;
}

:global(input),
:global(select),
:global(textarea),
:global(button) {
  font-family: inherit;
}

.app-shell {
  min-height: 100vh;
  background: #f8fafc;
}

/* ── Topbar ── */
.topbar {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid #e2e8f0;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
}

.brand-icon {
  font-size: 20px;
  line-height: 1;
}

.brand-name {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-links a {
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  transition: background 0.15s, color 0.15s;
}

.nav-links a:hover {
  background: #f1f5f9;
  color: #0f172a;
}

.nav-links a.router-link-active {
  background: #eff6ff;
  color: #4f46e5;
  font-weight: 600;
}

.nav-links .nav-cta {
  background: #4f46e5;
  color: #fff;
  font-weight: 600;
}

.nav-links .nav-cta:hover {
  background: #4338ca;
  color: #fff;
}

/* ── Main ── */
.main-content {
  max-width: 1120px;
  margin: 0 auto;
  padding: 36px 24px 80px;
}

@media (max-width: 720px) {
  .topbar {
    padding: 0 16px;
  }

  .main-content {
    padding: 24px 16px 64px;
  }
}
</style>
