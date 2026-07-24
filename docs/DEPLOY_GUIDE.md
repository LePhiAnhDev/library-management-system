# Hướng dẫn triển khai Production (Docker trên VPS)

Hệ thống Quản lý Thư viện: backend Spring Boot (Maven, Flyway), frontend Next.js,
PostgreSQL, xác thực Clerk, lưu ảnh bìa trên Cloudflare R2.

## Kiến trúc triển khai

```
Cloudflare (HTTPS, SSL Flexible)
        │  :443 -> :80
        ▼
VPS: public-nginx (jwilder/nginx-proxy, network_mode host, cổng 80)
        │  route theo VIRTUAL_HOST
        ├── shortloop.co        -> frontend  (container :3000)
        └── api.shortloop.co    -> backend   (container :8080)
                                      │
                                      ▼
                                 postgres (:5432, nội bộ Docker network)
```

- Tên miền: Frontend `https://shortloop.co`, Backend API `https://api.shortloop.co`.
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
- Tài khoản Docker Hub (`docker login` trên máy build), tài khoản Cloudflare quản lý `shortloop.co`.
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

cd backend  && ./build-push-be.sh && cd ..
cd frontend && ./build-push-fe.sh && cd ..
```

> Script build cho `linux/amd64` và push cả tag `latest` (và tag tuỳ chọn nếu truyền `./build-push-be.sh v1.0.0`).

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
`Started LibraryApplication`. Sau đó mở `https://shortloop.co` và
`https://api.shortloop.co/actuator/health` (trả `{"status":"UP"}`).

---

## Phần 2 — Cập nhật KHÔNG đổi schema (chỉ đổi code FE/BE)

**Máy local:**

```bash
git add . && git commit -m "feat: ..." && git push origin main

cd backend  && ./build-push-be.sh && cd ..
cd frontend && ./build-push-fe.sh && cd ..   # chỉ khi FE có đổi
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
cd backend && ./build-push-be.sh && cd ..
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

## Phần 4 — CI/CD tự động (khuyến nghị)

Pipeline `.github/workflows/ci.yml` (tên: `CI/CD`) chạy khi push/PR vào `main`:

1. **CI** (mọi push/PR): test backend (Maven + Testcontainers), lint/typecheck/build frontend, quét Trivy.
2. **CD** (chỉ khi push vào `main` hoặc chạy tay `workflow_dispatch`, sau khi CI xanh):
   - `build-push`: build và push image backend + frontend lên Docker Hub (tag `latest` và theo commit SHA).
   - `deploy`: SSH vào VPS, `git pull` -> `docker compose pull` -> `up -d` -> dọn image cũ.

Như vậy chỉ cần `git push origin main` là hệ thống tự build, push và deploy (E2E).

### Secrets cần thêm trong GitHub (Settings -> Secrets and variables -> Actions)

| Secret              | Mô tả                                                        |
| ------------------- | ------------------------------------------------------------ |
| `DOCKERHUB_USERNAME`| `lephianhdev386ht`                                           |
| `DOCKERHUB_TOKEN`   | Access token Docker Hub (Account Settings -> Security)       |
| `VPS_HOST`          | IP hoặc hostname VPS                                          |
| `VPS_USER`          | user SSH (ví dụ `root` hoặc `deploy`)                        |
| `VPS_SSH_KEY`       | private key SSH có quyền vào VPS                              |
| `VPS_PORT`          | (tuỳ chọn) cổng SSH, mặc định `22`                           |

> VPS cần đã `git clone` sẵn repo ở `/opt/projects/library-management-system` và có quyền
> `git pull` (deploy key hoặc HTTPS token). Muốn deploy ít tự động hơn: sửa điều kiện `if:`
> của job `deploy` để chỉ chạy theo tag hoặc chỉ khi bấm `workflow_dispatch`.

---

## Việc cần làm trước khi go-live (TODO)

- [ ] **Clerk production**: tạo Clerk production instance cho `shortloop.co`, cấu hình DNS theo
      hướng dẫn Clerk, rồi thay `pk_live_.../sk_live_...` và `CLERK_ISSUER_URI`/`CLERK_JWKS_URI`
      trong `backend/.env.production` và `frontend/.env.production` (hiện đang là khóa test).
- [ ] **Mật khẩu DB**: đặt `POSTGRES_PASSWORD` mạnh trong `backend/.env.production`.
- [ ] **DNS**: trỏ `@` và `api` về IP VPS, bật Proxied + SSL Flexible.
- [ ] **GitHub secrets**: thêm đủ các secret ở bảng trên để CD hoạt động.
- [ ] (Tuỳ chọn) Tạo bucket R2 riêng cho production thay vì dùng chung với dev.

## Ghi chú bảo mật

Các file `.env.production` được commit vào repo private này một cách có chủ đích để đơn giản
hóa pipeline (đã chấp nhận rủi ro). Nếu repo chuyển sang public, phải rotate toàn bộ secret trước.
