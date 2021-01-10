# Local Setup

To setup the water in your local system, clone the [Muncipal Service repository](https://github.com/egovernments/municipal-services).

## Dependencies

### Infra Dependency

- [x] Postgres DB
- [ ] Redis
- [ ] Elasticsearch
- [x] Kafka
  - [x] Consumer
  - [x] Producer

## Running Locally

To run the water in local system, you need to port forward below services.

```bash
 function kgpt(){kubectl get pods -n egov --selector=app=$1 --no-headers=true | head -n1 | awk '{print $1}'}
 kubectl port-forward -n egov $(kgpt property) 8084:8080 &
 kubectl port-forward -n egov $(kgpt mdms) 8085:8080 &
 kubectl port-forward -n egov $(kgpt idgen) 8086:8080 &
 kubectl port-forward -n egov $(kgpt water-calculator) 8087:8080 &
 kubectl port-forward -n egov $(kgpt billing-service) 8088:8080 &
 kubectl port-forward -n egov $(kgpt filestore) 8089:8080 &
 kubectl port-forward -n egov $(kgpt pdfgen) 8090:8080 &
 kubectl port-forward -n egov $(kgpt user) 8091:8080
``` 

Update below listed properties in `application.properties` before running the project:

```ini
egov.property.service.host = http://localhost:8084/
egov.mdms.host = http://localhost:8085/
egov.idgen.host = http://localhost:8086/
egov.ws.calculation.host = http://localhost:8087/
egov.billing.service.host=http://localhost:8088
egov.filestore.host=http://localhost:8089/
egov.pdfservice.host=http://localhost:8090/
egov.user.host=http://localhost:8091/
```