# Equiflux Block Explorer

Equiflux区块链浏览器是一个完整的Web应用程序，用于浏览和查询Equiflux区块链的数据。

## 功能特性

### 🔍 核心功能
- **区块查询**: 按高度、哈希查询区块详情
- **交易查询**: 按哈希查询交易详情
- **账户查询**: 查看账户余额、质押、交易历史
- **搜索功能**: 智能搜索区块、交易、账户
- **实时数据**: 自动刷新最新信息

### 📊 统计信息
- **链状态**: 当前高度、轮次、总供应量
- **网络统计**: 节点数量、连接状态
- **性能指标**: 出块时间、TPS等

### 🎨 用户界面
- **响应式设计**: 支持桌面和移动设备
- **现代化UI**: 基于Bootstrap 5的现代界面
- **实时更新**: 自动刷新数据
- **交互功能**: 复制哈希、链接跳转等

## 技术架构

### 后端组件
- **BlockExplorerService**: 核心业务逻辑服务
- **BlockExplorerController**: Web控制器和API接口
- **DTO类**: 数据传输对象
- **异常处理**: 统一的异常管理

### 前端组件
- **HTML模板**: Thymeleaf模板引擎
- **Bootstrap 5**: UI框架
- **JavaScript**: 前端交互逻辑
- **Font Awesome**: 图标库

## API接口

### 区块相关
```
GET /explorer/api/latest-block          # 获取最新区块
GET /explorer/api/block/height/{height} # 按高度获取区块
GET /explorer/api/block/hash/{hash}     # 按哈希获取区块
GET /explorer/api/blocks                # 获取区块列表（分页）
GET /explorer/api/blocks/range          # 获取区块范围
```

### 交易相关
```
GET /explorer/api/transaction/{hash}           # 按哈希获取交易
GET /explorer/api/transactions/address/{addr}  # 获取地址交易历史
GET /explorer/api/transactions/recent          # 获取最近交易
```

### 账户相关
```
GET /explorer/api/account/{address}      # 获取账户详情
GET /explorer/api/account/{addr}/balance # 获取账户余额
GET /explorer/api/account/{addr}/stake   # 获取账户质押
```

### 统计相关
```
GET /explorer/api/stats/chain    # 获取链统计信息
GET /explorer/api/stats/network # 获取网络统计信息
```

### 搜索相关
```
GET /explorer/api/search?q={query} # 搜索功能
```

## 页面路由

### 主要页面
- `/explorer/` - 首页（重定向到仪表板）
- `/explorer/dashboard` - 仪表板
- `/explorer/blocks` - 区块列表
- `/explorer/block/{hash}` - 区块详情
- `/explorer/transactions` - 交易列表
- `/explorer/transaction/{hash}` - 交易详情
- `/explorer/account/{address}` - 账户详情
- `/explorer/search` - 搜索页面

## 数据结构

### BlockDetailDto
```json
{
  "height": 1000,
  "hash": "0x...",
  "previous_hash": "0x...",
  "timestamp": 1640995200000,
  "round": 100,
  "proposer": "0x...",
  "vrf_output": "0x...",
  "vrf_proof": "0x...",
  "merkle_root": "0x...",
  "nonce": 12345,
  "difficulty_target": "1000000",
  "transaction_count": 10,
  "all_vrf_announcements": [...],
  "transactions": [...]
}
```

### TransactionDetailDto
```json
{
  "hash": "0x...",
  "from": "0x...",
  "to": "0x...",
  "amount": 1000000,
  "fee": 1000,
  "nonce": 1,
  "timestamp": 1640995200000,
  "signature": "0x...",
  "data": null
}
```

### AccountDetailDto
```json
{
  "address": "0x...",
  "public_key": "0x...",
  "balance": 1000000,
  "stake_amount": 500000,
  "nonce": 1,
  "last_updated": 1640995200000,
  "is_super_node": true
}
```

## 部署说明

### 1. 依赖要求
- Java 21+
- Spring Boot 3.x
- Maven 3.8+

### 2. 配置
```yaml
# application.yml
server:
  port: 8080

spring:
  thymeleaf:
    prefix: classpath:/templates/
    suffix: .html
    cache: false

equiflux:
  explorer:
    refresh-interval: 10000  # 刷新间隔（毫秒）
    page-size: 20            # 默认分页大小
```

### 3. 启动
```bash
mvn spring-boot:run
```

### 4. 访问
打开浏览器访问: http://localhost:8080/explorer/

## 开发指南

### 添加新功能
1. 在`BlockExplorerService`中添加业务逻辑
2. 在`BlockExplorerController`中添加API接口
3. 创建对应的DTO类
4. 更新前端页面模板

### 自定义样式
- 修改CSS样式文件
- 使用Bootstrap类名
- 保持响应式设计

### 性能优化
- 使用分页加载大数据量
- 实现数据缓存
- 优化数据库查询

## 测试

### 运行测试
```bash
mvn test
```

### 测试覆盖
- 单元测试: 服务层逻辑
- 集成测试: API接口
- 前端测试: JavaScript功能

## 贡献指南

1. Fork项目
2. 创建功能分支
3. 提交更改
4. 创建Pull Request

## 许可证

本项目采用MIT许可证。详见LICENSE文件。

## 联系方式

- 项目主页: https://github.com/equiflux/node
- 问题反馈: https://github.com/equiflux/node/issues
- 邮箱: master@equiflux.io

---

**Equiflux Block Explorer** - 探索Equiflux区块链的窗口
