# Local Setup

To setup the egov-user-event in your local system, clone the [Muncipal Service repository](https://github.com/egovernments/core-services).

## Dependencies

### Infra Dependency

- [X] Postgres DB
- [ ] Redis
- [X] Elastic search
- [X] Kafka
  - [X] Consumer
  - [X] Producer

## Running Locally

To run the egov-user-event locally, you need to port forward below services locally

```bash

Clone the Repo https://github.com/egovernments/core-services/tree/master/egov-mdms-service
`kubectl -n egov port-forward <egov-mdms-service pod id> 8094:8080`


Clone the Repo https://github.com/egovernments/core-services/tree/master/egov-localization
`kubectl -n egov port-forward <egov-localisation pod id> 8087:8080` 


Clone the Repo https://github.com/egovernments/core-services/tree/master/egov-persister
`kubectl -n egov port-forward <egov-persister pod id> 8082:8080` 


To run the th-services locally, update below listed properties in `application.properties` prior to running the project:

```ini
mseva.notif.search.offset : Default pagination offset
mseva.notif.search.limit : Default pagination limit



```

