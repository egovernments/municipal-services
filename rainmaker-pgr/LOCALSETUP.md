## User event Service

### Requirements
- Prior Knowledge of Java/J2EE.
- Prior Knowledge of Spring Boot.
- Prior Knowledge of REST APIs and related concepts like path parameters, headers, JSON etc.
- Prior knowledge of Git.
- Advanced knowledge on how to operate JSON data would be an added advantage to understand the service.

### Local Setup

- To setup the rainmaker-pgr service, clone the [Municipal Services repository](https://github.com/egovernments/municipal-services). And in rainmaker-pgr service update the `application.properties`
- Replicate the dev db and create a database. 
- Run the kafka and zookeepr servers.
  - bin/zookeeper-server-start.sh config/zookeeper.properties
    bin/kafka-server-start.sh config/server.properties
- Run the application from PGRApp.java
