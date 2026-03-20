<template>
  <section class="example-card">
    <p class="eyebrow">Result Example</p>
    <h3>{{ headline }}</h3>
    <p class="description">{{ description }}</p>
    <ul>
      <li v-for="item in items" :key="item">{{ item }}</li>
    </ul>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  headline: string
  description: string
  items: string[]
}>()
</script>

<style scoped>
.example-card {
  padding: 20px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.7);
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
  margin: 8px 0 10px;
  font-size: 24px;
}

.description {
  margin: 0;
  color: #514a40;
}

ul {
  margin: 14px 0 0;
  padding-left: 18px;
  display: grid;
  gap: 8px;
}
</style>
