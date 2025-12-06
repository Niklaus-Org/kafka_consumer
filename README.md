# Kafka Consumer - Driver Location Service

A Spring Boot application that consumes real-time driver location updates from Apache Kafka. This service acts as a Kafka consumer, listening to driver location data (coordinates and location details) from a Kafka topic for processing and monitoring.

## 📋 Project Overview

This is a **Kafka Consumer** application built with Spring Boot 4.0.0 and Java 17. It subscribes to Kafka topics to receive driver location information and processes it for real-time tracking and analytics.

### Use Case
- Listen for driver location updates from Kafka
- Process and log incoming driver location data in real-time
- Enable real-time monitoring of driver positions (latitude, longitude, location name)
- Act as a bridge between Kafka and downstream processing systems

## 🏗️ Architecture

### Components

```
┌─────────────────────────────────────────┐
│      Apache Kafka                       │
│  (Topic: driver-location-updates)       │
└────────────────┬────────────────────────┘
                 │
                 ↓
        ┌─────────────────────┐
        │  KafkaConfiguration │
        │  (ConsumerFactory,  │
        │   Message Deserial.)│
        └──────────┬──────────┘
                   │
                   ↓
        ┌──────────────────────────┐
        │    KafkaConsumer         │
        │  (@KafkaListener)        │
        │  (consumes messages)     │
        └──────────┬───────────────┘
                   │
                   ↓
        ┌──────────────────────────┐
        │  Data Processing Layer   │
        │  (logs, analytics,       │
        │   storage, etc.)         │
        └──────────────────────────┘
```

## 📁 Project Structure

```
kafkalearning/
├── src/
│   ├── main/
│   │   ├── java/com/niketan/kafka/kafkalearning/
│   │   │   ├── KafkalearningApplication.java      # Spring Boot entry point
│   │   │   ├── consumer/
│   │   │   │   └── KafkaConsumer.java             # Consumer listener service
│   │   │   └── model/
│   │   │       └── DriverLocation.java            # Data model for driver location
│   │   └── resources/
│   │       └── application.properties             # Configuration properties
│   └── test/
│       └── java/com/niketan/kafka/kafkalearning/
│           └── KafkalearningApplicationTests.java
├── pom.xml                                         # Maven dependencies
└── README.md                                       # This file
```

## 🔧 Key Components

### 1. **KafkaConsumer** (`consumer/KafkaConsumer.java`)
Service that listens to Kafka topics:
- Uses `@KafkaListener` annotation to consume messages from topic
- Subscribes to **driver-location-updates** topic
- Part of **driver-location-group** consumer group
- Processes incoming ConsumerRecord objects containing driver location data
- Logs consumed records with key and value information

### 2. **DriverLocation Model** (`model/DriverLocation.java`)
Data model representing a driver's location:
- `driverId` - Unique identifier for the driver
- `latitude` - Driver's latitude coordinate
- `longitude` - Driver's longitude coordinate
- `location` - Human-readable location name

### 3. **KafkalearningApplication** (`KafkalearningApplication.java`)
Spring Boot application entry point:
- Enables Spring Boot auto-configuration
- Initializes the application context
- Activates Kafka consumer listeners

## 🚀 Getting Started

### ⚠️ Prerequisites - Publisher Requirement

**This consumer requires the Kafka Publisher to be up and running before starting!**

- Clone and set up the **Kafka Publisher** repository first: [Kafka Publisher](https://github.com/Niklaus-Org/kafka_publisher.git)
- Ensure the Publisher application is running on **http://localhost:8081**
- The Publisher is responsible for sending driver location messages to Kafka that this consumer will receive

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Apache Kafka 2.8+ running locally or remotely
- Spring Boot 4.0.0
- **Kafka Publisher running on port 8081** (REQUIRED)

### Installation & Setup

1. **Clone/Download the project**
   ```bash
   cd kafkalearning
   ```

2. **Ensure Kafka is running**
   ```bash
   # Make sure Kafka broker is accessible at localhost:9092
   ```

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```
   
   The application will start on **http://localhost:8082** and begin listening to Kafka topics

## ⚙️ Configuration

Edit `src/main/resources/application.properties` to configure:

```properties
# Server port (default: 8082)
server.port=8082

# Kafka Bootstrap Servers
spring.kafka.bootstrap-servers=localhost:9092

# Consumer Group ID
spring.kafka.consumer.group-id=driver-location-group

# Auto offset reset strategy (earliest: start from beginning, latest: only new messages)
spring.kafka.consumer.auto-offset-reset=earliest

# Key Deserializer
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer

# Value Deserializer (JSON format)
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer

# Trusted packages for JSON deserialization
spring.kafka.consumer.properties.spring.json.trusted.packages=*

# Kafka Topic for driver locations
kafka.topic.driver-location=driver-location-updates
```

## 📡 How It Works

1. **Application Startup**: Consumer starts and registers listeners with Kafka broker
2. **Topic Subscription**: KafkaConsumer subscribes to **driver-location-updates** topic
3. **Message Reception**: When messages are published to the topic, Kafka delivers them to all consumers in the group
4. **Message Processing**: `consumeRecord()` method receives ConsumerRecord containing:
   - **Key**: Driver ID (used for partitioning)
   - **Value**: DriverLocation object (latitude, longitude, location details)
5. **Logging**: Consumed messages are logged to console output
6. **Data Handling**: Messages can be further processed for storage, analytics, notifications, etc.

## 📋 Example Output

When running the consumer and publisher together, you'll see console logs like:

```
Consumed record with key: driver001 and value: DriverLocation(driverId=driver001, latitude=37.7749, longitude=-122.4194, location=San Francisco)
Consumed record with key: driver002 and value: DriverLocation(driverId=driver002, latitude=34.0522, longitude=-118.2437, location=Los Angeles)
Consumed record with key: driver003 and value: DriverLocation(driverId=driver003, latitude=40.7128, longitude=-74.0060, location=New York City)
```

## 🔄 Consumer Group Behavior

- **Consumer Group ID**: `driver-location-group`
- **Multiple Instances**: Can run multiple consumer instances with the same group ID for load balancing
- **Partition Distribution**: Kafka automatically distributes topic partitions among consumers in the group
- **Offset Management**: Consumer group tracks processed messages using offsets

## 🐛 Debugging & Logs

The application includes console logging to monitor consumer activity:
- Displays consumed records with their keys and values
- Shows when the consumer connects to Kafka broker
- Displays any deserialization or processing errors

Typical log messages:
```
Consumed record with key: driver123 and value: DriverLocation(driverId=driver123, latitude=40.7128, longitude=-74.0060, location=New York City)
```

## 🚀 Production Considerations

- **Error Handling**: Add exception handling and dead-letter topic for failed messages
- **Logging**: Implement proper logging framework (SLF4J/Log4j) instead of System.out
- **Monitoring**: Add metrics and health checks for consumer lag monitoring
- **Persistence**: Store processed data in database or data warehouse
- **Retry Logic**: Implement retry policies for failed message processing
- **Performance**: Tune consumer batch size and poll timeout for optimal throughput
- **Security**: Add authentication and TLS/SSL for Kafka connections
- **Concurrency**: Configure thread pools for parallel message processing

## 🔗 Integration with Publisher

To see the full workflow:

1. **Start Kafka broker** on localhost:9092
2. **Start the Publisher** (runs on port 8081)
3. **Start the Consumer** (runs on port 8082)
4. **Send messages** via Publisher API to `POST /kafka/publish`
5. **Consumer automatically receives** and processes the messages

## 🎓 Self-Learning Project: Real-Time Video Analytics Platform

This repository includes a comprehensive **self-learning project** that demonstrates how to build an **enterprise-grade, real-time video engagement analytics platform** using Apache Kafka, Spring Boot, React, and PostgreSQL.

### 📖 Project Documentation

The complete learning material is available in the `self-learn-project-doc/` folder:

- **[Interactive Dashboard](./self-learn-project-doc/project-dashboard.html)** - Start here! Open this HTML file in your browser to see:
  - ✅ **12-Phase Implementation Roadmap** with 150+ tasks across 4-6 weeks
  - ✅ **Complete Architecture Overview** - Problem statements, solutions, and benefits
  - ✅ **Scale Capacity Analysis** - How the system handles 10K to 100M+ concurrent users
  - ✅ **Multi-User Tracking** - How real-time events are tracked for individual users
  - ✅ **User Identification** - 7-step journey showing how userId flows through entire system
  - ✅ **Database Schema** - Two-table design for aggregated metrics and audit trails
  - ✅ **Query Examples** - Real-world SQL patterns for analytics

### 🚀 Quick Start with Self-Learning Project

1. **View the Interactive Dashboard** (No setup required!)
   ```bash
   # Simply open in your browser:
   open self-learn-project-doc/project-dashboard.html
   # or
   # Navigate to: file:///.../self-learn-project-doc/project-dashboard.html
   ```

2. **Understand the Architecture**
   - Start with "Project Overview" section in the dashboard
   - Review the "Problems vs Solutions" to understand real-world challenges
   - Study the "Multi-User Tracking Architecture" to see how events flow

3. **Learn the Scaling Concepts**
   - Check "Scale Capacity" section showing Baseline → Enterprise → Global scale
   - Understand how Kafka partitioning enables horizontal scaling
   - See how consumer groups distribute load

4. **Deep Dive into User Identification**
   - Review "How System Identifies & Tracks Individual Users" section
   - Study the 7-step journey from browser to database
   - Examine SQL queries showing per-user vs aggregated analytics

### 📋 What You'll Learn

**Architecture & Design:**
- Event-driven microservices architecture
- Kafka topic partitioning strategies
- Database schema design for analytics (aggregated + audit trail)
- Real-time data flow design

**Implementation Topics (Phase Breakdown):**
- Phase 1-2: Project setup and backend foundation
- Phase 3: Spring Boot REST APIs and Kafka configuration
- Phase 4-5: Frontend React components and video player integration
- Phase 6-7: Real-time WebSocket implementation
- Phase 8-9: Database scaling and optimization
- Phase 10-12: Deployment, monitoring, and production hardening

**Real-World Patterns:**
- How to identify and track individual users in real-time
- Aggregating metrics across millions of events efficiently
- Handling concurrent multi-user scenarios
- Building compliance-ready audit trails

### 🎯 Use Cases Covered

- 🎬 **Video Analytics** - Track play/pause/seek events per user
- 📊 **Real-Time Dashboards** - Display live metrics as events stream in
- 👥 **Multi-User Scenarios** - Independently track multiple users on same video
- 🔍 **Debugging** - Query individual user behavior when issues occur
- 🤖 **Machine Learning** - Use detailed audit trail for recommendations
- 📋 **Compliance** - Maintain immutable event log for auditing

### 🏗️ Technology Stack

- **Backend:** Spring Boot 3.2+, Spring Kafka, Spring Data JPA
- **Frontend:** React 18+, Axios, WebSocket (optional)
- **Message Broker:** Apache Kafka 3.x
- **Database:** PostgreSQL 14+
- **Containerization:** Docker & Docker Compose
- **Language:** Java 17+, JavaScript/TypeScript

### 📈 Project Statistics

- **12 Phases** of implementation
- **150+ Tasks** covering all aspects
- **4-6 Weeks** estimated timeline
- **10K+ Events/sec** baseline throughput
- **100M+ Users** at global scale

### 🔗 GitHub Repository Structure

```
kafka-consumer/
├── README.md                          (this file)
├── src/                               (Kafka Consumer code)
├── pom.xml                            (Maven configuration)
└── self-learn-project-doc/            (📚 Self-Learning Material)
    ├── project-dashboard.html         (Interactive learning dashboard)
    └── README.md                      (Self-learning guide)
```

### 💡 How to Use This Learning Material

1. **Start with the Dashboard** - Open `project-dashboard.html` in your browser for interactive learning
2. **Expand Each Phase** - Click on phase cards to see detailed tasks and subtasks
3. **Follow the Checklist** - Progress tracking is saved locally in your browser
4. **Study the Architecture Sections** - Review multi-user tracking, user identification, and scale capacity
5. **Reference the Queries** - Use SQL examples for your own implementations
6. **Implement Phase by Phase** - Use this as a roadmap for building your own system

### 🌟 Key Insights from This Project

- **The Golden Thread:** userId attached to every event enables individual tracking AND aggregated metrics
- **Two-Table Strategy:** Separate `video_events` (detailed) and `video_analytics` (aggregated) tables for optimal performance
- **Kafka as Event Log:** Immutable event log provides compliance, debugging, and ML training data
- **Event-Driven at Scale:** Partitioning by videoId ensures ordering and enables horizontal scaling
- **Frontend Responsibility:** Browser generates UUID, enabling independent user tracking without server-side session overhead

## 📚 Additional Resources

- [Spring Boot Kafka Documentation](https://spring.io/projects/spring-kafka)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Boot Reference](https://spring.io/projects/spring-boot)
- [Kafka Consumer Groups](https://kafka.apache.org/documentation/#consumerconfigs)
- 📖 [Self-Learning Project Guide](./self-learn-project-doc/README.md)

## 📝 License

This is a learning project. Modify as needed for your use case.

## 👨‍💻 Author

Niketan
