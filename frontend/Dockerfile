# Next.js 프론트엔드를 production build 후 next start로 실행합니다.
# NEXT_PUBLIC_API_BASE_URL은 빌드 시점에 브라우저 번들에 포함됩니다.
FROM node:20-alpine AS deps

WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci

FROM node:20-alpine AS build

WORKDIR /app
ARG NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
ENV NEXT_PUBLIC_API_BASE_URL=$NEXT_PUBLIC_API_BASE_URL
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN npm run build

FROM node:20-alpine AS runner

WORKDIR /app
ENV NODE_ENV=production
ENV NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
COPY --from=build /app/.next/standalone ./
COPY --from=build /app/.next/static ./.next/static
EXPOSE 3000

CMD ["node", "server.js"]
