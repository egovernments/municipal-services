# Local Setup

To setup the rainmaker-pgr in your local system, clone the [Muncipal Service repository](https://github.com/egovernments/municipal-services).

## Dependencies

### Infra Dependency

- [X] Postgres DB
- [ ] Redis
- [X] Elastic search
- [X] Kafka
  - [X] Consumer
  - [X] Producer

## Running Locally

To run the rainmaker-pgr locally, you need to port forward below services locally

```bash

Clone the Repo https://github.com/egovernments/core-services/tree/master/egov-mdms-service
`kubectl -n egov port-forward <egov-mdms-service pod id> 8094:8080`


Clone the Repo https://github.com/egovernments/core-services/tree/master/egov-localization
`kubectl -n egov port-forward <egov-localisation pod id> 8087:8080` 


Clone the Repo https://github.com/egovernments/core-services/tree/master/egov-persister
`kubectl -n egov port-forward <egov-persister pod id> 8082:8080` 


To run the th-services locally, update below listed properties in `application.properties` prior to running the project:

```ini
egov.usr.events.rate.code : Code used to show up in the event notifications to Rate the complaint.
egov.usr.events.reopen.code : Code used to show up in the event notifications to Reopen the complaint.
egov.usr.events.review.code : Code used to show up in the event notifications to Review the complaint.
egov.usr.events.review.link : The link set for user notifications Review action (/citizen/otpLogin?mobileNo=$mobile&redirectTo=complaint-details/$servicerequestid)
egov.usr.events.rate.link : The link set for user notifications Rate action (/citizen/otpLogin?mobileNo=$mobile&redirectTo=feedback/$servicerequestid)
egov.usr.events.reopen.link : The link set for user notifications Reopen action (/citizen/otpLogin?mobileNo=$mobile&redirectTo=reopen-complaint/$servicerequestid)
egov.usr.events.notification.enabled : Key to enable and disable event notifications
reassign.complaint.enabled : Key to nable notifications on re-assign.
reopen.complaint.enabled : Key to enable notifications on re-open.
comment.by.employee.notif.enabled : Key to enable notifications on comment.
notification.allowed.on.status : Fallback locale incase the client doesn’t send the locale.

```

