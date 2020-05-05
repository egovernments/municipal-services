package org.egov.bpa.web.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Validated
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BPA {
	
	@JsonIgnore
	private ArrayList<String> docIds ;
	
	
	 @JsonProperty("id")
	  private String id;

	  @JsonProperty("applicationNo")
	  private String applicationNo;

	  @JsonProperty("approvalNo")
	  private String approvalNo;

	  @JsonProperty("accountId")
	  private String accountId;

	  @JsonProperty("edcrNumber")
	  private String edcrNumber;

	  @JsonProperty("riskType")
	  private String riskType;
	  
	  @JsonProperty("landId")
	  private String landId;

	  @NotNull
	  @JsonProperty("tenantId")
	  private String tenantId;

	  /**
	   * status of the application.
	   */
	  @JsonAdapter(StatusEnum.Adapter.class)
	  public enum StatusEnum {
	    ACTIVE("ACTIVE"),
	    INACTIVE("INACTIVE"),
	    INPROGRESS("INPROGRESS"),
	    APPROVED("APPROVED"),
	    REJECTED("REJECTED"),
	    CANCELED("CANCELED");

	    private String value;

	    StatusEnum(String value) {
	      this.value = value;
	    }
	    public String getValue() {
	      return value;
	    }

	    @Override
	    public String toString() {
	      return String.valueOf(value);
	    }
	    public static StatusEnum fromValue(String text) {
	      for (StatusEnum b : StatusEnum.values()) {
	        if (String.valueOf(b.value).equals(text)) {
	          return b;
	        }
	      }
	      return null;
	    }
	    public static class Adapter extends TypeAdapter<StatusEnum> {
	      @Override
	      public void write(final JsonWriter jsonWriter, final StatusEnum enumeration) throws IOException {
	        jsonWriter.value(enumeration.getValue());
	      }

	      @Override
	      public StatusEnum read(final JsonReader jsonReader) throws IOException {
	        String value = jsonReader.nextString();
	        return StatusEnum.fromValue(String.valueOf(value));
	      }
	    }
	  }  
	  
	  @JsonProperty("status")
	  private StatusEnum status;

	  @JsonProperty("documents")
	  private List<Document> documents;

	  @JsonProperty("landInfo")
	  private LandInfo landInfo;

	  @JsonProperty("workflow")
	  private Workflow workflow;
	  
	  @JsonProperty("auditDetails")
	  private AuditDetails auditDetails;
	  
	  @JsonProperty("additionalDetails")
	  private Object additionalDetails;
	  
		

		public BPA addDocumentsItem(Document documentsItem) {
			if (this.documents == null) {
				this.documents = new ArrayList<>();
			}
			if(this.docIds == null){
				this.docIds = new ArrayList<String>();
			}
			
			if(!this.docIds.contains(documentsItem.getId())){
				this.documents.add(documentsItem);
				this.docIds.add(documentsItem.getId());
			}
				
			return this;
		}
		
}
