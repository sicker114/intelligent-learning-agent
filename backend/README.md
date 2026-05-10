基于SpringAi的智能文档问答系统，支持PDF文档上传、解析、向量存储和智能问答功能。

## 🚀 项目简介

ChatPDF Backend是一个基于Java Spring Boot框架开发的智能文档处理与问答系统。该系统能够：
- 上传并解析PDF文档
- 将文档内容向量化存储到Qdrant向量数据库
- 提供基于AI的智能问答服务
- 支持多种意图识别和工具调用
- 实现会话历史管理

## 🛠️ 技术栈

- **后端框架**: Spring Boot 3.3.5
- **编程语言**: Java 17
- **AI框架**: Spring AI 1.0.0
- **向量数据库**: Qdrant
- **大语言模型**: 通义千问 (通过阿里云DashScope)
- **文档处理**: Apache PDFBox
- **网页抓取**: Jsoup
- **Excel/Word处理**: Apache POI
- **缓存/会话存储**: Redis
- **构建工具**: Maven

## 📋 核心功能

### 1. 文档处理
- PDF文件上传与解析
- 文本分块与向量化
- 文档内容检索

### 2. 智能问答
- 基于文档内容的精准问答
- 多轮对话支持
- 流式响应输出

### 3. 意图识别
- 学习答疑 (DOCUMENT_QUERY)
- 日常闲聊 (GENERAL_CHAT)
- 内容总结 (SUMMARY)
- 对比分析 (COMPARISON)
- 信息提取 (EXTRACT)

### 4. 工具集成
- 实时日期时间查询
- 网页内容抓取
- 网络搜索功能

### 5. 会话管理
- 基于Redis的会话存储
- 历史记录追踪
- 会话生命周期管理

## ⚙️ 环境要求

- JDK 17+
- Maven 3.6+
- Redis服务器
- Qdrant向量数据库
- 阿里云API密钥

## 🔧 安装配置

### 1. 克隆项目
bash git clone https://github.com/your-username/chatpdf-backend.git
cd chatpdf-backend/backend
### 2. 配置环境变量
编辑 `src/main/resources/application.yml` 文件：
yaml 
spring:
ai: 
openai: 
api-key: 
your-api-key-here # 替换为您的阿里云API密钥 
base-url: https://dashscope.aliyuncs.com/compatible-mode 
chat: 
options: 
model: qwen-plus 
embedding: options: model: text-embedding-v3 
dimensions: 1024 
vectorstore: qdrant: 
base-url: http://localhost:6334 # Qdrant服务地址 
api-key: "" 
init-schema: true
### 3. 启动依赖服务
确保以下服务正在运行：
- Redis服务器 (默认端口6379)
- Qdrant向量数据库 (默认端口6334)

### 4. 编译运行
bash 
mvn clean install 
mvn spring-boot:run
应用将在 `http://localhost:8080` 上启动。

## 📡 API接口

### 文件管理 (`/api/files`)
- `POST /upload` - 上传PDF文件
- `GET /list` - 获取已上传文件列表
- `GET /download/{filename}` - 下载原始PDF文件

### 聊天功能 (`/api/chat`)
- `POST /stream` - 发送消息并接收流式响应

### 会话管理 (`/api/session`)
- `GET /{sessionId}` - 获取指定会话历史
- `GET /list` - 获取所有会话ID列表
- `DELETE /{sessionId}` - 删除指定会话
- `DELETE /clear` - 清空所有会话

## 🏗️ 项目结构
src/main/java/com/chatpdf/backend/ ├── Config/ │ └── RedisConfig.java # Redis配置 ├── Controller/ │ ├── ChatController.java # 聊天接口 │ ├── FileController.java # 文件管理接口 │ └── SessionController.java # 会话管理接口 ├── Service/ │ ├── AgentOrchestratorService.java # Agent协调器 │ ├── DocumentService.java # 文档处理服务 │ ├── IntentRecognitionService.java # 意图识别服务 │ ├── SessionHistoryService.java # 会话历史服务 │ ├── ToolService.java # 工具服务 │ ├── ChatMessage.java # 聊天消息实体 │ └── ChatSession.java # 会话实体 ├── Utils/ └── BackendApplication.java # 应用入口

## 🧪 测试

运行单元测试：
bash 
mvn test
## 🤝 贡献指南

欢迎提交Issue和Pull Request来改进本项目。

## 📄 许可证

本项目采用MIT许可证。

## 👥 作者

祝旭东 - 项目负责人




