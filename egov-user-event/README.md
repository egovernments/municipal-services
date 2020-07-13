**mSeva Events and Notifications**


### Objective
- Create a common point to manage all the events generated for the user in the system

### Events Flow

/_create: API to create events in the system.

/_update: API to update events in the system.

/_search: API to search events in the system.

/notification/_count: API to fetch the count of total, unread, read notifications.

/lat/_update: API to update the last-login-time of the user. We store last-login-time of the user through this API thereby deciding which notifications have been read.




### Project Structure 
*Packages*
 - config - Contains all the configuration properties related to module
 - service - Consists of all services containing the business logic.
 - util - Contains utility functions and constatns.
 - repository - Fetch data from dependent micro services
 - repository/rowmapper - Rowmappers to convert db query results to object
 - repository/builder - Contains query builder for search
 - web/controllers - Controllers for the app.
 - web/models - POJO for the module.
 - consumer - Contains all the kafka consumers
 - producer - Contains kafka producer


### Resources
- Granular details about the API's can be found in the [swagger api definition](https://github.com/egovernments/municipal-services/blob/master/docs/user-events.yml)
- Postman collection for all the API's can be found in the [postman collection](https://www.getpostman.com/collections/14812d58dff5565bd3d9)


## Build & Run


    mvn clean install
    java -jar target/egov-user-event-1.0.0-SNAPSHOT.jar


## Dependencies


- Postgres database to store events.

- Location service to validate locality and set area code.

- ID Gen Module to generate unique events ID.

- Persister module for persistence.

- MDMS service to verify master data

- SMSNotification Service to send notifications related to registration and payment
