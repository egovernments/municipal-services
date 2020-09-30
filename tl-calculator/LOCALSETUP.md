# Local Setup

To setup the tl-calculator in your local system, clone the [Muncipal Service repository](https://github.com/egovernments/municipal-services).

## Dependencies

### Infra Dependency

- [X] Postgres DB
- [ ] Redis
- [X] Elastic search
- [X] Kafka
  - [ ] Consumer
  - [X] Producer

## Running Locally

To run the tl-services locally, you need to port forward below services locally

```bash
- kubectl -n egov port-forward <billing-service pod id> 8081:8080
- kubectl -n egov port-forward <tl-services pod id> 8082:8080
- kubectl -n egov port-forward <egov-mdms pod id> 8083:8080



To run the tl-calculator locally, update below listed properties in `application.properties` prior to running the project:

```ini
`egov.demand.minimum.payable.amount` : Specifies the Minimum amount to be paid
`egov.demand.businessserviceTL` : Business service code of TL. i.e `TL`
`egov.billingservice.host` : Billing service host if port forwarded using above command, value should be set to  http://localhost:8081
`egov.tradelicense.host` : Trade License service host if port forwarded using above command, value should be set to  http://localhost:8082
`egov.mdms.host` : MDMS service host if port forwarded using above command, value should be set to  http://localhost:8083

```

