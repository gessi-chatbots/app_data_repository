# Knowledge Graph Repository

The *KnowledgeGraphRepository* is developed as a Java-based Spring Boot service using the RDF4Jframework to build the hook with a GraphDB repository instance. 

## Description

This software component provides and API for querying, updating and extracting knowledge from a graph database. 

## Used technologies

| Component   | Description                                                                           | Version |
|-------------|---------------------------------------------------------------------------------------|---------|
| Spring Boot | Collection of java libraries for creating REST APIs                                   | 2.7.1   |
| RDF4J       | Java library for manipulating RDF graphs                                              | 3.0.0   | 
| GraphDB     | GraphDB is an enterprise ready Semantic Graph Database, compliant with W3C Standards. | 10.1.0  |



## How to configure

Configure the GraphDB connection by setting the proper values for ```db.url```, ```db.username``` and ```db.password``` in ```src/main/resources/application.properties```.

Configure the RML file path by setting proper value for ```rml.path``` to use a custom RML file for schema integration.

## How to build

To build the project, run the following command:

```mvn clean install package```

## How to use

To run the service using Java from the generated package (.jar), run the following command:

```java -jar target/repo-0.0.1-SNAPSHOT.jar```

To deploy the service in a Docker container, run the following commands from project root:

```docker build -t {image-name}```
```docker run -d -p {port#}:{port#} {image-name}```

## Features

The API of the App Data Repository is available here: http://localhost:8080/swagger-ui/. Below we provide a brief summarization of the main functionalities integrated in the last version of this service.

Main methods for data import are listed below:

- **Add Mobile Apps (JSON format)**: Store a list of mobile apps using a JSON Array of mobile apps as body for the HTTP request. See the Swagger doc for the schema.
- **Add Mobile Apps (RDF format)**: Store all triplets withina given RDF file.
- **Add Mobile Apps (RML-based)**: Store all mobile apps extracted from a JSON file using a given RML mapping instance.

In addition, based on inductive knowledge generation techniques:

- Send a POST request to /derivedNLFeatures to send textual data (i.e. descriptions, summaries, changelogs and/or reviews) through a natural language pipeline in order to extract potential app features. This requests needs the following query parameters:
  - documentType: the type of document to be processed. Possible values are: DESCRIPTION, SUMMARY, CHANGELOG, REVIEWS, USER_ANNOTATED and ALL.
  - batch-size: the number of documents to be processed at once.
  - from: offset. A value of n tells the service to start processing documents from the n-th onwards.
  - (optional) maxSubj: The subjectivity threshold. When processing reviews, all reviews above this threshold won't go through the NL pipeline.
- Send a POST request to /computeFeatureSimilarity to find and match synonyms between app features. This method accepts a "threshold" request parameter between 0 and 1. Default value is 0.5.
- Send a DELETE request to /deleteFeatureSimilarities to undo feature synonymy computed with /computeFeatureSimilarity.

## File structure

- \src\main\java\upc.edu.gessi.repo
  - AppGraphRepoApplication.java: Main class.
  - \controller: this package contains the repositories for processing HTTP requests.
  	- GraphDBController.java: Logic for storing and retrieving data from the GraphDB repository.
    - InductiveKnowledgeController.java: auxiliary repository handling extended knowledge generation embedded into the system.
  - \domain: this package contains entities for the domain.
  - \service: this package includes the services that build this application.
    - GraphDBService.java: main service. It contains methods for querying and updating the database.
    - NLFeatureService.java: auxiliary service that communicates with a remote NL service for feature extraction.
  - \utils: package with several auxiliary functions.

## RDF graph example
You can find an RDF graph instance already populated with app info in [data/statements.zip](https://github.com/gessi-chatbots/app_data_repository/tree/master/data). The data was originally obtained using the https://github.com/gessi-chatbots/app_data_scanner_service service.
App info includes, among other info:

- Package name
- Description
- Summary
- Changelog
- Reviews
- Annotated features
