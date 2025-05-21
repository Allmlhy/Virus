<template>
  <div class="page-wrapper">
    <NavBar />
    <div class="carousel-container">
      <!-- 标题点击弹窗 -->
      <h1 class="typing-title" @click="showSearchModal = true" style="cursor: pointer;">
        全球疫情最新动态，点我进行搜索
      </h1>

      <div class="carousel" @mouseenter="pause" @mouseleave="play">
        <div class="carousel-items" :style="carouselStyle">
          <a v-for="(news, index) in filteredNews" :key="index" class="carousel-item" :style="getItemStyle(index)"
            :href="news.url || '#'" target="_blank" rel="noopener noreferrer" @click="pause">
            <img :src="news.image" alt="新闻图片" />
            <div class="caption">
              <h2>{{ news.title }}</h2>
            </div>
          </a>
        </div>
      </div>
    </div>
    <SearchModal v-model="showSearchModal" @search="handleSearch" />

    <!-- 按钮固定在屏幕两侧 -->
    <button class="nav prev" @click="prev" aria-label="上一条新闻">‹</button>
    <button class="nav next" @click="next" aria-label="下一条新闻">›</button>
    <TreeChart />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import NavBar from '@/components/NavBar.vue';
import SearchModal from '@/components/SearchModal/SearchModal.vue'
import CountryCard from '@/components/CountryCard/CountryCard.vue'
import TreeChart from '@/components/TreeChart.vue'
const newsList = [
  {
    title: '中疾控回应新冠疫情态势：临床严重性没有发生明显变化',
    image: 'https://img-s.msn.cn/tenant/amp/entityid/AA1F6WDf.img?w=768&h=439&m=6',
    url: 'https://www.msn.com/zh-cn/news/other/%E4%B8%AD%E7%96%BE%E6%8E%A7%E5%9B%9E%E5%BA%94%E6%96%B0%E5%86%A0%E7%96%AB%E6%83%85%E6%80%81%E5%8A%BF-%E4%B8%B4%E5%BA%8A%E4%B8%A5%E9%87%8D%E6%80%A7%E6%B2%A1%E6%9C%89%E5%8F%91%E7%94%9F%E6%98%8E%E6%98%BE%E5%8F%98%E5%8C%96/ar-AA1F71tU?ocid=BingNewsSerp'
  },
  {
    title: '新冠病毒流行高峰无明显季节性特征，与人群抗体水平有关',
    image: 'https://img-s.msn.cn/tenant/amp/entityid/AA1F77LH.img?w=768&h=432&m=6',
    url: 'https://www.msn.com/zh-cn/news/other/%E4%B8%93%E5%AE%B6-%E6%96%B0%E5%86%A0%E7%97%85%E6%AF%92%E6%B5%81%E8%A1%8C%E9%AB%98%E5%B3%B0%E6%97%A0%E6%98%8E%E6%98%BE%E5%AD%A3%E8%8A%82%E6%80%A7%E7%89%B9%E5%BE%81-%E4%B8%8E%E4%BA%BA%E7%BE%A4%E6%8A%97%E4%BD%93%E6%B0%B4%E5%B9%B3%E6%9C%89%E5%85%B3/ar-AA1F6Whw?ocid=BingNewsSerp'
  },
  {
    title: '世卫组织：夏季新冠病毒感染率激增，不排除卷土重来可能性',
    image: 'https://global.unitednations.entermediadb.net/assets/mediadb/services/module/asset/downloads/preset/Collections/Embargoed/15-01-2024-WHO-Europe-COVID.jpg/image1170x530cropped.jpg',
    url: 'https://news.un.org/zh/story/2024/08/1130451'
  },
  {
    title: '新冠阳性率升高！钟南山紧急提醒！本轮何时结束',
    image: 'https://inews.gtimg.com/om_bt/Oolgsr5dV6bmiqDjMkJuLnLzo8-8yN2BSMxgeqhrz6MEEAA/641',
    url: 'https://news.qq.com/rain/a/20250520A069OP00'
  },
];

const currentIndex = ref(0);
let timer = null;

const filteredNews = computed(() => newsList);

const play = () => {
  timer = setInterval(() => {
    currentIndex.value = (currentIndex.value + 1) % filteredNews.value.length;
  }, 5000);
};

const pause = () => {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
};

const prev = () => {
  pause();
  currentIndex.value = (currentIndex.value - 1 + filteredNews.value.length) % filteredNews.value.length;
  play();
};

const next = () => {
  pause();
  currentIndex.value = (currentIndex.value + 1) % filteredNews.value.length;
  play();
};

const getItemStyle = (index) => {
  const diff = index - currentIndex.value;
  const absDiff = Math.abs(diff);

  if (absDiff > 2) return { display: 'none' };

  const baseZ = 100 - absDiff * 10;
  const baseScale = 1 - absDiff * 0.12;
  const baseTranslateX = diff * 300;

  return {
    transform: `translateX(${baseTranslateX}px) scale(${baseScale})`,
    zIndex: baseZ,
    opacity: absDiff === 2 ? 0.5 : 1,
    filter: absDiff === 2 ? 'blur(1.2px)' : 'none',
    transition: 'transform 0.6s ease, opacity 0.6s ease',
  };
};

const carouselWidth = 780;
const carouselHeight = Math.round(carouselWidth / 1.618); // 黄金比例高约483px

const carouselStyle = computed(() => ({
  height: `${carouselHeight}px`,
  position: 'relative',
}));

// 搜索弹窗状态和逻辑
const showSearchModal = ref(false);
const searchQuery = ref('');

const handleSearch = () => {
  if (!searchQuery.value.trim()) {
    alert('请输入搜索关键词');
    return;
  }
  // 这里写你的搜索逻辑，暂时用alert演示
  alert(`搜索关键词: ${searchQuery.value}`);

  // 搜索后关闭弹窗和清空
  showSearchModal.value = false;
  searchQuery.value = '';
};

onMounted(() => {
  play();
});

onBeforeUnmount(() => {
  pause();
});
</script>

<style scoped>
.page-wrapper {
  background: #fff;
  min-height: 100vh;
  user-select: none;
  color: #333;
  display: flex;
  flex-direction: column;
  align-items: center;
  /* 水平居中所有内容 */
}

.caption h2 {
  font-size: 1.4rem;
  /* 缩小字体，例如从2rem改为1.4rem */
  font-weight: 700;
  margin: 0;
  line-height: 1.4;
}


.carousel-container {
  max-width: 820px;
  margin: 80px auto 40px;
  text-align: center;
  padding: 0 10px;
}

.typing-title {
  font-weight: 700;
  font-size: 2.6rem;
  letter-spacing: 2.5px;
  color: #222;
  margin-bottom: 30px;
  white-space: nowrap;
  overflow: hidden;
  border-right: 4px solid rgba(34, 34, 34, 0.75);
  animation: typing 3.5s steps(25) forwards, blink-caret 0.7s step-end infinite;
  max-width: 100%;
  margin-left: auto;
  margin-right: auto;
  cursor: pointer;
}

@keyframes typing {
  from {
    width: 0
  }

  to {
    width: 30ch
  }
}

@keyframes blink-caret {

  0%,
  100% {
    border-color: transparent
  }

  50% {
    border-color: rgba(34, 34, 34, 0.75)
  }
}

.carousel {
  position: relative;
  width: 100%;
  height: 483px;
  perspective: 1200px;
  overflow: visible;
  margin: 0 auto;
}

.carousel-items {
  position: relative;
  height: 483px;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  transform-style: preserve-3d;
  user-select: none;
}

.carousel-item {
  position: absolute;
  width: 780px;
  height: 483px;
  border-radius: 16px;
  background: #fff;
  box-shadow:
    0 8px 28px rgb(0 0 0 / 0.15),
    0 16px 40px rgb(0 0 0 / 0.1);
  cursor: pointer;
  overflow: hidden;
  transition: all 0.6s ease;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  text-decoration: none;
  color: inherit;
}

.carousel-item img {
  position: absolute;
  top: 0;
  left: 0;
  width: 780px;
  height: 360px;
  object-fit: cover;
  filter: brightness(0.85);
  transition: filter 0.3s ease;
}

.carousel-item:hover img {
  filter: brightness(1);
}

.caption {
  position: relative;
  padding: 24px 32px;
  background: #fff;
  color: #222;
  font-weight: 800;
  font-size: 2rem;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  border-top: 1px solid #eee;
}

/* 按钮固定在屏幕两侧中间，不跟随容器宽度 */
.nav {
  position: fixed;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.7);
  border: none;
  border-radius: 50%;
  width: 48px;
  height: 48px;
  font-size: 28px;
  color: #fff;
  cursor: pointer;
  user-select: none;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.3);
  transition: background-color 0.3s ease, color 0.3s ease;
  z-index: 9999;
}

.nav:hover {
  background: rgba(64, 158, 255, 0.9);
  color: #fff;
}

.prev {
  left: 8px;
}

.next {
  right: 8px;
}
</style>
