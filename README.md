# MApp-KG (Old Knowledge Graph Repository)

The *MApp-KG* is developed as a Java-based Spring Boot service using the RDF4J framework to build the hook with a GraphDB repository instance.

## Description

This software component provides an API for querying, updating, and extracting knowledge from a graph database.

## Used Technologies

| Component   | Description                                                                           | Version |
|-------------|---------------------------------------------------------------------------------------|---------|
| Spring Boot | Collection of Java libraries for creating REST APIs                                   | 2.7.1   |
| RDF4J       | Java library for manipulating RDF graphs                                              | 3.0.0   | 
| GraphDB     | GraphDB is an enterprise-ready Semantic Graph Database, compliant with W3C Standards. | 10.1.0  |

## How to Configure

Configure the GraphDB connection by setting the proper values for `db.url`, `db.username`, and `db.password` in `src/main/resources/application.properties` and `src/main/resources/application-gessi.properties` (if you use docker) or  and `src/main/resources/application-localhost.properties` (if you use localhost).

Configure the RML file path by setting the proper value for `rml.path` to use a custom RML file for schema integration.

## How to Build

To build the project, run the following command:

```sh
mvn clean install package
```

## How to Use

To run the service using Java from the generated package (`.jar`), run the following command:

```sh
java -jar target/repo-0.0.1-SNAPSHOT.jar
```

To deploy the service in a Docker container, follow these steps:

### Build Docker Image
```sh
docker build -t kg_repository .
```

### Run Docker Container
```sh
docker run -d -p 3003:3003 --name KG_Repository kg_repository
```

## How to Deploy (New Method)

### Step 1: Pull Image
```sh
docker pull mtiessler/kg_repository:latest
```

### Step 2: Build Image (if needed)
```sh
docker build -t mtiessler/kg_repository:latest .
```

### Step 3: Create `kg_repository.env` File
This file contains the credentials required to access the SPARQL database.
The `.env` file must be in the same directory where the commands are executed.

```
DB_USERNAME=username
DB_PASSWORD=password
```

## Features

The API of the MAPP-KG is available in the [Postman Collection](https://www.postman.com/gessi-fib-upc/gessi-nlp4se/collection/ak3s503/mapp-kg-old-app-repo?action=share&source=copy-link&creator=32448387)

### Main Data Import Methods:
- **Add Mobile Apps (JSON format)**: Store a list of mobile apps using a JSON Array of mobile apps as the body of the HTTP request. See the Swagger documentation for the schema.
- **Add Mobile Apps (RDF format)**: Store all triples within a given RDF file.
- **Add Mobile Apps (RML-based)**: Store all mobile apps extracted from a JSON file using a given RML mapping instance.

### Inductive Knowledge Generation:
- **Extract Features**: Send a `POST` request to `/derivedNLFeatures` with textual data (descriptions, summaries, changelogs, and/or reviews) to extract potential app features.
  - **Query parameters:**
    - `documentType`: Type of document to be processed (DESCRIPTION, SUMMARY, CHANGELOG, REVIEWS, USER_ANNOTATED, ALL).
    - `batch-size`: Number of documents processed at once.
    - `from`: Offset to start processing from the nth document.
    - (Optional) `maxSubj`: Subjectivity threshold (reviews above this won't be processed).
- **Feature Similarity Matching**: Send a `POST` request to `/computeFeatureSimilarity` to find and match synonyms between app features.
  - Accepts a `threshold` parameter (between 0 and 1, default is 0.5).
- **Undo Feature Synonymy**: Send a `DELETE` request to `/deleteFeatureSimilarities` to undo feature synonymy computed with `/computeFeatureSimilarity`.

## File Structure

- `src/main/java/upc/edu/gessi/repo`
  - **AppGraphRepoApplication.java**: Main class.
  - **Controller Package**: Handles HTTP requests.
    - `GraphDBController.java`: Logic for storing and retrieving data from the GraphDB repository.
    - `InductiveKnowledgeController.java`: Auxiliary repository handling extended knowledge generation.
  - **Domain Package**: Contains domain-specific entities.
  - **Service Package**: Business logic and database interaction.
    - `GraphDBService.java`: Main service containing methods for querying and updating the database.
    - `NLFeatureService.java`: Auxiliary service that communicates with a remote NL service for feature extraction.
  - **Utils Package**: Auxiliary functions.

## RDF Graph Example

You can find an RDF graph instance already populated with app info in [statements.zip](https://github.com/gessi-chatbots/app_data_repository/tree/master/data).

The data was originally obtained using the [App Data Scanner Service](https://github.com/gessi-chatbots/app_data_scanner_service).

App info includes:
- Package name
- Description
- Summary
- Changelog
- Reviews
- Annotated features
