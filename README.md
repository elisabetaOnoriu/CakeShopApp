# 🍰 CakeShop — Spring Boot + JWT + Postgres + Docker

A complete demo shop: REST API secured with JWT, a small Thymeleaf UI for **Home**, **Cake details**, and **Cart**, plus **PostgreSQL** and **Docker Compose** for easy setup.

---

## ✨ Features

* 👤 **JWT authentication** (login/register), roles **USER** / **ADMIN**
* 🧁 **Cakes**: list, search, filter by category, CRUD (write = ADMIN only)
* 🧩 **Categories**: list, CRUD (write = ADMIN only)
* ⭐ **Reviews**: list + add review (logged-in users)
* 🛒 **Cart**: add to cart, list cart items, compute total (logged-in users)
* 🖥️ **UI Pages**:

  * `/home` — catalog grid (+ “Add Cake” if ADMIN)
  * `/cakes/{id}` — cake details + reviews + add review
  * `/cart` — user’s cart with **Place order** button and smooth animations
* 🐳 **Docker Compose** for Postgres + app

---

## 🧱 Tech Stack

* **Java 17**, **Spring Boot**
* Spring Security (JWT via `jjwt`)
* Spring Data JPA + **PostgreSQL**
* Thymeleaf (pages: **home**, **cake details**, **cart**)
* **Docker** & **Docker Compose**

---

## 🚀 Quick Start

### 1) Clone & `.env`

```bash
git clone <repo-url> cakeshop
cd cakeshop
```

Create a `.env` in the project root:

```env
# PostgreSQL
DB_NAME=cakeshop
DB_USER=cakeshop
DB_PASSWORD=secret123

# JWT (pick ONE approach)
# A) Base64 secret (recommended for prod)
JWT_SECRET_BASE64=ZHVteS1iYXNlNjQtc2VjcmV0LWF0LWxlYXN0LTMyLWNoYXJzLWxvbmc=
JWT_EXP_MINUTES=600

# OR B) Raw secret (dev): at least 32 characters
# JWT_SECRET=change-me-please-this-is-at-least-32-chars-long
# JWT_TTL_MS=36000000
```

> The app supports **either** `JWT_SECRET_BASE64` **or** `JWT_SECRET`. Keep only one active.

### 2) Run with Docker Compose

```bash
docker compose up --build
```

* App → [http://localhost:8080](http://localhost:8080)
* DB → runs in the `db` container (port 5432)

If you need sample data, see **Seed SQL** below.

### 3) Run locally (without Docker)

Requires **JDK 17**.

```bash
# Start Postgres locally or in a separate container, then:
./mvnw -DskipTests spring-boot:run
# or
./mvnw -q -DskipTests package && java -jar target/*.jar
```

Configure DB connection via `application.yml` or environment variables.

---

## 🔐 Authentication & JWT

### Login

```bash
curl -s -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ileana","password":"garofita"}'
```

The response contains a `token`. Use it on protected routes:

```
Authorization: Bearer <JWT>
```

> Logging out invalidates the token (blacklist). Log in again afterward.

---

## 🧭 Public UI Routes

* `GET /home` — product catalog
* `GET /cakes/{id}` — cake detail with reviews
* `GET /cart` — your cart (requires JWT for API calls under the hood)

Static assets and these pages are public; write APIs require **ADMIN**.

---

## 🧪 API Summary

### Cakes

* `GET /api/cakes` — list (supports `?q=...` and `?categoryId=...`)
* `GET /api/cakes/{id}`
* `POST /api/cakes` *(ADMIN)*
* `PUT /api/cakes/{id}` *(ADMIN)*
* `DELETE /api/cakes/{id}` *(ADMIN)*

### Categories

* `GET /api/categories` / `GET /api/categories/{id}`
* `POST /api/categories` *(ADMIN)*
* `PUT /api/categories/{id}` *(ADMIN)*
* `DELETE /api/categories/{id}` *(ADMIN)*

### Reviews

* `GET /api/cakes/{id}/reviews`
* `POST /api/cakes/{id}/reviews` *(logged-in)*

  * body: `{ "rating": 1..5, "comment": "..." }`

### Cart (logged-in)

* `POST /api/cart/{cakeId}` — add cake to cart
* `GET  /api/cart/cakes` — list cart items
* `GET  /api/cart/total` — cart total

---

## 🧰 Seed SQL (optional)

Populate a few categories/chefs/cakes (adjust table/column names if needed):

```sql
-- Categories
INSERT INTO categories (id, name) VALUES
  (1,'Cakes'), (2,'Cupcakes'), (3,'Cookies'), (4,'Pastries')
ON CONFLICT DO NOTHING;

-- Pastry Chefs (if you use a dedicated table)
INSERT INTO pastry_chefs (id, name) VALUES
  (1,'Mary Berry'), (2,'Paul Hollywood'), (3,'Claudia Sanderson')
ON CONFLICT DO NOTHING;

-- Cakes
INSERT INTO cakes (name, price, stock, weight, category_id, pastry_chef_id, description) VALUES
('Chocolate Dream', 24.50, 12, 1.20, 1, 1, 'Rich cocoa sponge, silky ganache.'),
('Vanilla Sky',     21.00, 8,  1.00, 1, 2, 'Creamy vanilla layers, light and fluffy.'),
('Croissant',        3.50, 50, 0.09, 4, 3, 'Buttery, flaky, freshly baked.');
```

Create a test admin if needed (use a real bcrypt hash for the password):

```sql
INSERT INTO users (username, password, role)
VALUES ('admin','{bcrypt_hash}','ADMIN')
ON CONFLICT DO NOTHING;
```

---

## 🧩 Pages & Frontend Logic

* **Home (`templates/home.html`)**

  * Fetches `GET /api/cakes` (with filters)
  * Clicking a card → `/cakes/{id}`
  * ADMIN-only “Add Cake” modal → `POST /api/cakes`

* **Cake (`templates/cake.html`)**

  * Fetches `GET /api/cakes/{id}`
  * Reviews: `GET /api/cakes/{id}/reviews`
  * Submit review: `POST /api/cakes/{id}/reviews` (JWT)
  * “Add to cart”: `POST /api/cart/{id}`

* **Cart (`templates/cart.html`)**

  * Fetches `GET /api/cart/cakes` and `GET /api/cart/total` (JWT)
  * “Place order” button (stubbed on UI; you can wire a real endpoint later)

The UI stores the JWT in `localStorage` (`token` / `authToken`) and attaches it to requests when needed.

---

## ⚙️ Configuration Notes

* **Port**: app listens on `8080` (mapped to `localhost:8080` in Docker).
* **DB**: in Docker, the app connects to host `db:5432`; locally, usually `localhost:5432`.
* **CORS**: open for dev; tighten for production.
* **JWT**: pick either `JWT_SECRET_BASE64` + `JWT_EXP_MINUTES` **or** `JWT_SECRET` + `JWT_TTL_MS`.

---

## ✅ Sanity Checks

* **Login**

  ```bash
  curl -s -X POST http://localhost:8080/api/users/login \
    -H "Content-Type: application/json" \
    -d '{"username":"ileana","password":"garofita"}' | jq
  ```

* **List cakes (public)**

  ```bash
  curl -s http://localhost:8080/api/cakes | jq
  ```

* **Add cake (ADMIN)**

  ```bash
  curl -s -X POST http://localhost:8080/api/cakes \
    -H "Authorization: Bearer <ADMIN_JWT>" \
    -H "Content-Type: application/json" \
    -d '{"name":"Almond Croissant","price":4.2,"stock":30,"weight":0.1,"categoryId":4,"pastryChefName":"Claudia Sanderson","description":"Buttery & flaky"}' | jq
  ```

* **Cart**

  ```bash
  curl -s -X POST http://localhost:8080/api/cart/1 -H "Authorization: Bearer <JWT>"
  curl -s http://localhost:8080/api/cart/cakes -H "Authorization: Bearer <JWT>" | jq
  curl -s http://localhost:8080/api/cart/total -H "Authorization: Bearer <JWT>"
  ```

---

## 🐞 Troubleshooting

* **401 on protected API** → ensure `Authorization: Bearer <token>` and that the token isn’t expired/blacklisted.
* **Cake detail not rendering** → navigate to `/cakes/{id}` and ensure `PageController` maps to `cake.html`.
* **Reviews don’t appear/persist** → `POST /api/cakes/{id}/reviews` requires a valid JWT; the page re-renders or re-fetches after posting.
* **WSL JAVA\_HOME** → install `openjdk-17-jdk` and set `JAVA_HOME` to the Linux JDK path (not Windows).


