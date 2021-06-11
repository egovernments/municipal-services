package org.egov.pgr.utils;

import org.json.JSONObject;

import okhttp3.*;

public class DGRApi {

	public String apiCalling(String complaintId)
	{
		String status="";
	OkHttpClient client = new OkHttpClient().newBuilder()
			  .followRedirects(false)
			  .build();
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = RequestBody.create(mediaType, " {\"Complaint_Id\":\""+complaintId+"\", \r\n \"Remarks\":\"Resolved Succesffully\", \r\n \"Status\":\"resolved\"}");
			Request request = new Request.Builder()
			  .url("http://devgrievanceapi.psegs.in/api/grievance/GetComplaintStatus_PMIDC")
			  .method("POST", body)
			  .addHeader("Content-Type", "application/json")
			  .build();
			Response responses = null;
			try {
				responses = client.newCall(request).execute();
				String jsonData = responses.body().string();
				JSONObject Jobject = new JSONObject(jsonData);	
				if(Jobject.get("response").equals(1))
					status= "Successfully updated.";
				else
					status= "Something went be wrong!";
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
			return status;
	}

}
