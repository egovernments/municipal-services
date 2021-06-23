/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2015>  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.noc.web.controller;

import java.util.List;

import javax.validation.Valid;

import org.egov.noc.config.ResponseInfoFactory;
import org.egov.noc.service.NOCService;
import org.egov.noc.web.model.Noc;
import org.egov.noc.web.model.NocRequest;
import org.egov.noc.web.model.NocResponse;
import org.egov.noc.web.model.NocSearchCriteria;
import org.egov.noc.web.model.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
	
@RestController
@RequestMapping("v1/noc")
public class NOCController {
	
	@Autowired
	private ResponseInfoFactory responseInfoFactory;
	
	@Autowired
	private NOCService nocService;

	@PostMapping(value = "/_create")
	public ResponseEntity<NocResponse> create(@Valid @RequestBody NocRequest nocRequest) {
		List<Noc> nocList = nocService.create(nocRequest);
		NocResponse response = NocResponse.builder().noc(nocList)
				.responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(nocRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@PostMapping(value = "/_update")
	public ResponseEntity<NocResponse> update(@Valid @RequestBody NocRequest nocRequest) {
		List<Noc> nocList = nocService.update(nocRequest);
		NocResponse response = NocResponse.builder().noc(nocList)
				.responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(nocRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@PostMapping(value = "/_search")
	public ResponseEntity<NocResponse> search(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @ModelAttribute NocSearchCriteria criteria) {

		List<Noc> nocList = nocService.search(criteria, requestInfoWrapper.getRequestInfo());

		NocResponse response = NocResponse.builder().noc(nocList).responseInfo(
				responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}