package org.egov.fsm.repository.querybuilder;

import org.springframework.stereotype.Component;

@Component
public class DataMartQueryBuilder {

	public static final String countQuery = "select tenantid, count(*) as count from eg_fsm_application";

	public static final String dataMartQuery = "SELECT fsm.id as applicationId,fsm.applicationStatus as fsmapplicationstatus,split_part(propertyusage::TEXT,'.', 1) as propertyType, split_part(propertyusage::TEXT,'.', 1) as propertySubType,fsm.sanitationType as sanitationType, fsmaddress.doorno as doorno, fsmaddress.street as streetName, fsmaddress.city as city, fsmaddress.pincode as pincode, fsmaddress.locality as locality, fsmaddress.district as district, fsmaddress.state as state, fsmaddress.slumname as slumname, fsm.source as applicationchannel, fsmdso.name as dsoname, fsmgeolocation.longitude as longitude, fsmgeolocation.latitude as latitude, fsmvehicle.registrationNumber as vehicleNumber, fsm.vehicleType as vehicleType, fsmvehicle.tankcapicity as vehicleCapacity, fsmvehicleTripdetail.volume as wasteCollected, fsmvehicleTrip.volumeCarried as wasteDumped, fsmvehicleTrip.tripstarttime as tripstarttime, fsmvehicleTrip.tripendtime as tripendtime, fsmpayment.totalamountpaid as paymentAmount, fsmpayment.paymentstatus as paymentStatus, fsmpayment.paymentmode as paymentsource, fsmpayment.paymentmode as instrumentType FROM eg_fsm_application as fsm JOIN eg_fsm_address as fsmaddress ON ( fsmaddress.fsm_id = fsm.id ) JOIN eg_fsm_geolocation as fsmgeolocation ON ( fsmaddress.id = fsmgeolocation.address_id ) LEFT JOIN eg_vendor as fsmdso ON ( fsmdso.id = fsm.dso_id) LEFT JOIN eg_vendor_vehicle as vendorvehicle ON ( vendorvehicle.vendor_id = fsm.dso_id) LEFT JOIN eg_vehicle as fsmvehicle ON ( fsmvehicle.id = vendorvehicle.vechile_id) LEFT JOIN eg_vehicle_trip_detail as fsmvehicleTripdetail ON ( fsmvehicleTripdetail.referenceNo = fsm.applicationNo) LEFT JOIN eg_vehicle_trip as fsmvehicleTrip ON ( fsmvehicleTripdetail.id = fsmvehicleTripdetail.trip_id) LEFT JOIN egcl_bill as egbill ON ( egbill.consumercode =fsm.applicationno) LEFT JOIN egcl_paymentdetail as paymentdl ON ( paymentdl.billid = egbill.id ) LEFT JOIN egcl_payment as fsmpayment ON ( fsmpayment.id=paymentdl.paymentid)";

}
