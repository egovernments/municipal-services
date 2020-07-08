

# eGov Trade License



Module is used to register Trade License in the system.

### License Flow
- Create
   - License is created by calling tl-services/v1/_create api which carries out validations based on the MDMS data of that particular city.
   - The response contains the License object with its assigned LicenseNumber, applicationNumber and the calculation of that License.
- Update
   -  On created License one can perform workflow actions by calling tl-services/v1/_update api.
    - Validations are carried out to verify the authenticity of the request and any modified data to the License.
- Search
   -  Trade License can be searched based on several search parameters using /tl-services/v1/_search API.




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
- Granular details about the API's can be found in the [swagger api definition](https://github.com/egovernments/egov-services/blob/master/docs/rainmaker/trade-license/tl-service.yml)
- Postman collection for all the API's can be found in the [postman collection](https://www.getpostman.com/collections/3438e6c40feadcdf4641)


## Build & Run


    mvn clean install
    java -jar target/tl-services-1.1.0-SNAPSHOT.jar


## Dependencies


- Postgres database to store Trade License data.

- Location service to validate locality and set area code.

- ID Gen Module to generate unique LicenseNumber and applicationNumber.

- Persister module for persistence.

- TL calculator module to calculate tax for the given License.

- MDMS service to verify master data

- User Service to create users of the License owners

- SMSNotification Service to send notifications related to registration and payment
