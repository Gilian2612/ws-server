
# ws-server — Scaffolding

## Estructura del proyecto

```
ws-server/
├── pom.xml                          # Configuración Maven + dependencias
├── src/
│   ├── main/
│   │   ├── java/com/chat/ws_server/
│   │   │   ├── WsServerApplication.java   # Spring Boot
│   │   │   ├── WebSocketConfig.java       # Registro del handler + CORS
│   │   │   ├── ChatHandler.java           # Handler WebSocket (métricas, trazas, logs)
│   │   │   └── ChatMessage.java           # DTO 
│   │   └── resources/
│   │       ├── application.properties     # Puerto, Actuator, Zipkin, sampling
│   │       └── logback-spring.xml         # Appenders: consola + Logstash (TCP)
│   └── test/
│       └── java/com/chat/ws_server/
│           └── WsServerApplicationTests.java
├── .gitignore
├── .gitattributes
├── mvnw / mvnw.cmd                  # Maven Wrapper
└── HELP.md
```

## Stack

- **Java 21** (Eclipse Adoptium)
- **Spring Boot 3.5.3**
- **spring-boot-starter-websocket** — soporte WebSocket nativo
- **spring-boot-starter-actuator** — endpoints de monitoreo
- **micrometer-registry-prometheus** — métricas para Prometheus/Grafana
- **micrometer-tracing-bridge-brave + zipkin-reporter-brave** — trazas distribuidas para Zipkin
- **logstash-logback-encoder 7.4** — logs estructurados JSON para ELK
- **jackson-databind** — serialización/deserialización JSON

## Flujo de datos

1. Cliente React se conecta vía WebSocket a `ws://localhost:8080/chat`
2. `WebSocketConfig` registra `ChatHandler` en la ruta `/chat` con CORS para `localhost:5173`
3. `ChatHandler` gestiona conexiones, mensajes y desconexiones:
   - Incrementa contadores Micrometer (received/broadcast)
   - Crea spans con Tracer para Zipkin
   - Emite logs estructurados con StructuredArguments para Logstash
4. Mensajes se broadcast a todas las sesiones activas

## Observabilidad

| Herramienta | Puerto | Qué monitorea |
|-------------|--------|----------------|
| Prometheus  | 9090   | Métricas (ws.messages.received, ws.messages.broadcast, ws.sessions.active) |
| Grafana     | 3000   | Dashboards visuales de las métricas |
| Zipkin      | 9411   | Trazas distribuidas (duración, tags por mensaje) |
| Kibana      | 5601   | Logs estructurados vía Elasticsearch |

## Cómo ejecutar

```bash
# Servidor
cd ws-server
mvn spring-boot:run

# Infraestructura de observabilidad
cd ..
docker compose up -d
```
