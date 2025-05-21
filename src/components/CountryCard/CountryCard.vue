<template>
  <div class="wrapper">
    <div v-if="loading" class="loading">加载中...</div>

    <div v-if="!loading" class="card-horizontal">
      <div class="flag-wrapper">
        <img
            :src="country.flagUrl"
            @error="handleImageError"
            alt="国旗"
            class="flag"
            v-if="!flagError"
        />
        <div v-else class="flag-placeholder"></div>
      </div>

      <div class="info-horizontal">
        <span class="name">{{ country.nameZh }} ({{ country.nameEn }})</span>
        <span><strong>代码:</strong> {{ country.countryCode }}</span>
        <span><strong>区域:</strong> {{ country.regionCode }}</span>
        <span><strong>人口:</strong> {{ country.population?.toLocaleString() }}</span>
        <span><strong>更新时间:</strong> {{ formatDate(country.populationUpdatedAt) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import axios from 'axios';

// 接收父组件传入的 code
const props = defineProps({
  code: {
    type: String,
    required: true,
  },
});

const loading = ref(true);
const flagError = ref(false);

const country = ref({
  countryCode: '',
  nameZh: '',
  nameEn: '',
  regionCode: '',
  population: 0,
  populationUpdatedAt: '',
  flagUrl: '',
});

const loadCountry = async (code) => {
  loading.value = true;
  flagError.value = false;
  try {
    const { data } = await axios.get(`http://localhost:8081/api/country?code=${code}`);
    country.value = data;
  } catch (e) {
    console.error('加载国家信息失败:', e);
    alert('加载失败，请稍后再试');
  } finally {
    loading.value = false;
  }
};

const handleImageError = (event) => {
  flagError.value = true;
  event.target.src = 'https://via.placeholder.com/160x100?text=No+Flag';
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleDateString();
};

// 监听 props.code 变化，重新加载数据
watch(() => props.code, (newCode) => {
  if (newCode) {
    loadCountry(newCode);
  }
});

// 组件挂载时加载一次
onMounted(() => {
  if (props.code) {
    loadCountry(props.code);
  }
});

</script>

<style scoped>
.wrapper {
  text-align: center;
  margin-top: 20px; /* 可以适当保留一点点顶部间距 */
  margin-bottom: 20px;
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  background-color: transparent; /* 去掉白色背景 */
  color: #222222;
  min-height: auto; /* 取消最小高度 */
}

.loading {
  font-size: 1.2rem;
  color: #555555;
  margin-top: 20px;
  font-style: italic;
}

.card-horizontal {
  width: 100%;         /* 占满整行 */
  max-width: none;     /* 取消最大宽度限制 */
  margin: 0 auto;
  background: #ffffff;
  border: 1px solid #cccccc;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  padding: 10px 16px;  /* 减小内边距，减小高度 */
  display: flex;
  align-items: center;
  gap: 16px;
  color: #222222;

  white-space: nowrap; /* 不换行 */
  overflow-x: hidden;  /* 隐藏横向滚动条 */
  box-sizing: border-box;
}

.flag-wrapper {
  flex-shrink: 0;
  width: 140px;      /* 宽度略小 */
  height: 90px;      /* 高度略小 */
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.flag {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 6px;
}

.flag-placeholder {
  width: 140px;
  height: 90px;
  border-radius: 6px;
  background-color: #eeeeee;
}

.info-horizontal {
  display: flex;
  align-items: center;
  gap: 20px;       /* 缩小间距 */
  font-size: 0.9rem; /* 字体稍小 */
  color: #222222;
  flex-wrap: nowrap; /* 防止换行 */
  overflow: hidden;  /* 内容溢出隐藏 */
  text-overflow: ellipsis;
}

.info-horizontal span {
  display: inline-block;
  white-space: nowrap;
  max-width: 150px;  /* 限制宽度，防止撑破布局 */
  overflow: hidden;
  text-overflow: ellipsis;
}

.info-horizontal span strong {
  font-weight: 600;
  text-transform: uppercase;
  color: #003366;
  margin-right: 4px;
}

.info-horizontal .name {
  font-weight: 700;
  font-size: 1.1rem; /* 稍微小一点 */
  color: #003366;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
}

</style>
