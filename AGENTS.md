# AGENTS.md

## Project Overview

`java_boilerplate` adalah referensi service Java 21 sederhana dengan REST, PostgreSQL, Redis,
Kafka, scheduler, observability, dan shared `sdk-util`. Domain contoh adalah `Task`.

## Structure

Production package root harus tetap `com.mac.boilerplate`. Gunakan pemisahan berikut:

- `config`: Spring bean dan integration configuration.
- `config.properties`: type-safe configuration di bawah `task.*`.
- `controller`: validasi dan HTTP mapping saja.
- `entities.dto`: kontrak eksternal; `entities.model`: model internal.
- `entities.mapper`: normalisasi dan mapping.
- `repository.impl`: SQL dan operasi Redis.
- `service.impl`: orkestrasi dan transaksi bisnis.
- `job` dan `subscriber`: inbound asynchronous boundaries.
- `utils.exception` dan `utils.handler`: error domain dan boundary handler.

## Coding Rules

- Gunakan Java 21, records untuk immutable data, constructor injection, `Instant`, dan injected
  `Clock`.
- Gunakan UTC untuk timezone JVM, koneksi database, log, dan timestamp API. Konversi ke timezone
  regional hanya pada presentation atau business-scheduling boundary yang eksplisit.
- Controller tidak boleh berisi SQL, Redis, Kafka, atau keputusan bisnis.
- Gunakan parameterized SQL dan Flyway untuk setiap perubahan schema.
- PostgreSQL adalah source of truth. Redis hanya cache best-effort; cache outage tidak boleh
  mengubah hasil bisnis bila database tersedia.
- Jangan memakai Redis `KEYS` pada production flow. Gunakan TTL, known-key eviction, atau cache
  abstraction yang memakai mekanisme clear aman dari provider.
- Validasi REST menggunakan Jakarta Validation. Kafka event wajib divalidasi eksplisit.
- Gunakan global HTTP exception handler dan response helper dari `sdk-util`.
- Pesan untuk client harus dalam bahasa Inggris dan tidak boleh mengekspos stack trace/secret.
- Gunakan `StructuredLog`, ECS fields, dan trace ID. Jangan log password, token, atau payload
  sensitif. Propagasi MDC secara eksplisit pada Kafka, scheduler, dan virtual thread.
- Tangani exception Kafka dengan retry/DLT dan exception scheduler/async pada boundary-nya.
- Susun setiap file application YAML per kelompok property utama dan beri banner komentar tiga
  baris (`# =========================`, nama section uppercase, lalu separator yang sama), mengikuti
  pola `DATABASE`, `KAFKA`, `SERVER`, `SHARED SDK`, dan `MANAGEMENT / ACTUATOR`. Pisahkan section
  dengan satu baris kosong dan jangan mengubah hierarchy property hanya demi formatting.

## Testing

- Setiap perubahan behavior wajib memiliki focused tests.
- JaCoCo line coverage untuk controller, mapper, job, repository implementation, service
  implementation, subscriber, dan utils minimal 90%.
- Test success, validation, not-found/state conflict, cache hit/miss/outage, dan asynchronous
  failure sesuai perubahan.
- Jangan menurunkan coverage gate atau menambah assertion tanpa makna.

Jalankan verifikasi akhir dari root project:

```bash
mvn clean verify
```
