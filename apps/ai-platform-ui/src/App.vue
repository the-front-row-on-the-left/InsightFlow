<template>
  <div class="app-shell">
    <header class="topbar">
      <div>
        <p class="eyebrow">InsightFlow</p>
        <h1>AI Service Studio</h1>
      </div>
      <nav class="nav-links">
        <RouterLink to="/catalog">Catalog</RouterLink>
        <RouterLink to="/workflows/new">New Workflow</RouterLink>
      </nav>
    </header>

    <main class="main-content">
      <section class="hero-panel">
        <p class="hero-kicker">Demo Target</p>
        <h2>서비스 탐색, 실행, 워크플로우 저장/재실행까지 한 흐름으로 연결합니다.</h2>
        <p>
          `svc_doc_search`는 end-to-end 데모 대상으로 연결하고, 나머지 서비스는 mock 결과로
          경험을 완성합니다.
        </p>
      </section>

      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<style scoped>
:global(body) {
  margin: 0;
  font-family: 'Helvetica Neue', Arial, sans-serif;
  background: #efe8db;
  color: #1f1f1f;
}

:global(*) {
  box-sizing: border-box;
}

:global(a) {
  color: inherit;
  text-decoration: none;
}

.app-shell {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(190, 127, 55, 0.22), transparent 24%),
    radial-gradient(circle at right top, rgba(28, 91, 82, 0.12), transparent 20%),
    linear-gradient(180deg, #f7f1e6 0%, #eadfcb 100%);
}

.topbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
  padding: 32px 24px 0;
  max-width: 1200px;
  margin: 0 auto;
}

.eyebrow {
  margin: 0 0 12px;
  font-size: 12px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

h1,
h2 {
  margin: 0;
  line-height: 1;
}

h1 {
  font-size: clamp(28px, 4vw, 44px);
}

.nav-links {
  display: flex;
  gap: 12px;
  align-items: center;
}

.nav-links a {
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(31, 31, 31, 0.08);
}

.nav-links a.router-link-active {
  background: #1f1f1f;
  color: #f7f1e6;
}

.main-content {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 24px 64px;
}

.hero-panel {
  margin-bottom: 24px;
  padding: 24px;
  border-radius: 28px;
  background: rgba(255, 250, 242, 0.72);
  border: 1px solid rgba(31, 31, 31, 0.06);
  box-shadow: 0 18px 36px rgba(68, 53, 24, 0.08);
}

.hero-kicker {
  margin: 0 0 10px;
  font-size: 13px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #8e5e20;
}

.hero-panel h2 {
  max-width: 760px;
  font-size: clamp(24px, 3.4vw, 38px);
}

.hero-panel p:last-child {
  max-width: 700px;
  margin-top: 14px;
  line-height: 1.5;
}

@media (max-width: 720px) {
  .topbar {
    flex-direction: column;
    align-items: stretch;
  }

  .nav-links {
    flex-wrap: wrap;
  }
}
</style>
