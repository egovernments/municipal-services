package org.egov.fsm.repository.querybuilder;

import org.springframework.stereotype.Component;

@Component
public class DataMartQueryBuilder {
	
	public static final String countQuery="select count(*) from eg_fsm_application";
	
	
	
	
	public static final String dataMartQuery="SELECT\\r\\n\"\r\n"
			+ "			+ \"fsm.id as applicationId,\\r\\n\"\r\n"
			+ "			+ \"fsm.applicationStatus as fsmapplicationstatus,\\r\\n\"\r\n"
			+ "			+ \"split_part(propertyusage::TEXT,'.', 1) as propertyType,\\r\\n\"\r\n"
			+ "			+ \"split_part(propertyusage::TEXT,'.', 1) as propertySubType,\\r\\n\"\r\n"
			+ "			+ \"fsm.sanitationType as sanitationType,\\r\\n\"\r\n"
			+ "			+ \"fsmaddress.doorno as doorno,\\r\\n\"\r\n"
			+ "			+ \"fsmaddress.street as streetName,\\r\\n\"\r\n"
			+ "			+ \"fsmaddress.city as city,\\r\\n\"\r\n"
			+ "			+ \"fsmaddress.pincode as pincode,\\r\\n\"\r\n"
			+ "			+ \"fsmaddress.locality as locality,\\r\\n\"\r\n"
			+ "			+ \"fsmaddress.district as district,\\r\\n\"\r\n"
			+ "			+ \"fsmaddress.state as state,\\r\\n\"\r\n"
			+ "			+ \"fsmaddress.slumname as slumname,\\r\\n\"\r\n"
			+ "			+ \"fsm.source as applicationchannel,\\r\\n\"\r\n"
			+ "			+ \"fsmdso.name as dsoname,\\r\\n\"\r\n"
			+ "			+ \"fsmgeolocation.longitude as longitude,\\r\\n\"\r\n"
			+ "			+ \"fsmgeolocation.latitude as latitude,\\r\\n\"\r\n"
			+ "			+ \"fsmvehicle.registrationNumber as vehicleNumber,\\r\\n\"\r\n"
			+ "			+ \"fsm.vehicleType as vehicleType,\\r\\n\"\r\n"
			+ "			+ \"fsmvehicle.tankcapicity as vehicleCapacity,\\r\\n\"\r\n"
			+ "			+ \"fsmvehicleTripdetail.volume as wasteCollected,\\r\\n\"\r\n"
			+ "			+ \"fsmvehicleTrip.volumeCarried as wasteDumped,\\r\\n\"\r\n"
			+ "			+ \"fsmvehicleTrip.tripstarttime as tripstarttime,\\r\\n\"\r\n"
			+ "			+ \"fsmvehicleTrip.tripendtime as tripendtime,\\r\\n\"\r\n"
			+ "			+ \"fsmpayment.totalamountpaid as paymentAmount,\\r\\n\"\r\n"
			+ "			+ \"fsmpayment.paymentstatus as paymentStatus,\\r\\n\"\r\n"
			+ "			+ \"fsmpayment.paymentmode as paymentsource,\\r\\n\"\r\n"
			+ "			+ \"fsmpayment.paymentmode as instrumentType\\r\\n\"\r\n"
			+ "			+ \"FROM eg_fsm_application as fsm\\r\\n\"\r\n"
			+ "			+ \"JOIN eg_fsm_address as fsmaddress ON ( fsmaddress.fsm_id = fsm.id )\\r\\n\"\r\n"
			+ "			+ \"JOIN eg_fsm_geolocation as fsmgeolocation ON ( fsmaddress.id = fsmgeolocation.address_id )\\r\\n\"\r\n"
			+ "			+ \"LEFT JOIN eg_vendor as fsmdso ON ( fsmdso.id = fsm.dso_id)\\r\\n\"\r\n"
			+ "			+ \"LEFT JOIN eg_vendor_vehicle as vendorvehicle ON ( vendorvehicle.vendor_id = fsm.dso_id)\\r\\n\"\r\n"
			+ "			+ \"LEFT JOIN eg_vehicle as fsmvehicle ON ( fsmvehicle.id = vendorvehicle.vechile_id)\\r\\n\"\r\n"
			+ "			+ \"LEFT JOIN eg_vehicle_trip_detail as fsmvehicleTripdetail ON ( fsmvehicleTripdetail.referenceNo = fsm.applicationNo)\\r\\n\"\r\n"
			+ "			+ \"LEFT JOIN eg_vehicle_trip as fsmvehicleTrip ON ( fsmvehicleTripdetail.id = fsmvehicleTripdetail.trip_id)\\r\\n\"\r\n"
			+ "			+ \"LEFT JOIN egcl_bill as egbill ON ( egbill.consumercode =fsm.applicationno)\\r\\n\"\r\n"
			+ "			+ \"LEFT JOIN egcl_paymentdetail as paymentdl ON ( paymentdl.billid = egbill.id )\\r\\n\"\r\n"
			+ "			+ \"LEFT JOIN egcl_payment as fsmpayment ON ( fsmpayment.id=paymentdl.paymentid)\"";

}
