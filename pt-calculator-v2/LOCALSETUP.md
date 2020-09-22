# Local Setup

To setup the pt-calculator-v2 service in your local system, clone the [Muncipal Service repository](https://github.com/egovernments/municipal-services).

## Dependencies

### Infra Dependency

- [x] Postgres DB
- [ ] Redis
- [ ] Elasticsearch
- [x] Kafka
  - [x] Consumer
  - [x] Producer

## Running Locally

To run the pt-calculator-v2 in local system, you need to port forward below services.

```bash
 function kgpt(){kubectl get pods -n egov --selector=app=$1 --no-headers=true | head -n1 | awk '{print $1}'}
 kubectl port-forward -n egov $(kgpt property-service) 8084:8080 &
 kubectl port-forward -n egov $(kgpt egov-mdms-service) 8085:8080 &
 kubectl port-forward -n egov $(kgpt billing-service) 8086:8080 &
 kubectl port-forward -n egov $(kgpt collection-services) 8087:8080
``` 

Update below listed properties in `application.properties` before running the project:

```ini
egov.pt.registry.host = http://localhost:8084/
egov.assessmentservice.host = http://localhost:8084/
egov.mdms.host = http://localhost:8085/
egov.billing.service.host = http://localhost:8086
egov.collectionservice.host = http://localhost:8087/
pt.module.minpayable.amount = Specifies the Minimum amount to be paid for property assessment
pt.mutation.minpayable.amount = Specifies the Minimum amount to be paid for property mutation
pt.module.code = Business service code of PT. i.e `PT`
pt.mutation.fees.business.code = Business service code of Property mutation. i.e PT.MUTATION`
```