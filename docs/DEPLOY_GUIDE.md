# Hướng dẫn triển khai Production (Docker trên VPS)

Hệ thống Quản lý Thư viện: backend Spring Boot (Maven, Flyway), frontend Next.js,
PostgreSQL, xác thực Clerk, lưu ảnh bìa trên Cloudflare R2.

> **Trạng thái: đã ngừng triển khai.** Bản deploy production trên VPS và toàn bộ
> pipeline CI/CD đã được tháo bỏ. Tài liệu này giữ lại làm tham chiếu kiến trúc và
> quy trình deploy thủ công nếu cần dựng lại từ đầu.

## Kiến trúc triển khai

```
Cloudflare (HTTPS, SSL Flexible)
        │  :443 -> :80
        ▼
VPS: public-nginx (jwilder/nginx-proxy, network_mode host, cổng 80)
        │  route theo VIRTUAL_HOST
        ├── drx.io.vn        -> frontend  (container :3000)
        └── api.drx.io.vn    -> backend   (container :8080)
                                      │
                                      ▼
                                 postgres (:5432, nội bộ Docker network)
```

- Tên miền: Frontend `https://drx.io.vn`, Backend API `https://api.drx.io.vn`.
- SSL: Cloudflare **Flexible** (không cấu hình TLS ở tầng ứng dụng/Docker).
- Registry: Docker Hub, user `lephianhdev386ht`.
  - Backend: `lephianhdev386ht/library-management-system-backend:latest`
  - Frontend: `lephianhdev386ht/library-management-system-frontend:latest`
- **Migration**: dùng **Flyway**. Các file `backend/src/main/resources/db/migration/V*.sql`
  được đóng vào jar và **tự động chạy khi backend khởi động**. Không có bước migrate thủ công.
- **Redis**: không dùng (backend không phụ thuộc Redis) nên không có trong stack production.

## Yêu cầu trước khi bắt đầu

- VPS đã chạy sẵn reverse proxy `public-nginx` (jwilder/nginx-proxy 1.6, `network_mode: host`, cổng 80).
- Docker + Docker Compose v2 trên VPS.
- Tài khoản Docker Hub (`docker login` trên máy build), tài khoản Cloudflare quản lý `drx.io.vn`.
- Đã điền các giá trị **TODO** trong `backend/.env.production` và `frontend/.env.production`
  (xem mục "Việc cần làm trước khi go-live").

---

## Phần 1 — Deploy lần đầu (thủ công)

### 1.1. DNS & SSL trên Cloudflare

| Type | Name  | Content       | Proxy      |
| ---- | ----- | ------------- | ---------- |
| A    | `@`   | `<VPS_IP>`    | Proxied ✅ |
| A    | `api` | `<VPS_IP>`    | Proxied ✅ |

- SSL/TLS: Encryption Mode = **Flexible**; Always Use HTTPS = **ON**; Automatic HTTPS Rewrites = **ON**.

### 1.2. Build & push images (chạy trên máy local)

Không cần tạo migration thủ công: các file Flyway `V1..V13` đã có sẵn trong repo và
được đóng vào image.

```bash
docker login

# --platform linux/amd64: máy local có thể khác kiến trúc với VPS.
cd backend
docker build --pull --platform linux/amd64 -t lephianhdev386ht/library-management-system-backend:latest .
docker push lephianhdev386ht/library-management-system-backend:latest
cd ../frontend
docker build --pull --platform linux/amd64 -t lephianhdev386ht/library-management-system-frontend:latest .
docker push lephianhdev386ht/library-management-system-frontend:latest
cd ..
```

### 1.3. Khởi động trên VPS

```bash
ssh <user>@<VPS_IP>
cd /opt/projects

# Lần đầu:
git clone git@github.com:LePhiAnhDev/library-management-system.git
cd library-management-system

docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d
```

Backend khi khởi động sẽ tự chạy Flyway để tạo schema + seed dữ liệu master trước khi mở cổng.

### 1.4. Kiểm tra

```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f backend
```

Trong log backend, tìm dòng Flyway kiểu `Successfully applied N migrations` và
`Started LibraryApplication`. Sau đó mở `https://drx.io.vn` và
`https://api.drx.io.vn/actuator/health` (trả `{"status":"UP"}`).

---

## Phần 2 — Cập nhật KHÔNG đổi schema (chỉ đổi code FE/BE)

**Máy local:**

```bash
git add . && git commit -m "feat: ..." && git push origin main

# build & push lại image (xem lệnh đầy đủ ở mục 1.2); frontend chỉ khi FE có đổi
```

**VPS:**

```bash
cd /opt/projects/library-management-system
git pull origin main
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d --force-recreate --remove-orphans
docker compose -f docker-compose.prod.yml ps
```

---

## Phần 3 — Cập nhật CÓ đổi schema (thêm migration Flyway)

Với thay đổi cấu trúc DB (thêm/sửa/xóa bảng, cột...), tạo một file migration Flyway MỚI
(không sửa file cũ đã chạy):

**Máy local:**

```bash
# Tạo file migration kế tiếp, đặt tên tăng dần theo phiên bản:
#   backend/src/main/resources/db/migration/V14__mo_ta_thay_doi.sql
# Viết câu lệnh DDL/DML vào file đó, rồi:

git add . && git commit -m "feat(db): V14 mo_ta_thay_doi" && git push origin main

# build & push lại image backend (xem lệnh đầy đủ ở mục 1.2)
```

**VPS:**

```bash
cd /opt/projects/library-management-system
git pull origin main
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d --force-recreate --remove-orphans
docker compose -f docker-compose.prod.yml logs -f backend
```

> Flyway chạy tự động trong lúc backend khởi động và áp mọi migration mới trước khi
> ứng dụng sẵn sàng. Theo dõi log để thấy `Successfully applied 1 migration` và
> `Started LibraryApplication`. Vì Hibernate ở chế độ `ddl-auto: validate`, nếu migration
> thiếu so với entity thì backend sẽ báo lỗi và không khởi động (an toàn, tránh chạy với schema sai).

---

## Trạng thái go-live

- [x] **Clerk production**: đã bật Clerk production instance cho `drx.io.vn`
      (Frontend API `clerk.drx.io.vn`), đã thay `pk_live_.../sk_live_...` và
      `CLERK_ISSUER_URI`/`CLERK_JWKS_URI` trong cả hai `.env.production`.
- [x] **DNS + SSL**: `@`, `api`, `clerk` trỏ về VPS qua Cloudflare (Proxied, SSL Flexible),
      HTTPS công khai đã xác minh.
- [ ] **Mật khẩu DB** (khuyến nghị): đặt `POSTGRES_PASSWORD` mạnh hơn trong `backend/.env.production`
      (đổi mật khẩu đồng nghĩa phải reset volume postgres của stack, xem ghi chú bên dưới).
- [ ] (Tuỳ chọn) Tạo bucket R2 riêng cho production thay vì dùng chung với dev.

## Ghi chú bảo mật

Các file `.env.production` được commit vào repo một cách có chủ đích để đơn giản hóa pipeline,
với giả định repo là private.

> ⚠️ **Repo hiện đang PUBLIC.** Điều kiện của giả định trên không còn đúng, nên các giá trị sau
> đang bị lộ công khai (và vẫn còn trong git history dù có xóa file):
> `CLERK_SECRET_KEY` (`sk_live_...`), `R2_ACCESS_KEY_ID`, `R2_SECRET_ACCESS_KEY`,
> `POSTGRES_PASSWORD`. Cần **rotate toàn bộ** các secret này.
