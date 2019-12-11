package org.egov.bpa.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.egov.bpa.service.BPAService;
import org.egov.bpa.util.BPAUtil;
import org.egov.bpa.util.ResponseInfoFactory;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.BPAResponse;
import org.egov.bpa.web.models.BPASearchCriteria;
import org.egov.bpa.web.models.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/bpa")
public class BPAController {

	@Autowired
	private BPAService bpaService;
	
	@Autowired
	private BPAUtil bpaUtil;

	@Autowired
	private ResponseInfoFactory responseInfoFactory;

	@PostMapping(value = "/appl/_create")
	public ResponseEntity<BPAResponse> create(
			@Valid @RequestBody BPARequest bpaRequest) {
		bpaUtil.defaultJsonPathConfig();
		BPA bpa = bpaService.create(bpaRequest);
		List<BPA> bpas = new ArrayList<BPA>();
		bpas.add(bpa);
		BPAResponse response = BPAResponse
				.builder()
				.BPA(bpas )
				.responseInfo(
						responseInfoFactory.createResponseInfoFromRequestInfo(
								bpaRequest.getRequestInfo(), true)).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping(value = "/appl/_update")
	public ResponseEntity<BPAResponse> update(
			@Valid @RequestBody BPARequest bpaRequest) {
		BPA bpa = bpaService.update(bpaRequest);
		List<BPA> bpas = new ArrayList<BPA>();
		bpas.add(bpa);
		BPAResponse response = BPAResponse
				.builder()
				.BPA(bpas)
				.responseInfo(
						responseInfoFactory.createResponseInfoFromRequestInfo(
								bpaRequest.getRequestInfo(), true)).build();
		return new ResponseEntity<>(response, HttpStatus.OK);

	}
	
	
	@RequestMapping(value="/appl/_search", method = RequestMethod.POST)
    public ResponseEntity<BPAResponse> search(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                                       @Valid @ModelAttribute BPASearchCriteria criteria){

        List<BPA> bpas = bpaService.search(criteria,requestInfoWrapper.getRequestInfo());

        BPAResponse response = BPAResponse.builder().BPA(bpas).responseInfo(
                responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
