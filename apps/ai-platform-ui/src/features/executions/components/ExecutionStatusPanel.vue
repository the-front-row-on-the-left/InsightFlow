<template>
  <section class="status-panel">
    <div>
      <p class="eyebrow">Execution Status</p>
      <h3>{{ status }}</h3>
    </div>

    <dl>
      <div>
        <dt>request_id</dt>
        <dd>{{ requestId }}</dd>
      </div>
      <div>
        <dt>cost</dt>
        <dd>{{ costLabel }}</dd>
      </div>
      <div>
        <dt>recommendation</dt>
        <dd>{{ recommendationMessage }}</dd>
      </div>
    </dl>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  status: string
  requestId: string
  costStatus: string
  recommendationMessage: string
}>()

const costLabel = computed(() => {
  if (props.costStatus === 'completed' || props.costStatus === 'READY') {
    return '계산 완료'
  }

  if (props.costStatus === 'estimated') {
    return '추정 비용'
  }

  return '계산 중'
})
</script>

<style scoped>
.status-panel {
  padding: 22px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(31, 31, 31, 0.08);
}

.eyebrow {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: #80592a;
}

h3 {
  margin: 8px 0 0;
  font-size: 32px;
}

dl {
  margin: 16px 0 0;
  display: grid;
  gap: 12px;
}

dt {
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #8b8177;
}

dd {
  margin: 4px 0 0;
  font-weight: 600;
}
</style>
