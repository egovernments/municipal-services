
# bpa-calculator

bpa-calculator service present in municipal services provides multiple functionalities like calculating Application Fee, Sanction Fee, Low Permit Fee, OC Deviation Charges, generating demands for a particular BPA, BPA OC applications , updating demands

### DB UML Diagram



### Service Dependencies

- dcr-services ( get  Edcr data )

- mdms  ( to refer the calculation configuraton )

- billing ( generate and update demands )

- bpa-services (get the bpa application data for fee calculation )

### Swagger API Contract

NA

## Service Details

bpa-calculator service present in municipal services provides multiple functionalities like calculating Application Fee, Sanction Fee, Low Permit Fee, OC Deviation Charges, generating demands for a particular BPA, BPA OC applications , updating demands

### API Details
- Calculate : bpa-calculator/v1/_calculate end point used to calculate the Fee and create Demand with the applicable businessService and TaxHeads

### Kafka Consumers
NA

### Kafka Producers
- persister.save.bpa.calculation.topic=save-bpa-calculation
