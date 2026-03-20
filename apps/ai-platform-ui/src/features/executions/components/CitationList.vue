<template>
  <section class="citation-panel">
    <h3>Citations</h3>
    <ul>
      <li v-for="citation in citations" :key="`${citation.doc_id}-${citation.section}`">
        <strong>{{ citation.title }}</strong>
        <span>{{ citation.doc_id }} · {{ citation.section }}</span>
      </li>
    </ul>
  </section>
</template>

<script setup lang="ts">
import type { Citation } from '../../platform/types'

defineProps<{
  citations: Citation[]
}>()
</script>

<style scoped>
.citation-panel {
  padding: 20px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(31, 31, 31, 0.08);
}

h3,
ul {
  margin: 0;
}

ul {
  margin-top: 14px;
  padding-left: 18px;
  display: grid;
  gap: 12px;
}

li span {
  display: block;
  margin-top: 4px;
  color: #5e564b;
}
</style>
