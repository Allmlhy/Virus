<!-- components/SearchModal.vue -->
<template>
  <transition name="fade">
    <div v-if="modelValue" class="modal-overlay" @click.self="$emit('update:modelValue', false)">
      <div class="modal-content" @click.stop>
        <input
            v-model="query"
            type="text"
            placeholder="请输入关键词搜索"
            class="search-input"
            @keyup.enter="onSearch"
            autofocus
        />
        <button class="search-button" @click="onSearch">搜索</button>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, watch } from 'vue';

defineProps({
  modelValue: Boolean
});
defineEmits(['update:modelValue']);

const query = ref('');

const onSearch = () => {
  if (!query.value.trim()) {
    alert('请输入搜索关键词');
    return;
  }
  alert(`搜索关键词: ${query.value}`);
  query.value = '';
  // 关闭弹窗
  emit('update:modelValue', false);
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10000;
}

.modal-content {
  background: #fff;
  border-radius: 12px;
  padding: 24px 32px;
  box-shadow: 0 8px 20px rgba(0,0,0,0.3);
  min-width: 320px;
  max-width: 90vw;
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  flex-grow: 1;
  font-size: 1.1rem;
  padding: 8px 12px;
  border: 1.5px solid #ccc;
  border-radius: 6px;
  outline: none;
  transition: border-color 0.3s ease;
}

.search-input:focus {
  border-color: #409eff;
}

.search-button {
  padding: 9px 20px;
  background-color: #409eff;
  border: none;
  color: white;
  font-weight: 600;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.search-button:hover {
  background-color: #66b1ff;
}
</style>
