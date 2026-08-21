# syntax=docker/dockerfile:1

# ---- Build stage --------------------------------------------------------
FROM node:22-alpine AS build
WORKDIR /workspace

# npm ci before the source copy so dependency installs cache independently
# of application code changes.
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY . .
# angular.json's build target defaults to the production configuration.
RUN npm run build

# ---- Runtime stage --------------------------------------------------------
# nginx-unprivileged: runs as a non-root user on port 8080 out of the box,
# rather than patching permissions onto the standard nginx image.
FROM nginxinc/nginx-unprivileged:1.27-alpine AS runtime

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/dist/dex-insights-ui/browser /usr/share/nginx/html

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -qO- http://localhost:8080/ >/dev/null || exit 1
