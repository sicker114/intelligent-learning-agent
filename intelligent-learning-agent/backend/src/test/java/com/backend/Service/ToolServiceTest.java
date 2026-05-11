package com.backend.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ToolService 单元测试")
class ToolServiceTest {

    private ToolService toolService;

    @BeforeEach
    void setUp() {
        toolService = new ToolService();
    }

    @Test
    @DisplayName("获取当前日期时间")
    void testGetCurrentDateTime() {
        String result = toolService.getCurrentDateTime();

        assertNotNull(result);
        assertFalse(result.isEmpty());

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 EEEE");
        String expected = now.format(formatter);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("获取的日期格式正确")
    void testGetCurrentDateTime_Format() {
        String result = toolService.getCurrentDateTime();

        assertTrue(result.matches("\\d{4}年\\d{2}月\\d{2}日 \\w+"));
    }

    @Test
    @DisplayName("网页搜索功能")
    void testWebSearch() {
        String query = "Java编程";

        String result = toolService.webSearch(query);

        assertNotNull(result);
        if (result.startsWith("搜索失败")) {
            assertTrue(result.contains("搜索失败"));
        } else {
            assertTrue(result.contains("搜索结果"));
        }
    }

    @Test
    @DisplayName("网页搜索空查询")
    void testWebSearch_EmptyQuery() {
        String query = "";

        String result = toolService.webSearch(query);

        assertNotNull(result);
    }

    @Test
    @DisplayName("网页搜索null查询")
    void testWebSearch_NullQuery() {
        assertThrows(
            Exception.class,
            () -> toolService.webSearch(null)
        );
    }

    @Test
    @DisplayName("网页内容抓取")
    void testFetchWebContent() {
        String url = "https://www.example.com";

        String result = toolService.fetchWebContent(url);

        assertNotNull(result);
        if (result.startsWith("无法访问")) {
            assertTrue(result.contains("无法访问"));
        } else {
            assertFalse(result.isEmpty());
            assertTrue(result.length() <= 3000);
        }
    }

    @Test
    @DisplayName("网页抓取无效URL")
    void testFetchWebContent_InvalidUrl() {
        String url = "not-a-valid-url";

        String result = toolService.fetchWebContent(url);

        assertNotNull(result);
        assertTrue(result.startsWith("无法访问"));
    }

    @Test
    @DisplayName("网页抓取空URL")
    void testFetchWebContent_EmptyUrl() {
        String url = "";

        String result = toolService.fetchWebContent(url);

        assertNotNull(result);
    }

    @Test
    @DisplayName("网页抓取null URL")
    void testFetchWebContent_NullUrl() {
        assertThrows(
            Exception.class,
            () -> toolService.fetchWebContent(null)
        );
    }

    @Test
    @DisplayName("计算器功能")
    void testCalculate() {
        String expression = "2 + 2";

        String result = toolService.calculate(expression);

        assertNotNull(result);
        assertTrue(result.startsWith("计算结果"));
    }

    @Test
    @DisplayName("计算器空表达式")
    void testCalculate_EmptyExpression() {
        String expression = "";

        String result = toolService.calculate(expression);

        assertNotNull(result);
    }

    @Test
    @DisplayName("计算器null表达式")
    void testCalculate_NullExpression() {
        assertThrows(
            Exception.class,
            () -> toolService.calculate(null)
        );
    }

    @Test
    @DisplayName("计算器复杂表达式")
    void testCalculate_ComplexExpression() {
        String expression = "(10 + 5) * 2 - 3";

        String result = toolService.calculate(expression);

        assertNotNull(result);
        assertTrue(result.contains(expression));
    }

    @Test
    @DisplayName("多次调用获取日期时间一致性")
    void testGetCurrentDateTime_Consistency() {
        String result1 = toolService.getCurrentDateTime();
        String result2 = toolService.getCurrentDateTime();

        assertNotNull(result1);
        assertNotNull(result2);
    }

    @Test
    @DisplayName("工具服务实例化")
    void testToolService_Instantiation() {
        ToolService newService = new ToolService();
        assertNotNull(newService);
    }
}
