package com.chatpdf.backend.Service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class ToolService {

    /**
     * 工具1：网页搜索
     */
    public String webSearch(String query) {
        try {
            // 伪装成浏览器去 DuckDuckGo 搜索(简易版，可以申请APIkey来实现）
            Document doc = Jsoup.connect("https://html.duckduckgo.com/html/")
                    .data("q", query)
                    .userAgent("Mozilla/5.0")
                    .post();

            StringBuilder result = new StringBuilder("搜索结果：\n");
            // 提取搜索结果中的标题和链接（根据 DDG 的 HTML 结构选择器）
            doc.select(".result__title").forEach(element -> {
                result.append("- ").append(element.text()).append("\n");
            });
            return result.toString();
        } catch (IOException e) {
            return "搜索失败: " + e.getMessage();
        }
    }


    /**
     * 工具2：获取当前日期时间
     */
    public String getCurrentDateTime() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 EEEE");
        return now.format(formatter);
    }

    /**
     * 工具3：网页内容抓取
     */
    public String fetchWebContent(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .timeout(5000)
                    .userAgent("Mozilla/5.0")
                    .get();

            // 清理 HTML，只保留文本
            String cleanText = Jsoup.clean(doc.body().text(), Safelist.none());
            return cleanText.substring(0, Math.min(cleanText.length(), 3000)); // 限制长度
        } catch (IOException e) {
            return "无法访问该网页: " + e.getMessage();
        }
    }

    /**
     * 工具4：计算器
     */
    public String calculate(String expression) {
        try {
            // 简单表达式计算（生产环境建议使用专业库）
            // 这里仅作示例
            return "计算结果: " + expression + " = [需要实现表达式解析器]";
        } catch (Exception e) {
            return "计算失败: " + e.getMessage();
        }
    }
}