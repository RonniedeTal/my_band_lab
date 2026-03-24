# 🎵 My Band Lab
[![Taiga](https://img.shields.io/badge/Taiga-Project-2E86AB?style=flat-square&logo=taiga&logoColor=white)](https://tree.taiga.io/project/ronniedetal-my-band-lab)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![GraphQL](https://img.shields.io/badge/GraphQL-2.0.2-pink.svg)](https://graphql.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17.6-blue.svg)](https://www.postgresql.org/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📊 Project Management

Development is tracked on **Taiga**:

- 📋 [Backlog](https://tree.taiga.io/project/ronniedetal-my-band-lab/backlog) – Prioritized user stories
- 🗂️ [Kanban](https://tree.taiga.io/project/ronniedetal-my-band-lab/kanban) – Work in progress board
- 🏷️ [Epics](https://tree.taiga.io/project/ronniedetal-my-band-lab/epics) – Major features
- 📚 [Wiki](https://tree.taiga.io/project/ronniedetal-my-band-lab/wiki) – Complete documentation
- 🐞 [Issues](https://tree.taiga.io/project/ronniedetal-my-band-lab/issues) – Bugs and tasks

## 📋 Description

**My Band Lab** is a music band management application that allows you to:
- 👥 Manage users and artists
- 🎸 Create music groups
- 🎵 Assign predefined music genres
- 👥 Add members to groups
- 🎤 Track individual artists

## ✨ Features

- ✅ **Full CRUD** operations for users
- ✅ **Individual artists** with stage names and biographies
- ✅ **Music groups** with founders and members
- ✅ **Predefined music genres** (Rock, Pop, Jazz, Metal, etc.)
- ✅ **REST API** for simple operations
- ✅ **GraphQL API** for flexible and efficient queries
- ✅ **Data validation** with Bean Validation
- ✅ **Custom exception handling**
- ✅ **Bidirectional JPA relationships**

## 🏗️ Technologies

- **Java 21** - Programming language
- **Spring Boot 4.0.3** - Main framework
- **Spring Data JPA** - Data persistence
- **Spring GraphQL** - GraphQL API
- **Spring Validation** - Data validation
- **PostgreSQL** - Relational database
- **Lombok** - Boilerplate code reduction
- **Maven** - Dependency management

## 📁 Project Structure
src/main/java/com/my_band_lab/my_band_lab/
├── controller/
│ ├── UserController.java # REST API
│ └── UserGraphQLController.java # GraphQL API
├── entity/
│ ├── User.java # User entity
│ ├── Artist.java # Artist entity
│ ├── MusicGroup.java # Music group entity
│ └── MusicGenre.java # Music genre enum
├── repository/
│ ├── UserRepository.java
│ ├── ArtistRepository.java
│ └── MusicGroupRepository.java
├── service/
│ ├── UserService.java
│ ├── UserServiceImpl.java
│ ├── ArtistService.java
│ ├── ArtistServiceImpl.java
│ ├── MusicGroupService.java
│ └── MusicGroupServiceImpl.java
└── MyBandLabApplication.java

text

## 🚀 Installation & Setup

### 📋 Prerequisites

- Java 21 or higher
- PostgreSQL 17 or higher
- Maven 3.8 or higher (optional)

### 🔧 Database Setup

1. Create the database in PostgreSQL:
```sql
CREATE DATABASE my_lab_band;
Configure credentials in application-dev.yml (create from application-example.yml):

yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/my_lab_band
    username: postgres
    password: your_password
💻 Run the Application
Option 1: From IDE (IntelliJ IDEA)
Open the project

Run MyBandLabApplication.java

Option 2: From terminal with Maven
bash
# Clean and compile
mvn clean install

# Run the application
mvn spring-boot:run
Option 3: From JAR file
bash
# Generate JAR
mvn clean package

# Run JAR
java -jar target/my_band_lab-0.0.1-SNAPSHOT.jar
📡 API Endpoints
🌐 REST API
Method	Endpoint	Description
POST	/saveUser	Create user
GET	/user/id/{id}	Find user by ID
GET	/user/name/{name}	Find by name
GET	/user/surname/{surname}	Find by surname
GET	/user/fullname?name=X&surname=Y	Find by full name
PUT/PATCH	/user/update/{id}	Update user
DELETE	/user/delete/{id}	Delete user
🔮 GraphQL API
Endpoint: http://localhost:9000/graphql

Available Queries
graphql
# Get all users
query {
  users {
    id
    name
    surname
    email
    artist {
      stageName
      genre
    }
    musicGroups {
      id
      name
    }
  }
}

# Find user by ID
query {
  userById(id: 1) {
    name
    surname
    email
  }
}

# Get all artists
query {
  artists {
    id
    stageName
    genre
    user {
      name
    }
  }
}

# Find artists by genre
query {
  artistsByGenre(genre: ROCK) {
    stageName
    user {
      name
    }
  }
}

# Get music groups
query {
  musicGroups {
    id
    name
    genre
    founder {
      name
    }
    members {
      name
      surname
    }
  }
}

# Get available genres
query {
  availableGenres
}
Available Mutations
graphql
# Create user
mutation {
  createUser(
    name: "John"
    surname: "Doe"
    email: "john@example.com"
    password: "123456"
  ) {
    id
    name
    email
  }
}

# Create artist
mutation {
  createArtist(
    userId: 1
    stageName: "John Rocker"
    biography: "Rock music lover"
    genre: ROCK
  ) {
    id
    stageName
    genre
  }
}

# Create music group
mutation {
  createMusicGroup(
    name: "The Rockers"
    description: "Alternative rock group"
    genre: ROCK
    founderId: 1
  ) {
    id
    name
    founder {
      name
    }
  }
}

# Add member to group
mutation {
  addMemberToGroup(groupId: 1, userId: 2) {
    id
    members {
      name
    }
  }
}
🎨 Available Music Genres
Genre	Display Name
ROCK	Rock
POP	Pop
JAZZ	Jazz
CLASSICAL	Classical
HIP_HOP	Hip Hop
ELECTRONIC	Electronic
REGGAE	Reggae
BLUES	Blues
COUNTRY	Country
METAL	Metal
PUNK	Punk
SOUL	Soul
FUNK	Funk
LATIN	Latin
INDIE	Indie
🧪 Testing
Test with GraphiQL (Altair)
Install Altair GraphQL Client (Chrome extension)

Set URL: http://localhost:9000/graphql

Run your queries

Test with cURL
bash
# GraphQL query
curl -X POST http://localhost:9000/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "{ users { id name surname } }"}'

# REST endpoint
curl http://localhost:9000/user/id/1
🔒 Security
Database credentials are NOT in the repository

Use environment variables or application-dev.yml (ignored by git)

Template application-example.yml for configuration

🤝 Contributing
Contributions are welcome! Please:

Fork the project

Create your branch (git checkout -b feature/AmazingFeature)

Commit your changes (git commit -m 'Add AmazingFeature')

Push to the branch (git push origin feature/AmazingFeature)

Open a Pull Request

📄 License
This project is licensed under the MIT License. See the LICENSE file for details.

📧 Contact
Developer: Ronnie

GitHub: @RonniedeTal

⭐️ If you like this project, don't forget to give it a star!