# ── Stage 1: Build ──────────────────────────────────────────────────
FROM node:20-alpine AS build
WORKDIR /app

# Copy package files and install dependencies
COPY package.json package-lock.json* ./
RUN npm install

# Copy source code
COPY . .

# Pass build arg to env
ARG VITE_API_BASE=/api
ENV VITE_API_BASE=$VITE_API_BASE

# Build the Vite app for production
RUN npm run build

# ── Stage 2: Serve with Nginx ───────────────────────────────────────
FROM nginx:alpine

# Copy built assets to Nginx html directory
COPY --from=build /app/dist /usr/share/nginx/html

# Copy custom Nginx configuration to handle React routing and backend proxy
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
