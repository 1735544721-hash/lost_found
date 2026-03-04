/**
 * 日期格式化工具类
 */
export default class DateUtils {
  /**
   * 格式化日期
   * @param {Date|string|number} date 日期对象/日期字符串/时间戳
   * @param {string} format 格式化模式 支持：
   * YYYY: 年
   * MM: 月
   * DD: 日
   * HH: 时
   * mm: 分
   * ss: 秒
   * @returns {string} 格式化后的日期字符串
   */
  static format(date, format = 'YYYY-MM-DD') {
    // 转换输入日期为Date对象
    const dateObj = date instanceof Date ? date : new Date(date);
    
    // 如果日期无效则返回空字符串
    if (isNaN(dateObj.getTime())) {
      return '';
    }

    // 获取日期各个部分
    const year = dateObj.getFullYear();
    const month = dateObj.getMonth() + 1;
    const day = dateObj.getDate();
    const hours = dateObj.getHours();
    const minutes = dateObj.getMinutes();
    const seconds = dateObj.getSeconds();

    // 补零函数
    const padZero = (num) => num.toString().padStart(2, '0');

    // 替换格式字符串
    return format
      .replace('YYYY', year)
      .replace('MM', padZero(month))
      .replace('DD', padZero(day))
      .replace('HH', padZero(hours))
      .replace('mm', padZero(minutes))
      .replace('ss', padZero(seconds));
  }

  /**
   * 格式化为年月 YYYY-MM
   * @param {Date|string|number} date 日期
   * @returns {string}
   */
  static formatYearMonth(date) {
    return this.format(date, 'YYYY-MM');
  }

  /**
   * 格式化为年月日 YYYY-MM-DD
   * @param {Date|string|number} date 日期
   * @returns {string}
   */
  static formatDate(date) {
    return this.format(date, 'YYYY-MM-DD');
  }

  /**
   * 格式化为完整日期时间 YYYY-MM-DD HH:mm:ss
   * @param {Date|string|number} date 日期
   * @returns {string}
   */
  static formatDateTime(date) {
    return this.format(date, 'YYYY-MM-DD HH:mm:ss');
  }
} 