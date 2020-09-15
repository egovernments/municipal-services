# Local Setup

To setup the tl-services in your local system, clone the [Muncipal Service repository](https://github.com/egovernments/municipal-services).

## Dependencies

### Infra Dependency

- [X] Postgres DB
- [ ] Redis
- [X] Elastic search
- [X] Kafka
  - [X] Consumer
  - [X] Producer

## Running Locally

To run the th-services locally, you need to port forward below services locally

```bash
function kgpt(){kubectl get pods -n egov --selector=app=$1 --no-headers=true | head -n1 | awk '{print $1}'}
kubectl port-forward -n egov $(kgpt tl-calculator) 8087:8080 & 
kubectl port-forward -n egov $(kgpt billing-service) 8088:8080 &
```

To run the th-services locally, update below listed properties in `application.properties` prior to running the project:

```ini
egov.usr.events.pay.link : Link to redirect the user to pay screen
egov.usr.events.pay.code : The action on which the notification to be triggered
egov.usr.events.pay.triggers : The status on which the notification to be triggered
egov.user.event.notification.enabledForTL : Controls the enabling of TL system generated notifications
egov.user.event.notification.enabledForTLRenewal : Controls the enabling of TL Renewal system generated notifications
notification.sms.enabled.forTL : Controls the enabling of TL sms notifications
notification.sms.enabled.forTLRENEWAL : Controls the enabling of TL Renewal sms notifications
```
