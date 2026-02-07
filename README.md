# Matches Application

A Spring Boot application for a matching/dating service platform.

## Features

*   **User Management**: Register, update, and manage users with extended profiles (bio, location, interests).
*   **Password Security**: Passwords are securely hashed using BCrypt.
*   **Image Upload**: Users can upload up to 3 images, with profile picture management.
*   **Matching System**:
    *   **Swipe Feed**: Get a list of potential matches (`/feed`).
    *   **Like/Dislike**: Users can like or dislike others.
    *   **Mutual Matches**: Matches are automatically created on mutual likes.
*   **Search**:
    *   Users can set their preferences (target gender, age range).
    *   Search for users based on set preferences.
*   **Premium Features**:
    *   **Like Limiting**: Non-premium users are limited to 10 likes per day.
    *   **View Likers**: Premium users can see who has liked them.
*   **Real-time Chat**:
    *   WebSocket and STOMP for real-time messaging between matched users.
    *   API to retrieve chat history.
*   **Caching**: Redis integration for caching user data to improve performance.
*   **API Documentation**: Integrated Swagger UI for easy API exploration and testing.

## Technologies

*   **Java**: 21
*   **Framework**: Spring Boot 3.5.3, Spring Security, Spring WebSockets
*   **Database**: MySQL 8
*   **Cache**: Redis
*   **Testing**: JUnit 5, Testcontainers
*   **Build Tool**: Maven

## Prerequisites

*   JDK 21
*   Docker (required for Redis and MySQL containers during testing and local development)
*   Maven

## Configuration

The application is configured to use MySQL and Redis. The default configuration in `application.properties` points to `localhost`. For testing, Testcontainers will automatically spin up the required services.

## How to Run

1.  **Clone the repository**
2.  **Build the project**:
    ```bash
    mvn clean install
    ```
3.  **Run the application**:
    ```bash
    mvn spring-boot:run
    ```
    *Alternatively, you can run the application from your IDE.*

## API Documentation

Once the application is running, you can access the Swagger UI documentation at:

```
http://localhost:8080/swagger-ui.html
```
This interface allows you to view all available endpoints and test them directly from your browser.

## Testing

The project uses **Testcontainers** for integration testing. This ensures that tests run against real MySQL and Redis instances in ephemeral Docker containers, providing a high degree of confidence.

To run tests:
```bash
mvn test
```
*Note: Docker must be running on your machine for tests to execute.*
