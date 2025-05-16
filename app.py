from flask import Flask, render_template, request, redirect, url_for
import pymysql
from flask import request, flash
from datetime import datetime


app = Flask(__name__)

DB_CONFIG = {
    "host": "106.12.170.52",
    "port": 13327,
    "user": "lmx",
    "password": "lmx",
    "database": "sparkprogram",
    "charset": "utf8mb4"
}

def get_db_connection():
    return pymysql.connect(
        host=DB_CONFIG["host"],
        port=DB_CONFIG["port"],
        user=DB_CONFIG["user"],
        password=DB_CONFIG["password"],
        database=DB_CONFIG["database"],
        charset=DB_CONFIG["charset"]
    )

@app.route('/')
def home():
    """显示欢迎首页"""
    return render_template('home.html')

"""
国内业务部分
"""
@app.route('/china')
def show_china():
    """中国疫情主页面"""
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT DISTINCT province FROM china_epidemic ORDER BY province")
    provinces = [row[0] for row in cursor.fetchall()]
    conn.close()
    return render_template('china.html', provinces=provinces)

@app.route('/province/<province>')
def show_province_dates(province):
    print(province)
    # 查询该省所有可用日期
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT DISTINCT report_date FROM china_epidemic WHERE province=%s ORDER BY report_date DESC", (province,))
    dates = [row[0].strftime("%Y-%m-%d") if hasattr(row[0], 'strftime') else str(row[0]) for row in cursor.fetchall()]
    conn.close()
    return render_template('china_dates.html', province=province, dates=dates)

@app.route('/province/<province>/<date>')
def show_province_data(province, date):
    conn = get_db_connection()
    cursor = conn.cursor()
    sql = '''
        SELECT city, new_confirmed, new_deaths, new_recovered, suspected FROM china_epidemic
        WHERE province = %s AND report_date = %s
    '''
    cursor.execute(sql, (province, date))
    rows = cursor.fetchall()
    conn.close()

    if not rows:
        result_text = f'<div class="no-data">暂未收录{date}{province}省的疫情数据</div>'
    else:
        confirmed = "，".join([f"{city} 新增 {new_confirmed}例" for city, new_confirmed, *_ in rows])
        deaths = "，".join([f"{city} 新增 {new_death}例" for city, _, new_death, *_ in rows])
        cured = "，".join([f"{city} 新增 {new_cured}例" for city, *_, new_cured, _ in rows])
        suspected = "，".join([f"{city} 新增 {new_suspected}例" for city, *_, new_suspected in rows])

        result_text = f'''
               <div class="data-section">
                   <div class="data-item confirmed">新增确诊病例：{confirmed}</div>
                   <div class="data-item death">新增死亡病例：{deaths}</div>
                   <div class="data-item cured">新增治愈出院：{cured}</div>
                   <div class="data-item suspected">新增疑似病例：{suspected}</div>
               </div>
           '''

    return render_template("china_result.html",
                           province=province,
                           date=date,
                           result_text=result_text)

@app.route('/admin/add_data', methods=['GET', 'POST'])
def add_china_epidemic_data():
    """疫情数据录入界面"""
    if request.method == 'POST':
        # 获取表单数据
        report_date = request.form.get('report_date')
        province = request.form.get('province')
        city = request.form.get('city')
        new_confirmed = request.form.get('new_confirmed')
        new_deaths = request.form.get('new_deaths')
        new_recovered = request.form.get('new_recovered')
        suspected = request.form.get('suspected')

        # 默认国家为中国
        country = "中国"

        # 数据验证
        errors = []
        try:
            datetime.strptime(report_date, '%Y-%m-%d')
        except ValueError:
            errors.append("日期格式不正确（应YYYY-MM-DD）")

        numeric_fields = {
            "新增确诊": new_confirmed,
            "新增死亡": new_deaths,
            "新增治愈": new_recovered,
            "疑似病例": suspected
        }

        for field, value in numeric_fields.items():
            if not value.isdigit():
                errors.append(f"{field}必须为整数")
            elif int(value) < 0:
                errors.append(f"{field}不能为负数")

        if errors:
            return render_template('add_data_china.html',
                                   errors=errors,
                                   provinces=get_provinces())

        # 检查 report_date 和 city 是否已存在
        conn = get_db_connection()
        try:
            with conn.cursor() as cursor:
                cursor.execute("""
                    SELECT COUNT(*) FROM china_epidemic 
                    WHERE report_date = %s AND city = %s
                """, (report_date, city))
                (count,) = cursor.fetchone()
                if count > 0:
                    errors.append(f"{report_date}日期，城市'{city}'的数据已存在，不能重复插入。")
                    return render_template('add_data_china.html',
                                           errors=errors,
                                           provinces=get_provinces())

                # 插入数据
                sql = """
                INSERT INTO china_epidemic 
                (report_date, province, city, country, new_confirmed, new_deaths, new_recovered, suspected)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
                """
                cursor.execute(sql, (
                    report_date,
                    province,
                    city,
                    country,
                    int(new_confirmed),
                    int(new_deaths),
                    int(new_recovered),
                    int(suspected)
                ))
            conn.commit()
            return redirect(url_for('add_china_success'))
        except pymysql.Error as e:
            conn.rollback()
            flash(f"数据库错误: {str(e)}")
            return render_template('add_data_china.html',
                                   errors=["保存失败"],
                                   provinces=get_provinces())
        finally:
            conn.close()

    return render_template('add_data_china.html',
                           provinces=get_provinces(),
                           errors=None)

@app.route('/admin/add_china_success')
def add_china_success():
    """数据添加成功页"""
    return render_template('add_data_china_success.html')


def get_provinces():
    """获取省份列表的公共函数"""
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT DISTINCT province FROM china_epidemic ORDER BY province")
    provinces = [row[0] for row in cursor.fetchall()]
    conn.close()
    return provinces

"""
世界业务部分
"""
@app.route('/global')
def show_global():
    """全球疫情主页：列出所有国家"""
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT DISTINCT country FROM global_epidemic ORDER BY country")
    countries = [row[0] for row in cursor.fetchall()]
    conn.close()
    return render_template('global.html', countries=countries)

@app.route('/global/<country>')
def show_country_dates(country):
    """显示某国家所有可用日期"""
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT DISTINCT date FROM global_epidemic WHERE country=%s ORDER BY date DESC", (country,))
    dates = [row[0].strftime("%Y-%m-%d") for row in cursor.fetchall()]
    conn.close()
    return render_template('global_dates.html', country=country, dates=dates)

@app.route('/global/<country>/<date>')
def show_country_data(country, date):
    """显示某国家某日数据详情"""
    conn = get_db_connection()
    cursor = conn.cursor()
    
    # 查询该国家该日期的疫情数据
    sql = """
        SELECT region_code, new_confirmed, total_confirmed, new_death, total_death 
        FROM global_epidemic 
        WHERE country = %s AND date = %s
    """
    try:
        cursor.execute(sql, (country, date))
        rows = cursor.fetchall()
    except Exception as e:
        conn.close()
        return f"<div class='error'>查询出错：{str(e)}</div>"

    conn.close()

    if not rows:
        result_text = f"<div class='no-data'>暂未收录 {date} {country} 的疫情数据。</div>"
    else:
        confirmed = "，".join([f"{r[1]}例" for r in rows])         # 新增确诊
        total = "，".join([f"{r[2]}例" for r in rows])             # 累计确诊
        deaths = "，".join([f"{r[3]}例" for r in rows])            # 新增死亡
        total_death = "，".join([f"{r[4]}例" for r in rows])       # 累计死亡

        result_text = f'''
        <div class="data-section">
            <div class="data-item confirmed">新增确诊病例：{confirmed}</div>
            <div class="data-item total-confirmed">累计确诊病例：{total}</div>
            <div class="data-item death">新增死亡病例：{deaths}</div>
            <div class="data-item total-death">累计死亡病例：{total_death}</div>
        </div>
        '''

    return render_template("country_result.html",
                           country=country,
                           date=date,
                           result_text=result_text)


from flask import render_template, request, redirect, url_for
from datetime import datetime
import pymysql

@app.route('/admin/add_global_success')
def add_global_success():
    """数据添加成功页"""
    return render_template('add_data_global_success.html')

@app.route('/admin/add_global_data', methods=['GET', 'POST'])
def add_global_epidemic_data():
    countries = [
        {'code': 'US', 'name': '美国', 'region': 'NA'},
        {'code': 'GB', 'name': '英国', 'region': 'EU'},
        {'code': 'FR', 'name': '法国', 'region': 'EU'},
        {'code': 'DE', 'name': '德国', 'region': 'EU'},
        {'code': 'IN', 'name': '印度', 'region': 'AS'},
        {'code': 'JP', 'name': '日本', 'region': 'AS'},
        {'code': 'BR', 'name': '巴西', 'region': 'SA'},
        {'code': 'RU', 'name': '俄罗斯', 'region': 'EU'},
        {'code': 'ZA', 'name': '南非', 'region': 'AF'},
        {'code': 'AU', 'name': '澳大利亚', 'region': 'OC'},
        {'code': 'CN', 'name': '中国', 'region': 'AS'},
    ]

    errors = []

    if request.method == 'POST':
        report_date = request.form.get('report_date')
        country_name = request.form.get('country')
        new_confirmed = request.form.get('new_confirmed')
        total_confirmed = request.form.get('total_confirmed')
        new_death = request.form.get('new_deaths')
        total_death = request.form.get('total_deaths')

        # 查找国家信息
        country_info = next((c for c in countries if c['name'] == country_name), None)
        if not country_info:
            errors.append("请选择有效的国家")

        # 日期格式校验
        try:
            if not report_date:
                raise ValueError("日期为空")
            datetime.strptime(report_date, '%Y-%m-%d')
        except (ValueError, TypeError):
            errors.append("日期格式不正确（应为 YYYY-MM-DD）")

        # 数字字段校验
        numeric_fields = {
            "新增确诊": new_confirmed,
            "累计确诊": total_confirmed,
            "新增死亡": new_death,
            "累计死亡": total_death,
        }

        for label, value in numeric_fields.items():
            if value is None or value.strip() == '':
                errors.append(f"{label}不能为空")
            else:
                try:
                    ivalue = int(value)
                    if ivalue < 0:
                        errors.append(f"{label}不能为负数")
                except ValueError:
                    errors.append(f"{label}必须为非负整数")

        if errors:
            return render_template('add_data_global.html', errors=errors, countries=[c['name'] for c in countries])

        conn = get_db_connection()
        try:
            with conn.cursor() as cursor:
                # 唯一性检查：相同日期和国家不允许重复插入
                check_sql = """
                    SELECT COUNT(*) FROM global_epidemic 
                    WHERE date = %s AND country = %s
                """
                cursor.execute(check_sql, (report_date, country_info['name']))
                count = cursor.fetchone()[0]
                if count > 0:
                    errors.append("该日期和国家的数据已存在，不能重复添加。")
                    return render_template('add_data_global.html', errors=errors, countries=[c['name'] for c in countries])

                # 插入数据
                insert_sql = """
                    INSERT INTO global_epidemic 
                    (date, country_code, country, region_code, new_confirmed, total_confirmed, new_death, total_death)
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
                """
                cursor.execute(insert_sql, (
                    report_date,
                    country_info['code'],
                    country_info['name'],
                    country_info['region'],
                    int(new_confirmed),
                    int(total_confirmed),
                    int(new_death),
                    int(total_death),
                ))
            conn.commit()
            return redirect(url_for('add_global_success'))
        except pymysql.MySQLError as e:
            conn.rollback()
            errors.append(f"数据库错误：{str(e)}")
            return render_template('add_data_global.html', errors=errors, countries=[c['name'] for c in countries])
        finally:
            conn.close()

    # GET请求或无提交时，展示页面
    return render_template('add_data_global.html', countries=[c['name'] for c in countries], errors=errors)

if __name__ == '__main__':
    app.run(debug=True, host="0.0.0.0", port=5000)
