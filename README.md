# Knowledge Graph Repository

The *KnowledgeGraphRepository* is developed as a Java-based Spring Boot service using the RDF4Jframework to build the hook with a GraphDB repository instance. 

## Description

This software component provides and API for querying, updating and extracting knowledge from a graph database. 

## File structure

- \src\main\java\upc.edu.gessi.repo
  - AppGraphRepoApplication.java: Main class and controller for the service.
  - \domain: this package contains entities for the domain.
  - \service: this package includes the services that build this application.
    - GraphDBService.java: main service. It contains methods for querying and updating the database.
    - NLFeatureService.java: auxiliary service that communicates with a remote NL service for feature extraction.
  - \utils: package with several auxiliary functions.

## Used technologies

| Component   | Description                                                                           | Version |
|-------------|---------------------------------------------------------------------------------------|---------|
| Spring Boot | Collection of java libraries for creating REST APIs                                   | 2.7.1   |
| RDF4J       | Java library for manipulating RDF graphs                                              | 3.0.0   | 
| GraphDB     | GraphDB is an enterprise ready Semantic Graph Database, compliant with W3C Standards. | 10.1.0  |



## Usage
Provide the DB Url in the application.properties field "db.url".

To launch it from IntelliJ, open AppGraphRepoApplication.java and click the Play button.

To deploy the service in a Docker container, run the following commands from project root:
1. docker build -t {image-name} .
2. docker run -d -p {port#}:{port#} {image-name}

## Features

The API of the App Data Repository is available here: http://localhost:8080/swagger-ui/. Below we provide a brief summarization of the main functionalities integrated in the last version of this service.

For now, this API accepts several requests:
- Send a GET request to /data?app_name to retrieve all the info saved for that app.
- Send a POST request to /insert with a json containing the app info you want to save.
- Send a GET request to /export (with a query parameter named "fileName") to export the contents of the DB to a file.
- Send a POST request to /updateRepository with a single JSON object with the attribute "url" to change the graph database endpoint.
- Send a POST request to /derivedNLFeatures to send textual data (i.e. descriptions, summaries, changelogs and/or reviews) through a natural language pipeline in order to extract potential app features. This requests needs the following query parameters:
  - documentType: the type of document to be processed. Possible values are: DESCRIPTION, SUMMARY, CHANGELOG, REVIEWS, USER_ANNOTATED and ALL.
  - batch-size: the number of documents to be processed at once.
  - from: offset. A value of n tells the service to start processing documents from the n-th onwards.
  - (optional) maxSubj: The subjectivity threshold. When processing reviews, all reviews above this threshold won't go through the NL pipeline.
- Send a POST request to /computeFeatureSimilarity to find and match synonyms between app features. This method accepts a "threshold" request parameter between 0 and 1. Default value is 0.5.
- Send a DELETE request to /deleteFeatureSimilarities to undo feature synonymy computed with /computeFeatureSimilarity.

## RDF graph example
You can find an RDF graph instance already populated with app info in [data/statements.zip](https://github.com/gessi-chatbots/app_data_repository/tree/master/data). The data was originally obtained using the https://github.com/gessi-chatbots/app_data_scanner_service service.
App info includes, among other info:

- Package name
- Description
- Summary
- Changelog
- Reviews
- Annotated features
