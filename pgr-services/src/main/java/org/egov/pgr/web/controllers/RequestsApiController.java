package org.egov.pgr.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import org.egov.pgr.web.models.RequestInfoWrapper;
import org.egov.pgr.web.models.RequestSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import javax.validation.constraints.*;
import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2020-07-15T11:35:33.568+05:30")

@Controller
@RequestMapping("/v2")
public class RequestsApiController{

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @Value("classpath:mockData.json")
    Resource resourceFile;

    @Autowired
    public RequestsApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }



    @RequestMapping(value="/requests/_create", method = RequestMethod.POST)
    public ResponseEntity<String> requestsCreatePost() throws IOException {
        try{
            URI uri = resourceFile.getURI();
            byte[] encoded = Files.readAllBytes(Paths.get(uri));
            String res = new String(encoded,  StandardCharsets.US_ASCII);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        catch (Exception e){
            e.printStackTrace();
            throw new CustomException("FILEPATH_ERROR","Failed to read file for mock data");
        }

    }

    @RequestMapping(value="/requests/_search", method = RequestMethod.POST)
    public ResponseEntity<String> requestsSearchPost(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                                     @Valid @ModelAttribute RequestSearchCriteria criteria) {
     try{
         URI uri = resourceFile.getURI();
         byte[] encoded = Files.readAllBytes(Paths.get(uri));
         String res = new String(encoded,  StandardCharsets.US_ASCII);
         return new ResponseEntity<>(res, HttpStatus.OK);
     }
     catch (Exception e){
         e.printStackTrace();
         throw new CustomException("FILEPATH_ERROR","Failed to read file for mock data");
     }

    }

    @RequestMapping(value="/requests/_update", method = RequestMethod.POST)
    public ResponseEntity<String> requestsUpdatePost() throws IOException {
        try{
            URI uri = resourceFile.getURI();
            byte[] encoded = Files.readAllBytes(Paths.get(uri));
            String res = new String(encoded,  StandardCharsets.US_ASCII);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        catch (Exception e){
            e.printStackTrace();
            throw new CustomException("FILEPATH_ERROR","Failed to read file for mock data");
        }
    }

}
