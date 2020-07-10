

# eGov TL calculator



Module is used calculate in the system.

### License Flow
- Create
   - Tax is calculated by calling tl-calculator/v1/_calculate api.
   - The response contains the tax amount calculated by creating demand.


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
- Granular details about the API's can be found in the [swagger api definition](https://github.com/egovernments/egov-services/blob/master/docs/rainmaker/trade-license/tl-calculator.yml)
- Postman collection for all the API's can be found in the [postman collection](https://www.getpostman.com/collections/20ae4d9aaee9aff7ba07)


## Build & Run


    mvn clean install
    java -jar target/tl-calculator-1.1.1-SNAPSHOT.jar


## Dependencies


- Postgres database to fetch the Billing Slab data.

- Demand Service to create the demand and calculate the tax based on billing slabs.

- MDMS service to verify master data
