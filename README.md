# TaskFlow

A Kanban-style task management application built with Spring Boot, Thymeleaf, and PostgreSQL. TaskFlow provides a visual board with three columns (To Do, In Progress, Done) where users can create, edit, assign, prioritize, and drag-and-drop tasks between stages.

## Tech Stack

- **Backend:** Java 17, Spring Boot 3.2, Spring Data JPA
- **Frontend:** Thymeleaf (server-side rendering), vanilla JavaScript (drag-and-drop)
- **Database:** PostgreSQL 16
- **Build:** Maven
- **Containerization:** Docker, Docker Compose

## Features

- Kanban board with three columns: To Do, In Progress, Done
- Create, edit, and delete tasks
- Assign tasks to team members and set deadlines
- Four priority levels: Low, Medium, High, Critical
- Drag-and-drop cards between columns (persisted via AJAX)
- Filter the board by assignee
- Responsive design for mobile and desktop
- REST API for programmatic access

## Getting Started

### Prerequisites

- Docker and Docker Compose installed

### Run with Docker Compose

```bash
git clone https://github.com/cfvbhgc/task-flow.git
cd task-flow
docker-compose up --build
```

The application will be available at [http://localhost:8080](http://localhost:8080).

### Run Locally (without Docker)

Make sure you have Java 17+, Maven 3.9+, and a running PostgreSQL instance.

```bash
# Set environment variables or update application.yml
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=taskflow
export DB_USERNAME=taskflow
export DB_PASSWORD=taskflow

mvn clean spring-boot:run
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Kanban board view |
| GET | `/tasks/new` | Create task form |
| POST | `/tasks` | Submit new task |
| GET | `/tasks/{id}/edit` | Edit task form |
| POST | `/tasks/{id}` | Submit task update |
| POST | `/tasks/{id}/delete` | Delete a task |
| GET | `/tasks/filter?assignee=` | Filter by assignee |
| GET | `/api/tasks` | List all tasks (JSON) |
| GET | `/api/tasks/{id}` | Get single task (JSON) |
| PATCH | `/api/tasks/{id}/status` | Move task to new column |

## Project Structure

```
src/main/java/com/taskflow/
  ├── TaskFlowApplication.java
  ├── config/AppConfig.java
  ├── controller/TaskController.java
  ├── controller/ApiController.java
  ├── dto/TaskCreateRequest.java
  ├── dto/TaskUpdateRequest.java
  ├── model/Task.java
  ├── model/TaskStatus.java
  ├── model/TaskPriority.java
  ├── repository/TaskRepository.java
  ├── service/TaskService.java
  └── service/TaskServiceImpl.java
```

## License

This project is open source and available under the MIT License.
