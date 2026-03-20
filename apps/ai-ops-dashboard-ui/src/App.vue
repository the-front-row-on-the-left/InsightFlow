<template>
  <div class="app-shell">
    <header class="topbar">
      <RouterLink to="/overview" class="brand">
        <span class="brand-mark">◆</span>
        <div class="brand-copy">
          <span class="brand-name">InsightFlow</span>
          <span class="brand-sub">AIOps Dashboard</span>
        </div>
      </RouterLink>

      <nav class="nav-links">
        <RouterLink to="/overview">Overview</RouterLink>
        <RouterLink to="/usage">Usage</RouterLink>
        <RouterLink to="/billing">Billing</RouterLink>
        <RouterLink to="/recommendations">Recommendations</RouterLink>
        <RouterLink to="/notifications">Notifications</RouterLink>
        <RouterLink to="/executions" class="nav-cta">Executions</RouterLink>
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
  background:
    radial-gradient(circle at top left, rgba(79, 70, 229, 0.08), transparent 28%),
    linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  color: #0f172a;
  line-height: 1.5;
  -webkit-font-smoothing: antialiased;
}

:global(a) {
  color: inherit;
  text-decoration: none;
}

:global(button),
:global(input),
:global(select) {
  font-family: inherit;
}

:global(.page-stack) {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

:global(.page-header) {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

:global(.page-title) {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: #0f172a;
}

:global(.page-subtitle) {
  font-size: 14px;
  color: #64748b;
  margin-top: 6px;
}

:global(.card-grid) {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 14px;
}

:global(.panel) {
  padding: 20px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

:global(.panel-title) {
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: #94a3b8;
  margin-bottom: 12px;
}

:global(.kpi-card) {
  padding: 18px;
  border-radius: 16px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid #e2e8f0;
}

:global(.kpi-label) {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

:global(.kpi-value) {
  margin-top: 10px;
  font-size: 30px;
  line-height: 1;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.04em;
}

:global(.kpi-hint) {
  margin-top: 8px;
  font-size: 13px;
  color: #64748b;
}

:global(.data-table) {
  width: 100%;
  border-collapse: collapse;
}

:global(.data-table th) {
  text-align: left;
  font-size: 12px;
  color: #94a3b8;
  font-weight: 700;
  padding: 0 0 10px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

:global(.data-table td) {
  padding: 12px 0;
  border-top: 1px solid #eef2f7;
  font-size: 14px;
  color: #334155;
  vertical-align: top;
}

:global(.pill) {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

:global(.pill-success) { background: #dcfce7; color: #166534; }
:global(.pill-warning) { background: #fef3c7; color: #92400e; }
:global(.pill-danger) { background: #fee2e2; color: #b91c1c; }
:global(.pill-muted) { background: #e2e8f0; color: #475569; }
:global(.pill-info) { background: #dbeafe; color: #1d4ed8; }

:global(.metric-row) {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
}

:global(.metric-box) {
  padding: 14px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

:global(.metric-box strong) {
  display: block;
  font-size: 22px;
  color: #0f172a;
  margin-top: 6px;
  letter-spacing: -0.03em;
}

:global(.metric-box span) {
  font-size: 12px;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  font-weight: 700;
}

:global(.empty-state) {
  padding: 24px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  color: #64748b;
  font-size: 14px;
}

:global(.toolbar) {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

:global(.field) {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 180px;
}

:global(.field label) {
  font-size: 12px;
  color: #64748b;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

:global(.field input) {
  height: 40px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  padding: 0 12px;
  background: #fff;
  color: #0f172a;
  outline: none;
}

:global(.field input:focus) {
  border-color: #a5b4fc;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}

:global(.split-grid) {
  display: grid;
  grid-template-columns: 1.25fr 1fr;
  gap: 16px;
}

.app-shell {
  min-height: 100vh;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 64px;
  padding: 10px 24px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid #e2e8f0;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 10px;
  background: linear-gradient(135deg, #4f46e5 0%, #1d4ed8 100%);
  color: #fff;
  font-size: 12px;
  box-shadow: 0 8px 20px rgba(79, 70, 229, 0.24);
}

.brand-copy {
  display: flex;
  flex-direction: column;
  line-height: 1.05;
}

.brand-name {
  font-size: 15px;
  font-weight: 800;
  color: #0f172a;
}

.brand-sub {
  font-size: 11px;
  color: #64748b;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.nav-links a {
  padding: 7px 12px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
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
}

.nav-links .nav-cta {
  background: #0f172a;
  color: #fff;
}

.nav-links .nav-cta:hover {
  background: #1e293b;
  color: #fff;
}

.main-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 34px 24px 80px;
}

@media (max-width: 900px) {
  :global(.split-grid) {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .topbar {
    padding: 12px 16px;
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
  }

  .nav-links {
    width: 100%;
  }

  .main-content {
    padding: 24px 16px 64px;
  }
}
</style>
