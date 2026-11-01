# Video Streaming Platform

A microservices-based video streaming platform (YouTube-like) built with Java (Spring Boot) and Python (FastAPI), connected via Kafka and backed by PostgreSQL, Redis, Elasticsearch, and MinIO.

## Architecture

```
                       ┌─────────────────────┐
                       │   Client / Frontend │
                       └──────────┬──────────┘
                                  │
        ┌─────────────┬───────────┼───────────┬─────────────┐
        ▼             ▼           ▼           ▼             ▼
   user-service  video-service streaming  search-service  engagement
    (auth/JWT)    (upload)  (HLS/DASH)     (Elasticsearch) (comments/likes)
                    │          │
                 Kafka    Kafka(view.events)
                    │          │
     ┌──────────────┼───┐      ├────────────────────────────┐
     ▼              ▼   ▼      ▼                            ▼
transcoding  thumbnail moderation              recommendation  analytics
  worker        service    service               service        service
                    │
                    ▼
             notification-service
```

### Services

| Service | Language | Port | Purpose |
| --- | --- | --- | --- |
| `user-service` | Java 17 / Spring Boot | 8081 | Auth (JWT), users, channels, subscriptions |
| `video-service` | Java 17 / Spring Boot | 8082 | Chunked/resumable uploads, metadata, playlists |
| `streaming-service` | Java 17 / Spring Boot | 8083 | Adaptive bitrate streaming, HLS/DASH manifests, playback sessions |
| `engagement-service` | Java 17 / Spring Boot | 8084 | Comments and likes |
| `transcoding-worker` | Python / FastAPI | 8091 | ffmpeg transcoding, HLS/DASH packaging (144p -> 4K) |
| `thumbnail-service` | Python / FastAPI | 8092 | Frame extraction and thumbnail generation |
| `moderation-service` | Python / FastAPI | 8093 | NSFW / content classification (ML) |
| `search-service` | Python / FastAPI | 8094 | Elasticsearch-backed search + autocomplete |
| `recommendation-service` | Python / FastAPI | 8095 | Watch-time prediction + personalized recommendations |
| `analytics-service` | Python / FastAPI | 8096 | View event aggregation pipelines |
| `notification-service` | Python / FastAPI | 8097 | Push/email notifications to subscribers |

### Event flow

1. `video-service` accepts a chunked upload (`POST /api/videos/init`, `/upload/{sessionId}/chunk`).
2. On completion it publishes `video.uploaded` on Kafka and sets status `PROCESSING`.
3. `transcoding-worker` consumes the event, transcodes into a resolution ladder, packages HLS/DASH, and publishes `video.ready`.
4. `thumbnail-service` and `moderation-service` also consume `video.uploaded`.
5. `streaming-service` serves `GET /api/stream/watch/{videoId}` with signed, expiring CDN URLs and publishes `view.events`.
6. `recommendation-service` and `analytics-service` consume `view.events` for rankings and metrics.
7. `notification-service` consumes `video.uploaded` to notify channel subscribers.

## Kafka topics

- `video.uploaded` - published by video-service after upload completes
- `video.ready` - published by transcoding-worker when renditions are ready
- `view.events` - published by streaming-service on playback activity

## Getting started (local)

```bash
# 1. Start infrastructure + services
docker compose -f infrastructure/docker-compose/docker-compose.local.yml up -d --build

# 2. Build a single service
cd services/video-service
mvn clean package -DskipTests

# 3. Run a Python service directly
cd services/transcoding-worker
pip install -r requirements.txt
uvicorn app.main:app --port 8091
```

## Directory layout

```
services/            Individual microservices
infrastructure/      docker-compose, kubernetes manifests
shared/              Reusable java/python common libraries
```

## Production notes

- All secrets are configurable via environment variables; never hardcode keys.
- CDN URL signing in `streaming-service` protects media endpoints with expiring tokens.
- The `shared/` libraries are placeholders intended for common DTOs, Kafka config, and tracing helpers.
- Kubernetes manifests live under `infrastructure/kubernetes/`.
