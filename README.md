# Chat-GPT Assistant Backend
> (File is generated by ChatGPT-4)

Chat-GPT Assistant Backend is a Spring Boot powered web application that uses Kotlin and MongoDB to provide a chatbot experience powered by OpenAI's GPT models.

## Features

- Spring Boot 3.0.5
- Kotlin 1.7.22
- Reactive MongoDB support
- Actuator for monitoring and management
- WebFlux for reactive web programming
- Testcontainers for integration testing
- Jib for building Docker images
- OpenAI Kotlin API

## Prerequisites

- JDK 17
- MongoDB instance (local or remote)
- Docker (optional)

## Setup

1. Clone the repository.

```
git clone https://github.com/chat-gpt-assistant/assistant-back.git
cd assistant-back
```

2. Update the `application-local.yml` and/or `application-docker.yml` file with your MongoDB connection details, OpenAI API key, and any other configurations you need.

3. For production, update the `application-prod.yml` file in the `src/main/resources` folder with your production configurations.

4. Build the project.

```
./gradlew build
```

5. Run the application.

```
./gradlew bootRun
```

The application will start, and you can access it at `http://localhost:8080`.

## MongoDB Setup with Docker

You can set up a MongoDB instance using Docker and the provided `mongo.yml` file in the `/src/main/docker` folder.

Run the MongoDB Docker container.

```
docker-compose -f ./src/main/docker/mongo.yml up -d
```

This command will start a MongoDB container with the specified configuration, exposing the MongoDB port. Make sure your application configuration matches the settings in the `mongo.yml` file.

## Testing

To run the tests, execute the following command:

```
./gradlew test
```

## Building a Docker image

To build a Docker image using Jib, run:

```
./gradlew jibDockerBuild
```

This will create a Docker image named `com.github.chat-gpt-assistant/assistant-back:0.0.1-SNAPSHOT`. You can then run the application using Docker:

```
docker run -d -p 8080:8080 --network docker_chatgptassistant-net -e SPRING_PROFILES_ACTIVE=docker --name chat-gpt-assistant com.github.chat-gpt-assistant/assistant-back:0.0.1-SNAPSHOT
```

## Contributing

Feel free to submit pull requests, create issues, or ask questions. We welcome any contributions and feedback.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
