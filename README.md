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
*   **Framework**: Spring Boot 3.3.4, Spring Security, Spring WebSockets
*   **Database**: MySQL 8
*   **Cache**: Redis
*   **Testing**: JUnit 5, Testcontainers
*   **Build Tool**: Maven

## Prerequisites

*   JDK 21
*   Docker (required for Redis and MySQL containers during testing and local development)
*   Maven

## Configuration

The application is configured through `application.properties` and can be overridden with environment variables, which is the recommended approach for sensitive data.

The default configuration in `application.properties` points to `localhost`. For testing, Testcontainers will automatically spin up the required services.

### Environment Variables

For local development, it's best to configure your IDE's run configuration with the following environment variables:

| Variable              | Description                       | Default Value   |
| --------------------- | --------------------------------- | --------------- |
| `SPRING_DATASOURCE_URL` | JDBC URL for the MySQL database   | `jdbc:mysql://localhost:3306/matches` |
| `SPRING_DATASOURCE_USERNAME` | Database username               | `user`          |
| `SPRING_DATASOURCE_PASSWORD` | Database password                 | `password`      |
| `SPRING_REDIS_HOST`   | Hostname for the Redis server     | `localhost`     |
| `SPRING_REDIS_PORT`   | Port for the Redis server         | `6379`          |

These values correspond to the services defined in the `docker-compose.yml` file.

## How to Run

This application can be run in two ways:

1.  **Via Docker Compose (Recommended for Production/Staging)**: Simple, one-command startup.
2.  **Locally from IDE (Recommended for Development)**: Ideal for debugging and active development.

### 1. Running with Docker Compose

This method builds the application and runs it along with MySQL and Redis containers.

1.  **Prerequisites**: Ensure you have Docker and Docker Compose installed.
2.  **Build and Run**: From the root of the project, run the following command:
    ```bash
    docker compose up --build
    ```
    This command will:
    *   Build the Spring Boot application using a multi-stage `Dockerfile`.
    *   Start containers for the application, a MySQL database, and a Redis cache.
    *   The application will be available at `http://localhost:8080`.

To stop the application and all related services, run:
```bash
docker compose down
```

### 2. Running Locally for Development

This setup allows you to run the Spring Boot application directly from your IDE (like IntelliJ or VS Code) for a better debugging experience, while still using Docker for backing services.

1.  **Start Services**: First, start only the database and cache using Docker Compose:
    ```bash
    docker compose up -d mysql redis
    ```
    The `-d` flag runs the containers in detached mode.

2.  **Configure Environment**: Set the required environment variables in your IDE's run configuration. See the **Configuration** section below for details.

3.  **Run Application**: Start the application by running the `main` method in `MatchesApplication.java` from your IDE. The application will connect to the MySQL and Redis containers running on your local machine.

## API Documentation

Once the application is running, you can access the Swagger UI documentation at:

```
http://localhost:8080/swagger-ui.html
```
This interface allows you to view all available endpoints and test them directly from your browser.

### Key API Endpoints

Here is a summary of some of the core API endpoints:

| Method | Path                           | Description                               | Authentication |
| ------ | ------------------------------ | ----------------------------------------- | -------------- |
| POST   | `/api/users/register`          | Registers a new user.                     | Public         |
| POST   | `/api/users/login`             | Authenticates a user and returns a token. | Public         |
| GET    | `/api/feed`                    | Gets the swipe feed of potential matches. | Required       |
| POST   | `/api/likes/{userId}`          | Likes a user.                             | Required       |
| GET    | `/api/matches`                 | Retrieves a list of mutual matches.       | Required       |
| GET    | `/api/messages/{matchId}`      | Gets the chat history with a matched user.| Required       |

## Testing

The project uses **Testcontainers** for integration testing. This ensures that tests run against real MySQL and Redis instances in ephemeral Docker containers, providing a high degree of confidence.

To run tests:
```bash
mvn test
```
*Note: Docker must be running on your machine for tests to execute.*
