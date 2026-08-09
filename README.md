# Java Service Boilerplate

Contoh service sederhana berbasis Java 21 dan Spring Boot 4.1.0. Domain contoh adalah `Task`:
task dapat dibuat melalui REST atau Kafka, disimpan di PostgreSQL, dibaca dengan pola Redis
cache-aside, diselesaikan melalui REST, dan dibersihkan oleh scheduler.

JVM, JDBC session, persisted timestamps, logs, dan API timestamps menggunakan UTC secara default
melalui `APP_TIMEZONE=UTC`.

## Teknologi

- Java 21 dan virtual threads
- Spring MVC dan Jakarta Validation
- Spring JDBC, PostgreSQL, dan Flyway
- Spring Data Redis
- Spring Kafka dengan retry dan dead-letter topic
- `sdk-util` untuk response envelope, global exception, trace ID, ECS logging, dan AOP logging
- Actuator, Prometheus, JUnit 5, Mockito, dan JaCoCo

## Alur contoh

```text
REST POST /api/v1/tasks        Kafka task.create
             |                       |
             +--- validate/map ------+
                         |
                         v
                  TaskServiceImpl
                         |
                 PostgreSQL (source of truth)
                         |
                 Redis cache-aside (TTL 10m)

GET task: Redis hit -> response
          Redis miss/failure -> PostgreSQL -> populate Redis -> response
```

Kegagalan Redis tidak membuat endpoint baca gagal selama PostgreSQL masih tersedia. Operasi cache
bersifat best-effort dan kegagalannya dicatat sebagai structured log.

## Contoh setiap package

| Package | Contoh | Tanggung jawab |
| --- | --- | --- |
| `config` | `ApplicationConfig`, `RedisConfig`, `KafkaErrorHandlingConfig` | Bean Clock, virtual-thread executor, Redis JSON cache, Kafka retry/DLT |
| `config.properties` | `TaskProperties` | TTL cache dan retention scheduler yang type-safe |
| `controller` | `TaskController` | REST API dan response dari `sdk-util` |
| `entities.constant` | `TaskStatus`, `TaskLogFields` | Enum domain dan nama structured field |
| `entities.dto` | `CreateTaskRequest`, `CreateTaskEvent`, `TaskResponse` | Kontrak REST/Kafka |
| `entities.mapper` | `TaskMapper` | Normalisasi request dan mapping response |
| `entities.model` | `Task` | Model internal/persistence |
| `job` | `TaskCleanupJob` | Boundary scheduler dengan trace ID dan error handler |
| `repository` | `TaskRepository`, `TaskCacheRepository` | Abstraksi PostgreSQL dan Redis |
| `repository.impl` | `JdbcTaskRepository`, `RedisTaskCacheRepository` | SQL parameterized dan cache-aside operations |
| `service` | `TaskService`, `TaskMaintenanceService` | Kontrak use case |
| `service.impl` | `TaskServiceImpl`, `TaskMaintenanceServiceImpl` | Orkestrasi bisnis dan transaksi |
| `subscriber` | `TaskEventSubscriber` | Kafka listener dan validasi event eksplisit |
| `utils.exception` | `InvalidTaskStateException` | Exception bisnis untuk input/state invalid |
| `utils.handler` | `AsyncExceptionHandler` | Logging error Kafka/scheduler/virtual thread |

Folder resource juga menyediakan Flyway migration, template HTML, konfigurasi profile, dan contoh
payload di `src/main/resources/json/index.json`.

## Menyiapkan dependency

Project memakai sibling library `sdk_util`:

```bash
cd ../../github/sdk_util
mvn clean install
cd ../../boilerplate/java_boilerplate
```

Siapkan PostgreSQL, Redis, dan Kafka, lalu sesuaikan environment berdasarkan `.env.example`.
Database awal:

```sql
CREATE DATABASE java_boilerplate;
```

Nilai lokal default:

| Komponen | Alamat |
| --- | --- |
| HTTP | `localhost:9010` |
| PostgreSQL | `localhost:5432/java_boilerplate` |
| Redis | `localhost:6379` |
| Kafka | `localhost:9092` |
| Kafka create topic | `task.create` |
| Kafka DLT | `task.create.dlt` |

## Menjalankan

```bash
mvn spring-boot:run
```

Profile `local` menonaktifkan security agar contoh mudah dicoba. Aktifkan dan konfigurasi security
`sdk-util` sebelum digunakan di environment nyata. Default issuer adalah `usermanagement` pada
`http://localhost:9005`; SDK mengambil public RSA key melalui discovery/JWKS dan memakai claim
`username`, `roles`, serta `permissions`.

## REST API

Membuat task:

```bash
curl -i -X POST http://localhost:9010/api/v1/tasks \
  -H 'Content-Type: application/json' \
  -H 'X-Correlation-Id: task-demo-001' \
  -d '{"title":"Learn Redis","description":"Try cache-aside"}'
```

Membaca task (Redis lalu PostgreSQL):

```bash
curl http://localhost:9010/api/v1/tasks/{taskId}
```

Menyelesaikan task:

```bash
curl -X PATCH http://localhost:9010/api/v1/tasks/{taskId}/complete
```

Kontrak lengkap REST dan Kafka tersedia di
[`src/main/resources/json/index.json`](src/main/resources/json/index.json).

## Perilaku Redis

- Key cache memakai UUID task pada cache `tasks`.
- TTL default `10m`, dikonfigurasi melalui `TASK_CACHE_TTL`.
- Create menulis PostgreSQL dahulu lalu mengisi cache.
- Get memakai cache-aside.
- Complete mengubah PostgreSQL, menghapus key lama, lalu memuat nilai baru.
- Scheduler membersihkan seluruh cache hanya bila ada task kedaluwarsa yang terhapus.
- Error Redis tidak menutupi error database dan tidak membocorkan payload pada log.

## Build dan coverage

```bash
mvn clean verify
```

JaCoCo menggagalkan build bila line coverage production business code kurang dari 90%. Laporan
HTML tersedia di `target/site/jacoco/index.html`.

## Docker

Image hanya memuat JRE Java 21 dan JAR aplikasi. Build JAR lebih dahulu agar dependency sibling
`sdk-util` tetap diselesaikan oleh Maven lokal atau CI:

```bash
mvn clean package
docker build -t java-boilerplate:1.0.0 .
docker run --rm --env-file .env -p 9010:9010 java-boilerplate:1.0.0
```

Isi `.env` dari `.env.example`, lalu ubah host dependency `localhost` menjadi nama service pada
Docker network. Jangan masukkan credential ke image atau repository.

## Catatan produksi

- Gunakan credential melalui secret manager, bukan YAML atau repository.
- Gunakan Redis dengan authentication, TLS, replication, dan eviction policy yang sesuai beban.
- Tambahkan idempotency persistence untuk Kafka bila kontrak event dipakai pada sistem nyata.
- Tambahkan Testcontainers untuk contract PostgreSQL/Redis/Kafka pada CI yang memiliki Docker.
- Template ini sengaja sederhana; salin hanya package yang relevan untuk service baru.
