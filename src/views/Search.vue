<template>
  <div class="search">
    <NavBar />
    <div class="container">

      <!-- 省市选择及按钮 -->
      <div class="test-select">
        <label for="province">选择省份：</label>
        <select id="province" v-model="selectedProvince" @change="onProvinceChange">
          <option value="" disabled>请选择省份</option>
          <option v-for="prov in provinces" :key="prov" :value="prov">{{ prov }}</option>
        </select>

        <label for="city" style="margin-left:20px;">选择城市：</label>
        <select
            id="city"
            v-model="selectedCity"
            :disabled="!selectedProvince || cities.length === 0"
            @change="onCityChange"
        >
          <option value="" disabled>请选择城市</option>
          <option v-for="city in cities" :key="city" :value="city">{{ city }}</option>
        </select>

        <!-- 模型选择 -->
        <label for="model" style="margin-left:20px;">预测模型：</label>
        <select id="model" v-model="selectedModel">
          <option value="tcn">TCN</option>
          <option value="lstm">LSTM</option>
          <option value="gru">GRU</option>
          <option value="transformer">Transformer</option>
        </select>


        <!-- 开始预测按钮 -->
        <button
            class="predict-btn"
            @click="predictData"
            :disabled="!selectedProvince || !selectedCity"
        >
          开始预测
        </button>
      </div>

      <!-- 上传状态 -->
      <p v-if="uploadStatus" class="upload-status">{{ uploadStatus }}</p>

      <!-- 总趋势图 -->
      <div class="chart-section">
        <h3>疫情趋势图</h3>
        <div id="mainChart" style="width: 100%; height: 400px;"></div>
      </div>

      <!-- 每日新增图 -->
      <div class="chart-section">
        <h3>每日新增确诊与死亡</h3>
        <div id="dailyChart" style="width: 100%; height: 400px;"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import NavBar from "@/components/NavBar.vue";
import axios from "axios";
import * as echarts from "echarts";

const provinces = ref([
  "河北", "山西", "内蒙古", "辽宁", "吉林", "黑龙江",
  "江苏", "浙江", "安徽", "福建", "江西", "山东",
  "河南", "湖北", "湖南", "广东", "广西", "海南",
  "四川", "贵州", "云南", "西藏", "陕西", "甘肃",
  "青海", "宁夏", "新疆", "北京", "上海"
]);

const cityMap = {
  河北: ["石家庄", "唐山", "秦皇岛", "邯郸", "邢台", "保定", "张家口", "承德", "沧州", "廊坊", "衡水"],
  山西: ["太原", "大同", "阳泉", "长治", "晋城", "朔州", "晋中", "运城", "忻州", "临汾", "吕梁"],
  内蒙古: ["呼和浩特", "包头", "乌海", "赤峰", "通辽", "鄂尔多斯", "呼伦贝尔", "巴彦淖尔", "乌兰察布"],
  辽宁: ["沈阳", "大连", "鞍山", "抚顺", "本溪", "丹东", "锦州", "营口", "阜新", "辽阳", "盘锦", "铁岭", "朝阳", "葫芦岛"],
  吉林: ["长春", "吉林", "四平", "辽源", "通化", "白山", "松原", "白城"],
  黑龙江: ["哈尔滨", "齐齐哈尔", "鸡西", "鹤岗", "双鸭山", "大庆", "伊春", "佳木斯", "七台河", "牡丹江", "黑河", "绥化"],
  江苏: ["南京", "无锡", "徐州", "常州", "苏州", "南通", "连云港", "淮安", "盐城", "扬州", "镇江", "泰州", "宿迁"],
  浙江: ["杭州", "宁波", "温州", "嘉兴", "湖州", "绍兴", "金华", "衢州", "舟山", "台州", "丽水"],
  安徽: ["合肥", "芜湖", "蚌埠", "淮南", "马鞍山", "淮北", "铜陵", "安庆", "黄山", "阜阳", "宿州", "滁州", "六安", "宣城", "池州", "亳州"],
  福建: ["福州", "厦门", "莆田", "三明", "泉州", "漳州", "南平", "龙岩", "宁德"],
  江西: ["南昌", "景德镇", "萍乡", "九江", "抚州", "鹰潭", "赣州", "吉安", "宜春", "新余", "上饶"],
  山东: ["济南", "青岛", "淄博", "枣庄", "东营", "烟台", "潍坊", "济宁", "泰安", "威海", "日照", "临沂", "德州", "聊城", "滨州", "菏泽"],
  河南: ["郑州", "开封", "洛阳", "平顶山", "安阳", "鹤壁", "新乡", "焦作", "濮阳", "许昌", "漯河", "三门峡", "南阳", "商丘", "信阳", "周口", "驻马店"],
  湖北: ["武汉", "黄石", "十堰", "宜昌", "襄阳", "鄂州", "荆门", "孝感", "荆州", "黄冈", "咸宁", "随州"],
  湖南: ["长沙", "株洲", "湘潭", "衡阳", "邵阳", "岳阳", "常德", "张家界", "益阳", "郴州", "永州", "怀化", "娄底"],
  广东: ["广州", "韶关", "深圳", "珠海", "汕头", "佛山", "江门", "湛江", "茂名", "肇庆", "惠州", "梅州", "汕尾", "河源", "阳江", "清远", "东莞", "中山", "潮州", "揭阳", "云浮"],
  广西: ["南宁", "柳州", "桂林", "梧州", "北海", "防城港", "钦州", "贵港", "玉林", "百色", "贺州", "河池", "来宾", "崇左"],
  海南: ["海口", "三亚", "三沙", "儋州"],
  四川: ["成都", "自贡", "攀枝花", "泸州", "德阳", "绵阳", "广元", "遂宁", "内江", "乐山", "南充", "眉山", "宜宾", "广安", "达州", "雅安", "巴中", "资阳"],
  贵州: ["贵阳", "六盘水", "遵义", "安顺", "毕节", "铜仁"],
  云南: ["昆明", "曲靖", "玉溪", "保山", "昭通", "丽江", "普洱", "临沧"],
  西藏: ["拉萨", "日喀则", "昌都", "林芝", "山南", "那曲"],
  陕西: ["西安", "铜川", "宝鸡", "咸阳", "渭南", "延安", "汉中", "榆林", "安康", "商洛"],
  甘肃: ["兰州", "嘉峪关", "金昌", "白银", "天水", "武威", "张掖", "平凉", "酒泉", "庆阳", "定西", "陇南"],
  青海: ["西宁", "海东"],
  宁夏: ["银川", "石嘴山", "吴忠", "固原", "中卫"],
  新疆: ["乌鲁木齐", "克拉玛依", "吐鲁番", "哈密"],
  北京: ["北京"],
  上海: ["上海"]
};


const cities = ref([]);
const selectedProvince = ref("");
const selectedCity = ref("");
const selectedModel = ref("tcn");
const uploadStatus = ref("");

let myChart = null;
let dailyChart = null;

const onProvinceChange = () => {
  cities.value = selectedProvince.value ? cityMap[selectedProvince.value] || [] : [];
  selectedCity.value = "";
  clearCharts();
};

const onCityChange = () => {
  clearCharts();
  loadChartData();
  loadDailyChartData();
};

const clearCharts = () => {
  if (myChart) myChart.clear();
  if (dailyChart) dailyChart.clear();
};

const loadChartData = () => {
  if (!selectedProvince.value || !selectedCity.value) return clearCharts();

  axios.get("http://localhost:8082/api/region-stats/history", {
    params: { province: selectedProvince.value, city: selectedCity.value },
  })
      .then((res) => {
        const historyData = res.data;
        const dates = historyData.map((i) => i.date);
        const confirmed = historyData.map((i) => i.totalConfirmed);
        const deaths = historyData.map((i) => i.totalDeaths);
        const recovered = historyData.map((i) => i.totalRecovered);

        const option = {
          animationDuration: 10000,
          title: { text: "疫情发展趋势" },
          tooltip: { trigger: "axis" },
          legend: { data: ["确诊", "死亡", "治愈"] },
          xAxis: { type: "category", data: dates },
          yAxis: { type: "value" },
          series: [
            { name: "确诊", type: "line", data: confirmed },
            { name: "死亡", type: "line", data: deaths },
            { name: "治愈", type: "line", data: recovered },
          ],
          dataZoom: [
            { type: 'slider', start: 80, end: 100 },
            { type: 'inside', start: 80, end: 100 }
          ]
        };

        if (!myChart) myChart = echarts.init(document.getElementById("mainChart"));
        myChart.setOption(option);
      })
      .catch(console.error);
};

const loadDailyChartData = () => {
  if (!selectedProvince.value || !selectedCity.value) return clearCharts();

  axios.get("http://localhost:8082/api/region-stats/daily", {
    params: { province: selectedProvince.value, city: selectedCity.value },
  })
      .then((res) => {
        const dailyData = res.data;
        renderDailyChart(dailyData);
      })
      .catch(console.error);
};

const predictData = async () => {
  if (!selectedProvince.value || !selectedCity.value) {
    uploadStatus.value = "请先选择省份和城市！";
    return;
  }

  try {
    uploadStatus.value = "正在准备预测数据...";
    const res = await axios.get("http://localhost:8082/api/region-stats/daily", {
      params: { province: selectedProvince.value, city: selectedCity.value },
    });

    const dailyData = res.data;
    const lastTwoWeeks = dailyData.slice(-14).map((d) => ({
      date: d.date,
      newConfirmed: d.newConfirmed,
      newDeaths: d.newDeaths,
    }));

    const modelUrlMap = {
      lstm: "http://localhost:8000/predict_lstm/",
      tcn: "http://localhost:8000/predict_tcn/",
      gru: "http://localhost:8000/predict_gru/",
      transformer: "http://localhost:8000/predict_transformer/",
    };

    const predictUrl = modelUrlMap[selectedModel.value] || modelUrlMap["lstm"];
    const predictRes = await axios.post(predictUrl, lastTwoWeeks);


    if (predictRes.status === 200 && Array.isArray(predictRes.data)) {
      uploadStatus.value = "预测成功！";
      const updatedData = [...dailyData];
      predictRes.data.forEach(pred => {
        updatedData.push({
          date: pred.date,
          newConfirmed: pred.predictedConfirmed,
          newDeaths: pred.predictedDeaths
        });
      });
      renderDailyChart(updatedData);
    } else {
      uploadStatus.value = "预测失败，接口返回异常";
    }
  } catch (e) {
    uploadStatus.value = "预测失败，发生错误";
    console.error(e);
  }
};

const renderDailyChart = (dailyData) => {
  const dates = dailyData.map((d) => d.date);
  const newConfirmed = dailyData.map((d) => d.newConfirmed);
  const newDeaths = dailyData.map((d) => d.newDeaths);

  const option = {
    animationDuration: 10000,
    title: { text: "每日新增确诊与死亡（含预测）" },
    tooltip: { trigger: "axis" },
    legend: { data: ["新增确诊", "新增死亡"] },
    xAxis: { type: "category", data: dates },
    yAxis: { type: "value" },
    series: [
      { name: "新增确诊", type: "line", data: newConfirmed },
      { name: "新增死亡", type: "line", data: newDeaths },
    ],
    dataZoom: [
      { type: 'slider', start: 80, end: 100 },
      { type: 'inside', start: 80, end: 100 }
    ]
  };

  if (!dailyChart) dailyChart = echarts.init(document.getElementById("dailyChart"));
  dailyChart.setOption(option);
};
</script>

<style scoped>
.search {
  padding-top: 50px;
  background-color: #f5f7fa;
  min-height: 100vh;
  font-family: 'Helvetica Neue', sans-serif;
}

.container {
  max-width: 1000px;
  margin: auto;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.test-select {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
}

.test-select label {
  font-weight: bold;
  margin-right: 6px;
}

.test-select select {
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #ccc;
  font-size: 14px;
  background-color: #fdfdfd;
  transition: border-color 0.3s;
}

.test-select select:focus {
  border-color: #409eff;
  outline: none;
}

.predict-btn {
  padding: 10px 20px;
  background-color: #409eff;
  border: none;
  border-radius: 6px;
  color: white;
  font-weight: bold;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.predict-btn:hover:not(:disabled) {
  background-color: #66b1ff;
}

.upload-status {
  color: #333;
  font-size: 14px;
  margin-bottom: 10px;
}

.chart-section {
  margin-top: 30px;
}
</style>
