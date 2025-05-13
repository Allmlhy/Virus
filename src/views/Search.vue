<template>
  <div class="search">
    <NavBar />
    <div class="container">
      <h2>数据检索</h2>
      <p>通过关键词检索相关疫情数据。</p>
      <div class="search-box">
        <input v-model="searchQuery" type="text" placeholder="请输入检索关键词" />
        <button @click="searchData">搜索</button>
      </div>
      <div v-if="searchResults.length > 0" class="results">
        <h3>检索结果：</h3>
        <ul>
          <li v-for="(result, index) in searchResults" :key="index">
            {{ result }}
          </li>
        </ul>
      </div>
      <div v-else>
        <p>没有找到相关数据。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import NavBar from "@/components/NavBar.vue";

const searchQuery = ref("");
const searchResults = ref([]);

const searchData = () => {
  // 模拟检索功能，实际可替换为真实的数据请求
  if (searchQuery.value) {
    searchResults.value = [
      `${searchQuery.value} - 结果 1`,
      `${searchQuery.value} - 结果 2`,
      `${searchQuery.value} - 结果 3`,
    ];
  } else {
    searchResults.value = [];
  }
};
</script>

<style scoped>
.container {
  padding-top: 60px;
  margin: 0 20px;
}


h2 {
  text-align: center;
}

.search-box {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.search-box input {
  padding: 8px;
  margin-right: 10px;
  font-size: 16px;
}

.search-box button {
  padding: 8px 14px;
  background-color: #4a148c;
  color: white;
  border: none;
  cursor: pointer;
}

.search-box button:hover {
  background-color: #6a1b9a;
}

.results {
  margin-top: 20px;
}

.results ul {
  list-style-type: none;
  padding: 0;
}

.results li {
  background-color: #f0f0f0;
  padding: 10px;
  margin-bottom: 5px;
}
</style>
