# Local Setup

This document will walk you through the dependencies of this service and how to set it up locally

- To setup the fsm-calculator in your local system, clone the [municipal-services repository](https://github.com/egovernments/municipal-services).

## Dependencies

### Infra Dependency

- [] Postgres DB
- [ ] Redis
- [ ] Elasticsearch
- [X] Kafka
  - [] Consumer
  - [X] Producer

## Running Locally


To run the fsm locally, you need to port forward below services locally

```bash
billing-service
mdms-service
```

To run the fsm-calculator locally, update below listed properties in `application.properties` before running the project:

```ini
If any host value of the server for any external service pointing to local port can be changed to https://egov-micro-dev.egovernments.org
```
