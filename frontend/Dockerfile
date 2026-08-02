FROM node:22-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Baked into the production bundle at build time. Defaults to a relative path
# so the app calls the API on its own origin; nginx.conf proxies /api to the
# backend service, so this normally doesn't need to change in docker-compose.
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

RUN npm run build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
# Именно 127.0.0.1, а не localhost: nginx слушает только IPv4 (0.0.0.0:80),
# а localhost внутри контейнера резолвится в ::1 — проверка получала отказ
# при полностью работоспособном сервере.
HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=5 \
  CMD wget -q -O /dev/null http://127.0.0.1:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
