
<<<<<<< HEAD
# noc-services

Module is used to apply NOC application for approval of respective noc users.


### DB UML Diagram



### Service Dependencies

- egov-user  ( Manage user )

- egov-idgen ( To generate the application No)

- egov-localization ( To use the localized messages )

- egov-location ( To store the address locality )

- egov-mdms ( Configurations/master data used in the application is served by MDMS )

- egov-notification-sms ( Service to send SMS to the users involved in the application )

- egov-persister ( Helps to persist the data  )

- egov-workflow-v2 ( Workflow configuration for different BPA application is configured )

### Swagger API Contract

 - [Swagger API](https://github.com/egovernments/municipal-services/blob/Noc-Contract/docs/noc/noc-v-1.0.0.yaml)


## Service Details

For every building plan service applied there is a need to get the No objection certificate from concerned departments. Based on the  configuration we have for the NOCs , for every application there will be a set of NOCs required. There should be a provision to allow the NOC department user to login to our system and upload the required NOC. We are providing a user to one NOC department. Based on the workflow mode(online/offline) of each noc type, the NOC department user can perform action. 

Online mode – NOC department user can login to system and approve/reject the application.

Offline mode – NOC application will be auto approved.

### API Details
- Create : NOC application with noc-services/v1/noc/_create api.
The response contains the NOC object with its assigned application number .
- Update : On created NOC multiple assessments can be done by calling the noc-services/v1/noc/_update api. Validations are carried out to verify the authenticity of the request and generate application fee which will be paid by the architect and gets approval number generated on approval .
- Search : NOC can be searched based on several search parameters by calling noc-services/v1/noc/_search.

### Kafka Consumers
- persister.save.noc.topic=save-noc-application
- persister.update.noc.topic=update-noc-application
- persister.update.noc.workflow.topic=update-noc-workflow


### Kafka Producers
- persister.save.noc.topic=save-noc-application
- persister.update.noc.topic=update-noc-application
- persister.update.noc.workflow.topic=update-noc-workflow
=======

# Noc Services



Module is used to apply NOC application for approval of respective noc users.

### Building Plan Approval Flow
- Create
   - NOC application with noc-services/v1/noc/_create api.
   - The response contains the NOC object with its assigned applicationNumber .
- Update
   -  On created NOC multiple assessments can be done by calling the noc-servcies/v1/noc/_update api.
    - Validations are carried out to verify the authenticity of the request and generate application fee which will be paid by the architect and gets approval number generated on approval .
- Search
   -  BPA can be searched based on several search parameters as detailed in the swagger yaml [[ Resources ](#resources)] .




### Project Structure 
*Packages*
 - config - Contains all the configuration properties related to module
 - service - Consists of all services containing the business logic.
 - util - Contains utility functions and constatns.
 - validator - Contains all validation code
 - repository - Fetch data from dependent micro services
 - repository/rowmapper - Rowmappers to convert db query results to object
 - repository/builder - Contains query builder for search
 - web/controllers - Controllers for the app.
 - web/models - POJO for the module.
 - consumer - Contains all the kafka consumers
 - producer - Contains kafka producer


### Resources
- Granular details about the API's can be found in the [swagger api definition](services.yaml)
- Postman collection for all the API's can be found in the [postman collection](https://www.getpostman.com/collections/a13920f8bb971c065e13)


## Build & Run


    mvn clean install
    java -jar target/noc-services-1.0.0-SNAPSHOT.jar


## Dependencies


- Postgres database to store property data.

- ID Gen Module to generate unique PropertyId and assessmentNumber.

- Persister module for persistence.


- MDMS service to verify master data

- User Service to create users of the property owners

- SMSNotification Service to send notifications related to registration and payment
>>>>>>> c1f03e96a4231f45783bae8d0a18f04d93b69c2b

