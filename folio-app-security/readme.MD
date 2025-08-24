# 🚦 Spring Boot Rate Limiter Library

A lightweight **rate limiting library** for Spring Boot applications.  
Supports multiple algorithms, pluggable storage backends, and both **YAML configuration** and **annotations**.

---

## ✨ Features
- ✅ Algorithms: **Fixed Window**, **Token Bucket**, **GCRA**
- ✅ Storage: **In-Memory** (local JVM) and **Redis** (distributed)
- ✅ Configurable via `application.yaml`
- ✅ Fine-grained rules using `@RateLimit` annotation
- ✅ Spring Boot **Auto-Configuration** (works out-of-the-box when dependency is added)
- ✅ Extensible design for new algorithms and storage engines

---

## ⚙️ Installation

Add the library to your Spring Boot project (once published, e.g. to Maven Central or private repo):

```xml
<dependency>
  <groupId>com.renansouza</groupId>
  <artifactId>folio-app-backends</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

---

## 📖 Configuration

`application.yaml` Example

```yaml
rate-limiter:
  enabled: true
  key: ip
  algorithm: fixed-window
  storage: in-memory
  redis:
    host: localhost
    port: 6379
  rules:
    - path: /api/orders
      method: GET
      limit: 10
      duration: 60
    - path: /api/payments
      method: POST
      limit: 5
      duration: 120
```

| Property           | Type      | Description                                                                          |
| ------------------ | --------- |--------------------------------------------------------------------------------------|
| `enabled`          | boolean   | Enables or disables rate limiting globally.                                          |
| `key`              | string    | Key used for identifying requests (`ip`, `user`, or custom).                         |
| `algorithm`        | enum      | Default algorithm: `FIXED_WINDOW`, `TOKEN_BUCKET`, `SLIDING_WINDOW`, `LEAKY_BUCKET`. |
| `storage`          | enum      | Storage backend: `IN_MEMORY`, `REDIS`.                                               |
| `rules[].path`     | string    | Request path to apply rule.                                                          |
| `rules[].method`   | string    | HTTP method (e.g., GET, POST).                                                       |
| `rules[].limit`    | int       | Max number of allowed requests.                                                      |
| `rules[].duration` | int (sec) | Time window for the limit.                                                           |

---
📝 Annotation Support

You can also apply rate limits directly at the controller or method level:

```java
@RestController
public class OrderController {

    @GetMapping("/api/orders")
    @RateLimit(limit = 5, duration = 60, algorithm = AlgorithmType.TOKEN_BUCKET)
    public List<Order> getOrders() {
        return List.of();
    }
}
```

| Parameter   | Type      | Default       | Description              |
| ----------- | --------- | ------------- | ------------------------ |
| `limit`     | int       | —             | Max requests allowed.    |
| `duration`  | int (sec) | —             | Time window.             |
| `algorithm` | enum      | FIXED\_WINDOW | Rate limiting algorithm. |
| `storage`   | enum      | IN\_MEMORY    | Storage backend.         |
