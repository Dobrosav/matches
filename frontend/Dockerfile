FROM ghcr.io/cirruslabs/flutter:stable AS builder

USER root
WORKDIR /app
RUN flutter config --no-analytics
RUN git config --global --add safe.directory '*'
COPY pubspec.yaml ./
RUN flutter pub get

COPY . .

RUN flutter build web

FROM nginx:alpine
WORKDIR /usr/share/nginx/html
RUN rm -rf ./*
COPY --from=builder /app/build/web .
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]