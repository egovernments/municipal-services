package org.egov.pgr.web.controllers;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.egov.pgr.util.HRMSUtil;
import org.egov.pgr.web.models.RequestInfoWrapper;
import org.egov.pgr.web.models.RequestSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2020-07-15T11:35:33.568+05:30")

@Controller
@RequestMapping("/mock")
@Slf4j
public class MockController {

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    private ResourceLoader resourceLoader;

    private HRMSUtil hrmsUtil;

    @Autowired
    public MockController(ObjectMapper objectMapper, HttpServletRequest request, ResourceLoader resourceLoader, HRMSUtil hrmsUtil) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.resourceLoader = resourceLoader;
        this.hrmsUtil = hrmsUtil;
    }




    @RequestMapping(value = "/requests/_create", method = RequestMethod.POST)
    public ResponseEntity<String> requestsCreatePost() throws IOException {
        try {
            Resource resource = resourceLoader.getResource("classpath:mockData.json");
            InputStream mockDataFile = resource.getInputStream();
            log.info("mock file: " + mockDataFile.toString());
            String res = IOUtils.toString(mockDataFile, StandardCharsets.UTF_8.name());
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (CustomException e) {
            throw new CustomException("FILEPATH_ERROR", "Failed to read file for mock data");
        }

    }

    @RequestMapping(value = "/requests/_search", method = RequestMethod.POST)
    public ResponseEntity<String> requestsSearchPost(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                                     @Valid @ModelAttribute RequestSearchCriteria criteria) {
        try {
            Resource resource = resourceLoader.getResource("classpath:mockData.json");
            InputStream mockDataFile = resource.getInputStream();
            log.info("mock file: " + mockDataFile.toString());
            String res = IOUtils.toString(mockDataFile, StandardCharsets.UTF_8.name());
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (CustomException | IOException e) {
            throw new CustomException("FILEPATH_ERROR", "Failed to read file for mock data");
        }

    }

    @RequestMapping(value = "/requests/_update", method = RequestMethod.POST)
	public ResponseEntity<String> requestsUpdatePost() throws IOException {

		Resource resource = resourceLoader.getResource("classpath:mockData.json");
		InputStream mockDataFile = resource.getInputStream();
		log.info("mock file: " + mockDataFile.toString());
		String res = IOUtils.toString(mockDataFile, StandardCharsets.UTF_8.name());
		return new ResponseEntity<>(res, HttpStatus.OK);

	}


    @RequestMapping(value = "/requests/_test", method = RequestMethod.POST)
    public ResponseEntity<List<String>> requestsTest(@RequestBody RequestInfoWrapper requestInfoWrapper,
                                               @RequestParam String tenantId, @RequestParam List<String> uuids) {

        List<String> department = hrmsUtil.getDepartment(tenantId, uuids, requestInfoWrapper.getRequestInfo());

        return  new ResponseEntity<>(department, HttpStatus.OK);
    }
}
