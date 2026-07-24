# syntax=docker/dockerfile:1

# ============================================================================
# Builder: install deps and build the Next.js standalone bundle.
# .env.production is copied into the context before `pnpm build`, so the
# NEXT_PUBLIC_* variables get baked into the client bundle.
# ============================================================================
FROM node:22-alpine AS builder
WORKDIR /app
RUN npm install -g pnpm@10

COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY . .
RUN pnpm build

# ============================================================================
# Production: run the standalone server (node server.js).
# Standalone does NOT include static assets or public/, so copy them explicitly.
# ============================================================================
FROM node:22-alpine AS production
WORKDIR /app
ENV NODE_ENV=production
ENV PORT=3000
ENV HOSTNAME=0.0.0.0

RUN addgroup -S app && adduser -S -G app app

COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
COPY --from=builder /app/public ./public
# Server-side runtime vars (e.g. CLERK_SECRET_KEY) also come from compose env_file;
# this keeps the image self-contained.
COPY --from=builder /app/.env.production ./.env.production

USER app
EXPOSE 3000

CMD ["node", "server.js"]
