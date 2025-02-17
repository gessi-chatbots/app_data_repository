# MApp-KG (Old Knowledge Graph Repository)

MApp-KG is a Java-based Spring Boot service that utilizes the RDF4J framework to interact with a **GraphDB repository instance**.

## 📌 Description

This component provides a **REST API** for querying, updating, and extracting knowledge from a graph database.

## 🛠️ Used Technologies

| Component   | Description                                                                           | Version |
|-------------|---------------------------------------------------------------------------------------|---------|
| **Spring Boot** | Java framework for creating REST APIs                                  | 2.7.1   |
| **RDF4J**       | Java library for manipulating RDF graphs                              | 3.0.0   | 
| **GraphDB**     | Enterprise-ready **Semantic Graph Database**, compliant with W3C standards | 10.1.0  |

## ⚙️ Configuration

To configure the **GraphDB connection**, set the appropriate values for:

- `db.url`
- `db.username`
- `db.password`

These values should be updated in the respective properties files:

- **Docker deployment**: `src/main/resources/application-gessi.properties`
- **Localhost deployment**: `src/main/resources/application-localhost.properties`

To set a custom **RML file path**, configure `rml.path` accordingly.

---

## 🚀 How to Build

To build the project, run:

```sh
mvn clean install package
```

## ▶️ How to Run

### **Run using Java**

```sh
java -jar target/repo-0.0.1-SNAPSHOT.jar
```

### **Deploy using Docker**

#### **Step 1: Build Docker Image**
```sh
docker build -t mapp-kg .
```

#### **Step 2: Run Docker Container**
```sh
docker run -d -p 3003:3003 --name MApp-KG \
  -e DB_USERNAME=my_db_user \
  -e DB_PASSWORD=my_db_password \
  -e REPO_NAME=my_repo_name \
  mapp-kg
```

---

## 🌍 How to Deploy (New Method)

### **Step 1: Pull Image from Repository**
```sh
docker pull mtiessler/kg_repository:latest
```

### **Step 2: Build Image (if needed)**
```sh
docker build -t mtiessler/kg_repository:latest .
```

### **Step 3: Create `.env` File**

Create a file named `kg_repository.env` in the same directory where you execute the Docker commands. This file should contain:

```
DB_USERNAME=username
DB_PASSWORD=password
```

---

## 🔗 API Documentation

The API of MApp-KG is available in the **Postman Collection**:  
[Postman Collection - MApp-KG](https://www.postman.com/gessi-fib-upc/gessi-nlp4se/collection/ak3s503/mapp-kg-old-app-repo?action=share&source=copy-link&creator=32448387)

---

