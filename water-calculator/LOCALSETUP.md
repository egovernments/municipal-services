# Local Setup

To setup the water-calculator in your local system, clone the [Muncipal Service repository](https://github.com/egovernments/municipal-services).

## Dependencies

### Infra Dependency

- [x] Postgres DB
- [ ] Redis
- [ ] Elasticsearch
- [x] Kafka
  - [x] Consumer
  - [x] Producer

## Running Locally

To run the water-calculator in local system, you need to port forward below services.

```bash
 function kgpt(){kubectl get pods -n egov --selector=app=$1 --no-headers=true | head -n1 | awk '{print $1}'}
 kubectl port-forward -n egov $(kgpt property) 8084:8080 &
 kubectl port-forward -n egov $(kgpt mdms) 8085:8080 &
 kubectl port-forward -n egov $(kgpt ws-servics) 8086:8080 &
 kubectl port-forward -n egov $(kgpt user) 8087:8080
 kubectl port-forward -n egov $(kgpt billing-service) 8088:8080
 kubectl port-forward -n egov $(kgpt workflow) 8089:8080
``` 

Update below listed properties in `application.properties` before running the project:

```ini
egov.property.service.host = http://localhost:8084/
egov.mdms.host = http://localhost:8085/
egov.ws.host = http://localhost:8086/
egov.user.host = http://localhost:8087/
egov.billingservice.host = http://localhost:8088/
workflow.workDir.path = http://localhost:8089/
```