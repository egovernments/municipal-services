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
- kubectl -n egov port-forward <tl-calculator-PODNAME> 8085:8080

Clone the [Business Service repository](https://github.com/egovernments/business-services).

- kubectl -n egov port-forward <billing-service-PODNAME> 8081:8080


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

