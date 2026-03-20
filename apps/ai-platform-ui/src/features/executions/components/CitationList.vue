<template>
  <div class="citation-panel">
    <p class="panel-title">출처</p>
    <div class="citation-list">
      <div
        v-for="(citation, i) in citations"
        :key="`${citation.doc_id}-${citation.section}`"
        class="citation-item"
      >
        <span class="citation-num">{{ i + 1 }}</span>
        <div>
          <p class="citation-title">{{ citation.title }}</p>
          <p class="citation-meta">{{ citation.doc_id }} · {{ citation.section }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Citation } from '../../platform/types'
defineProps<{ citations: Citation[] }>()
</script>

<style scoped>
.citation-panel {
  padding: 20px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
}

.panel-title {
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: #94a3b8;
  margin-bottom: 12px;
}

.citation-list { display: flex; flex-direction: column; gap: 8px; }

.citation-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 10px 12px;
  background: #f8fafc;
  border-radius: 8px;
}

.citation-num {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #e0e7ff;
  color: #4338ca;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.citation-title { font-size: 13px; font-weight: 600; color: #0f172a; margin-bottom: 2px; }
.citation-meta { font-size: 11px; color: #94a3b8; }
</style>
