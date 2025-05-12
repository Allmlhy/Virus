<template>
  <div class="domestic">
    <NavBar />
    <div class="container">
      <h2>国内疫情指标</h2>
      <p>这里显示国内疫情的相关数据。</p>
      <div class="map-container" style="height: 400px; background-color: #f0f0f0;">
        <p>占位地图，待加载...</p>
      </div>
      <div class="data-container">
        <div class="data-box">
          <h3>国内每日死亡数</h3>
          <p>模拟数据：800</p>
        </div>
        <div class="data-box">
          <h3>国内每日确诊数</h3>
          <p>模拟数据：4000</p>
        </div>
        <div class="data-box">
          <h3>国内总接种数量</h3>
          <p>模拟数据：7000</p>
        </div>
        <div class="data-box">
          <h3>国内每日治愈数</h3>
          <p>模拟数据：2500</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import NavBar from "@/components/NavBar.vue";
</script>

<style scoped>
.container {
  margin: 20px;
}

h2 {
  text-align: center;
}

.map-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 18px;
  color: #666;
}

.data-container {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr;
  gap: 20px;
  margin-top: 20px;
}

.data-box {
  background-color: #fff;
  border: 1px solid #ddd;
  padding: 10px;
  text-align: center;
}

.data-box h3 {
  margin: 0;
  color: #333;
}

.data-box p {
  margin: 10px 0 0;
  color: #555;
}
</style>
