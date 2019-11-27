/*package org.egov.bpa.repository.querybuilder;

import org.egov.bpa.config.BpaConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

public class BPAQueryBuilder {
	
	@Autowired
    private BpaConfiguration config;

    @Autowired
    public bpaQueryBuilder(BpaConfiguration config) {
        this.config = config;
    }

    private static final String INNER_JOIN_STRING = " INNER JOIN ";
    private static final String LEFT_OUTER_JOIN_STRING = " LEFT OUTER JOIN ";
    
    private static final String QUERY = "SELECT bpa.*,bpaunit.*,bpaacc.*,bpaowner.*,"+"bpaaddress.*,bpa.id as bpa_id,bpa.tenantid as bpa_tenantId,bpa.lastModifiedTime as " +
            "bpa_lastModifiedTime,bpa.createdBy as bpa_createdBy,bpa.lastModifiedBy as bpa_lastModifiedBy,bpa.createdTime as " +
            "bpa_createdTime,,bpad.id as bpad_id,bpaaddress.id as bpa_ad_id,bpad.createdBy as bpad_createdBy," +
            "bpaowner.id as bpaowner_uuid,bpaowner.active as useractive,";	

    private static final String QUERY = "SELECT bpa.*,bpad.*,bpaunit.*,bpaacc.*,bpaowner.*," +
            "bpaaddress.*,bpaapldoc.*,bpaverdoc.*,bpaownerdoc.*,bpainsti.*,bpa.id as bpa_id,bpa.tenantid as bpa_tenantId,bpa.lastModifiedTime as " +
            "bpa_lastModifiedTime,bpa.createdBy as bpa_createdBy,bpa.lastModifiedBy as bpa_lastModifiedBy,bpa.createdTime as " +
            "bpa_createdTime,bpad.id as bpad_id,bpaaddress.id as bpa_ad_id,bpad.createdBy as bpad_createdBy," +
            "bpaowner.id as bpaowner_uuid,bpaowner.active as useractive," +
            "bpad.createdTime as bpad_createdTime,bpad.lastModifiedBy as bpad_lastModifiedBy,bpad.createdTime as " +
            "bpad_createdTime,bpaunit.id as bpa_un_id,bpaunit.buildingType as bpa_un_buildingType,bpaunit.uom as bpa_un_uom,bpaunit.active as bpa_un_active," +
            "bpaunit.uomvalue as bpa_un_uomvalue,bpaacc.id as bpa_acc_id,bpaacc.uom as bpa_acc_uom,bpaacc.uomvalue as bpa_acc_uomvalue,bpaacc.active as bpa_acc_active," +
            "bpaapldoc.id as bpa_ap_doc_id,bpaapldoc.documenttype as bpa_ap_doc_documenttype,bpaapldoc.filestoreid as bpa_ap_doc_filestoreid,bpaapldoc.active as bpa_ap_doc_active," +
            "bpaverdoc.id as bpa_ver_doc_id,bpaverdoc.documenttype as bpa_ver_doc_documenttype,bpaverdoc.filestoreid as bpa_ver_doc_filestoreid,bpaverdoc.active as bpa_ver_doc_active," +
            "bpaownerdoc.userid as docuserid,bpaownerdoc.buildingplanDetailId as docbuildingplandetailid,bpaownerdoc.id as ownerdocid,"+
            "bpaownerdoc.documenttype as ownerdocType,bpaownerdoc.filestoreid as ownerfileStoreId,bpaownerdoc.documentuid as ownerdocuid,bpaownerdoc.active as ownerdocactive," +
            " bpainsti.id as instiid,bpainsti.name as institutionname,bpainsti.type as institutiontype,bpainsti.tenantid as institenantId,bpainsti.active as instiactive "+
            " FROM eg_bpa_buildingplan bpa"
            +INNER_JOIN_STRING
            +"eg_bpa_buildingplandetail bpad ON bpad.buildingplanid = bpa.id"
            +INNER_JOIN_STRING
            +"eg_bpa_address bpaaddress ON bpaaddress.buildingplandetailid = bpad.id"
            +INNER_JOIN_STRING
            +"eg_bpa_owner bpaowner ON bpaowner.buildingplandetailid = bpad.id"
            +INNER_JOIN_STRING
            +"eg_bpa_buildingunit bpaunit ON bpaunit.buildingplandetailid = bpad.id"
            +LEFT_OUTER_JOIN_STRING
            +"eg_bpa_accessory bpaacc ON bpaacc.buildingplandetailid = bpad.id"
            +LEFT_OUTER_JOIN_STRING
            +"eg_bpa_document_owner bpaownerdoc ON bpaownerdoc.userid = bpaowner.id"
            +LEFT_OUTER_JOIN_STRING
            +"eg_bpa_applicationdocument bpaapldoc ON bpaapldoc.buildingplandetailid = bpad.id"
            +LEFT_OUTER_JOIN_STRING
            +"eg_bpa_verificationdocument bpaverdoc ON bpaverdoc.buildingplandetailid = bpad.id"
            +LEFT_OUTER_JOIN_STRING
            +"eg_bpa_institution bpainsti ON bpainsti.buildingplandetailid = bpad.id ";


}
*/