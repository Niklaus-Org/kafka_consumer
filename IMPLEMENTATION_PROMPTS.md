# 🎬 Real-Time Video Analytics Platform - Implementation Prompts

## 📋 Overview
This document contains two separate, comprehensive prompts:
1. **FRONTEND PROMPT** - For React Implementation
2. **BACKEND PROMPT** - For Spring Boot Implementation

Use these prompts with Claude/ChatGPT or any AI assistant to generate complete, production-ready code.

---

# 🖥️ FRONTEND IMPLEMENTATION PROMPT (React)

## Context
You are building a Real-Time Video Analytics Platform frontend using React. This is an enterprise-grade application that tracks user engagement with videos in real-time using Kafka events.

## Requirements

### Core Features

1. **Video Player Component**
   - Display video with play/pause/seek controls
   - Track currentTime, duration, quality settings
   - Support fullscreen mode
   - Handle buffering states
   - Emit events for: PLAY, PAUSE, SEEK, QUALITY_CHANGE, BUFFER_START, BUFFER_END, ENDED

2. **User Identification**
   - Generate unique userId using UUID on app initialization
   - Persist userId in localStorage/sessionStorage
   - Attach userId to every event sent to backend
   - Display current userId in UI (for debugging)

3. **Event Tracking System**
   - Create VideoEventDTO for every user action:
     ```
     {
       videoId: string,
       userId: string,
       eventType: "PLAY" | "PAUSE" | "SEEK" | "QUALITY_CHANGE" | "BUFFER_START" | "BUFFER_END" | "ENDED",
       watchedSeconds?: number,
       timestamp: ISO string,
       metadata?: {
         quality?: string,
         deviceType?: string,
         browserInfo?: string
       }
     }
     ```
   - Send events to backend API: POST /api/events
   - Use Axios for HTTP calls
   - Handle failed requests with retry logic
   - Queue events if backend is unavailable

4. **Analytics Dashboard**
   - Display real-time metrics:
     - Total Views (count of unique views)
     - Play Count (total play events)
     - Pause Count (total pause events)
     - Average Watch Time (in seconds)
     - Seek Count (total seek events)
   - Poll backend every 2 seconds: GET /api/analytics/{videoId}
   - Show last updated timestamp
   - Animate metric changes (color flash on update)
   - Support WebSocket as optional upgrade (Phase 8)

5. **Multi-Video Support**
   - Video selection dropdown/grid
   - Switch between videos without losing session
   - Keep userId consistent across video switches
   - Display per-video analytics

6. **User Experience**
   - Responsive design (mobile, tablet, desktop)
   - Beautiful UI with smooth animations
   - Loading states and error boundaries
   - Graceful error handling with user-friendly messages
   - Dark mode support (optional)

### Data Models (Frontend)

```typescript
interface Video {
  id: string;
  title: string;
  durationSeconds: number;
  uploadedAt: string;
  thumbnailUrl?: string;
}

interface VideoEvent {
  videoId: string;
  userId: string;
  eventType: EventType;
  watchedSeconds?: number;
  timestamp: string;
  metadata?: Record<string, any>;
}

enum EventType {
  PLAY = "PLAY",
  PAUSE = "PAUSE",
  SEEK = "SEEK",
  QUALITY_CHANGE = "QUALITY_CHANGE",
  BUFFER_START = "BUFFER_START",
  BUFFER_END = "BUFFER_END",
  ENDED = "ENDED"
}

interface VideoAnalytics {
  videoId: string;
  totalViews: number;
  playCount: number;
  pauseCount: number;
  seekCount: number;
  totalPlaytimeSeconds: number;
  lastUpdated: string;
}
```

### API Integration

```
POST /api/events
- Body: VideoEvent
- Response: { success: boolean, eventId: string }
- Headers: Content-Type: application/json

GET /api/analytics/{videoId}
- Response: VideoAnalytics
- Headers: Accept: application/json

GET /api/videos
- Response: Video[]
- Headers: Accept: application/json
```

### Project Structure

```
src/
├── components/
│   ├── VideoPlayer/
│   │   ├── VideoPlayer.jsx
│   │   ├── VideoPlayer.css
│   │   └── useVideoTracking.js (custom hook)
│   ├── Analytics/
│   │   ├── AnalyticsDashboard.jsx
│   │   ├── AnalyticsCard.jsx
│   │   ├── useAnalytics.js (custom hook)
│   │   └── AnalyticsDashboard.css
│   ├── VideoSelector/
│   │   ├── VideoSelector.jsx
│   │   └── VideoSelector.css
│   └── App.jsx
├── hooks/
│   ├── useUserId.js
│   ├── useVideoTracking.js
│   ├── useAnalytics.js
│   └── useEventQueue.js
├── services/
│   ├── apiService.js
│   ├── eventService.js
│   └── analyticsService.js
├── utils/
│   ├── constants.js
│   ├── helpers.js
│   └── logger.js
├── styles/
│   ├── global.css
│   ├── variables.css
│   └── animations.css
└── App.css

### Key Implementation Details

1. **Custom Hooks**
   - `useUserId()` - Generate and manage userId lifecycle
   - `useVideoTracking()` - Handle video event tracking
   - `useAnalytics()` - Fetch and manage analytics data
   - `useEventQueue()` - Queue and retry failed events

2. **Event Handling**
   - Debounce seek events to avoid spam (max 1 per 500ms)
   - Throttle quality change events (max 1 per 1000ms)
   - Batch events if possible before sending
   - Track precise currentTime for SEEK events

3. **Error Handling**
   - Retry failed requests with exponential backoff
   - Show error toast notifications
   - Log errors to console for debugging
   - Gracefully degrade if analytics unavailable

4. **Performance**
   - Memoize components to prevent unnecessary re-renders
   - Lazy load video metadata
   - Debounce analytics polling
   - Minimize re-renders with proper state management

### Testing Requirements

- Unit tests for custom hooks
- Integration tests for video player tracking
- Mock API responses for testing
- Test userId persistence
- Test event payload structure

### Deployment

- Build: `npm run build`
- Output: Static files ready for CDN
- Environment variables: REACT_APP_API_BASE_URL
- Optimize for production (bundle size, minification)

---

# ⚙️ BACKEND IMPLEMENTATION PROMPT (Spring Boot)

## Context
You are building the backend for a Real-Time Video Analytics Platform using Spring Boot. This service handles video event ingestion, Kafka publishing/consuming, and analytics aggregation.

## Architecture

```
┌──────────────────┐
│   React Client   │
└────────┬─────────┘
         │
    POST /api/events
    GET  /api/analytics
    GET  /api/videos
         │
┌────────▼─────────────────────┐
│   Spring Boot Application      │
│  - EventController            │
│  - AnalyticsController        │
│  - VideoController            │
│  - KafkaConfiguration         │
└────────┬─────────────────────┘
         │
    ┌────┴────┐
    │          │
    ▼          ▼
 Kafka      PostgreSQL
 (Events)   (Analytics)
```

## Core Components

### 1. **Event Controller (REST API)**
   
```java
POST /api/events
- Receives VideoEvent from frontend
- Validates event payload
- Publishes to Kafka topic "video-events"
- Returns: { success: boolean, eventId: string }

POST /api/events/batch
- Accept multiple events in single request
- Publish all to Kafka
- Returns: { successCount: int, failedCount: int }

GET /api/events/{videoId}
- Retrieve detailed event history for a video
- Pagination support
- Queryable by userId (optional)
- Returns: Page<VideoEvent>
```

### 2. **Analytics Controller (REST API)**

```java
GET /api/analytics/{videoId}
- Returns aggregated metrics for a video
- Real-time updated metrics
- Returns: VideoAnalytics

GET /api/analytics/{videoId}/by-user
- Analytics aggregated by userId
- Shows per-user engagement
- Returns: List<UserAnalytics>

GET /api/analytics/top-videos
- Top 10 videos by views
- Top 10 by engagement
- Returns: List<VideoAnalytics>
```

### 3. **Video Controller (REST API)**

```java
GET /api/videos
- List all available videos with metadata
- Pagination support
- Returns: Page<Video>

POST /api/videos
- Create new video entry
- Returns: Video

GET /api/videos/{videoId}
- Get video details with current analytics
- Returns: VideoWithAnalytics
```

### 4. **Kafka Configuration**

**Producer Configuration:**
- Bootstrap Servers: localhost:9092 (configurable)
- Topic: "video-events"
- Partitioner: CustomPartitioner (by videoId)
- Serializer: JSON
- Acks: all (durability)
- Retries: 3
- Batch Size: 32KB

**Consumer Configuration:**
- Group ID: "video-analytics-group"
- Topic: "video-events"
- Partition Assignment Strategy: Range
- Max Poll Records: 500
- Session Timeout: 10 seconds
- Poll Timeout: 100ms

### 5. **Kafka Producer**

```java
@Service
public class KafkaEventProducer {
    public void publishEvent(VideoEvent event);
    public void publishEventsBatch(List<VideoEvent> events);
    public ListenableFuture<SendResult<String, VideoEvent>> publishEventAsync(VideoEvent event);
}
```

**Key Points:**
- Use videoId as partition key (ensures ordering per video)
- Handle send failures gracefully
- Log sent events for debugging
- Optional: Implement dead-letter queue for failed events

### 6. **Kafka Consumer**

```java
@Service
public class VideoEventsConsumer {
    @KafkaListener(
        topics = "video-events",
        groupId = "video-analytics-group"
    )
    public void consumeEvent(VideoEvent event);
}
```

**Processing Logic:**
- Receive event from Kafka
- Validate event structure
- Update video_analytics table (aggregated metrics)
- Insert into video_events table (audit trail)
- Log processing status

### 7. **JPA Entities & Repositories**

```java
@Entity
@Table(name = "videos")
public class Video {
    @Id
    private String id;
    private String title;
    private Integer durationSeconds;
    private LocalDateTime uploadedAt;
    // getters, setters, constructors
}

@Entity
@Table(name = "video_events")
public class VideoEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String videoId;
    private String userId;
    
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    
    private Integer watchedSeconds;
    private LocalDateTime timestamp;
    private String metadata; // JSON stored as String
    // getters, setters, constructors
}

@Entity
@Table(name = "video_analytics")
public class VideoAnalytics {
    @Id
    private String videoId;
    private Long totalViews;
    private Long playCount;
    private Long pauseCount;
    private Long seekCount;
    private Long totalPlaytimeSeconds;
    private LocalDateTime lastUpdated;
    // getters, setters, constructors
}

// Repositories
@Repository
public interface VideoRepository extends JpaRepository<Video, String> {
    Page<Video> findAll(Pageable pageable);
}

@Repository
public interface VideoEventRepository extends JpaRepository<VideoEvent, String> {
    Page<VideoEvent> findByVideoId(String videoId, Pageable pageable);
    List<VideoEvent> findByVideoIdAndUserId(String videoId, String userId);
    List<VideoEvent> findByVideoIdAndEventType(String videoId, EventType eventType);
}

@Repository
public interface VideoAnalyticsRepository extends JpaRepository<VideoAnalytics, String> {
    Optional<VideoAnalytics> findByVideoId(String videoId);
    List<VideoAnalytics> findTop10ByOrderByTotalViewsDesc();
    List<VideoAnalytics> findTop10ByOrderByPlayCountDesc();
}
```

### 8. **Service Layer**

```java
@Service
public class AnalyticsService {
    public VideoAnalytics getAnalytics(String videoId);
    public List<UserAnalytics> getAnalyticsByUser(String videoId);
    public void updateAnalyticsFromEvent(VideoEvent event);
    public List<VideoAnalytics> getTopVideos(int limit, String sortBy);
}

@Service
public class VideoService {
    public Page<Video> getAllVideos(Pageable pageable);
    public Video getVideo(String videoId);
    public Video createVideo(VideoDTO dto);
    public VideoWithAnalytics getVideoWithAnalytics(String videoId);
}

@Service
public class EventService {
    public void publishEvent(VideoEvent event);
    public void publishEventsBatch(List<VideoEvent> events);
    public Page<VideoEvent> getEventHistory(String videoId, Pageable pageable);
    public List<VideoEvent> getUserEventHistory(String videoId, String userId);
}
```

### 9. **Database Schema**

```sql
CREATE TABLE videos (
    id VARCHAR(255) PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    duration_seconds INT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE video_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    video_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    watched_seconds INT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (video_id) REFERENCES videos(id),
    INDEX idx_video_id (video_id),
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp)
);

CREATE TABLE video_analytics (
    video_id VARCHAR(255) PRIMARY KEY,
    total_views BIGINT DEFAULT 0,
    play_count BIGINT DEFAULT 0,
    pause_count BIGINT DEFAULT 0,
    seek_count BIGINT DEFAULT 0,
    total_playtime_seconds BIGINT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (video_id) REFERENCES videos(id)
);

CREATE INDEX idx_events_video_user ON video_events(video_id, user_id);
CREATE INDEX idx_events_type ON video_events(event_type);
CREATE INDEX idx_analytics_views ON video_analytics(total_views DESC);
```

### 10. **Configuration Files**

**application.properties:**
```properties
# Server
server.port=8080
spring.application.name=video-analytics-platform

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/video_analytics
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.consumer.group-id=video-analytics-group
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.type.mapping=videoEvent:com.niketan.kafka.VideoEvent

# Logging
logging.level.root=INFO
logging.level.com.niketan.kafka=DEBUG
```

### 11. **Error Handling & Validation**

```java
// Custom Exceptions
public class VideoNotFoundException extends RuntimeException {}
public class EventPublishingException extends RuntimeException {}
public class AnalyticsProcessingException extends RuntimeException {}

// Global Exception Handler
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(VideoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleVideoNotFound(...);
    
    @ExceptionHandler(EventPublishingException.class)
    public ResponseEntity<ErrorResponse> handleEventPublishing(...);
}

// Input Validation
@RestController
@Validated
public class EventController {
    @PostMapping("/api/events")
    public ResponseEntity<?> publishEvent(
        @Valid @RequestBody VideoEvent event
    );
}
```

### 12. **Required Dependencies (pom.xml)**

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>3.2.0</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
        <version>3.2.0</version>
    </dependency>
    
    <!-- Kafka -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
        <version>3.1.0</version>
    </dependency>
    
    <!-- PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>42.7.0</version>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
        <version>3.2.0</version>
    </dependency>
    
    <!-- Lombok (optional, for reducing boilerplate) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
        <scope>provided</scope>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <version>3.2.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 13. **Testing Requirements**

- Unit tests for services (mock repositories)
- Integration tests for Kafka producer/consumer
- Controller tests with MockMvc
- Repository tests with embedded PostgreSQL
- Mock Kafka broker for testing
- Test event payload structure validation

### 14. **CORS Configuration (for React Frontend)**

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:3000")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*");
            }
        };
    }
}
```

### 15. **Docker Setup (Optional)**

**Dockerfile:**
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/video-analytics-platform.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Docker Compose:**
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: video_analytics
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
  
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
    ports:
      - "2181:2181"
  
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    ports:
      - "9092:9092"
  
  app:
    build: .
    depends_on:
      - postgres
      - kafka
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/video_analytics
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
    ports:
      - "8080:8080"
```

---

## 🚀 Implementation Order

### Phase 1: Foundation (Week 1)
1. Set up Spring Boot project structure
2. Configure PostgreSQL and create database schema
3. Create JPA entities
4. Create repositories and basic services

### Phase 2: Event System (Week 2)
1. Implement Kafka configuration
2. Create KafkaEventProducer
3. Create EventController (REST API)
4. Add input validation

### Phase 3: Analytics Processing (Week 2)
1. Create VideoEventsConsumer
2. Implement analytics aggregation logic
3. Create AnalyticsService
4. Add AnalyticsController

### Phase 4: Additional Features (Week 3)
1. Create VideoController
2. Add CORS configuration
3. Implement error handling
4. Add logging and monitoring

### Phase 5: Testing & Deployment (Week 3+)
1. Write comprehensive tests
2. Create Docker setup
3. Performance optimization
4. Production hardening

---

## 📊 Success Metrics

- ✅ Events published at 1000+ events/sec
- ✅ Consumer lag < 5 seconds
- ✅ Analytics update within 2-3 seconds
- ✅ 99.9% event delivery rate
- ✅ API response time < 200ms
- ✅ Support 10K concurrent users

---

## 🔗 Integration Points

1. **Frontend → Backend Communication**
   - POST /api/events (send tracking events)
   - GET /api/analytics/{videoId} (fetch metrics)
   - GET /api/videos (fetch video list)

2. **Backend → Kafka Communication**
   - Publish VideoEvent to "video-events" topic
   - Consume from "video-events" topic
   - Process and aggregate metrics

3. **Backend → Database Communication**
   - Store raw events in video_events table
   - Update aggregated metrics in video_analytics
   - Query for historical data and reports

---

## 📝 Notes

- Both frontend and backend should be built independently
- Use these prompts with Claude/ChatGPT for code generation
- Follow the exact specifications for API contracts
- Test integration between frontend and backend
- Use provided database schema exactly as specified
- Configure Kafka with exact settings for consistency

---

**Created:** December 2025
**For:** Real-Time Video Analytics Platform
**Stack:** React + Spring Boot + Apache Kafka + PostgreSQL
