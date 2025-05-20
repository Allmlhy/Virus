<template>
  <div class="wrapper">
    <button class="toggle-btn" @click="toggleShow">
      {{ show ? '隐藏国家信息' : '显示国家信息' }}
    </button>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-if="show && !loading" class="card">
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
      <div class="info">
        <h2>{{ country.nameZh }} ({{ country.nameEn }})</h2>
        <p><strong>国家代码:</strong> {{ country.countryCode }}</p>
        <p><strong>所属区域:</strong> {{ country.regionCode }}</p>
        <p><strong>人口:</strong> {{ country.population?.toLocaleString() }}</p>
        <p><strong>人口数据更新时间:</strong> {{ formatDate(country.populationUpdatedAt) }}</p>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue';
import axios from 'axios';

const show = ref(false);
const loading = ref(false);
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


const toggleShow = async () => {
  show.value = !show.value;

  // 如果还没加载过，才发请求
  if (show.value && !country.value.name_zh) {
    loading.value = true;
    try {
      // ✅ 这里替换成你真实后端接口地址和默认国家代码 CN
      const { data } = await axios.get('http://localhost:8081/api/country?code=CN');
      country.value = data;
    } catch (e) {
      console.error('加载国家信息失败:', e);
      alert('加载失败，请稍后再试');
      show.value = false;
    } finally {
      loading.value = false;
    }
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
</script>


<style scoped>
.wrapper {
  text-align: center;
  margin-top: 40px;
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  background-color: #ffffff;
  color: #222222;
  min-height: 100vh;
  padding: 40px 20px;
}

.toggle-btn {
  padding: 12px 28px;
  font-size: 16px;
  background-color: #003366; /* 深蓝色 */
  color: #ffffff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 30px;
  transition: background-color 0.3s ease;
  font-weight: 600;
}

.toggle-btn:hover {
  background-color: #002244;
}

.loading {
  font-size: 1.2rem;
  color: #555555;
  margin-top: 20px;
  font-style: italic;
}

.card {
  max-width: 260px;
  margin: 0 auto;
  background: #ffffff;
  border: 1px solid #cccccc;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  padding: 24px 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #222222;
}

.flag-wrapper {
  width: 60%;
  max-height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  border-radius: 8px;
  overflow: hidden;
  background-color: #f9f9f9;
  border: 1px solid #dddddd;
}

.flag {
  width: 100%;
  object-fit: contain;
  border-radius: 6px;
}

.flag-placeholder {
  width: 100%;
  height: 140px;
  border-radius: 6px;
  background-color: #eeeeee;
}

.info {
  text-align: left;
  width: 100%;
  font-size: 1rem;
  line-height: 1.5;
  color: #222222;
  font-weight: 400;
}

.info h2 {
  margin-bottom: 16px;
  font-size: 1.9rem;
  font-weight: 700;
  color: #003366;
  letter-spacing: 0.02em;
}

.info p {
  margin: 10px 0;
  display: flex;
  justify-content: space-between;
  font-weight: 400;
  color: #333333;
}

.info p strong {
  font-weight: 600;
  min-width: 120px;
  color: #222222;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  font-size: 0.9rem;
}

.info p span {
  font-weight: 400;
  color: #444444;
  text-align: right;
  min-width: 150px;
}

.info p.population span {
  font-weight: 700;
  color: #003366;
  font-size: 1.1rem;
}

.info p.updated {
  font-style: normal;
  font-size: 0.85rem;
  color: #666666;
  font-weight: 400;
}

</style>
