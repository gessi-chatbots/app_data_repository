package upc.edu.gessi.repo.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upc.edu.gessi.repo.dto.Analysis.ApplicationDayStatisticsDTO;
import upc.edu.gessi.repo.dto.Analysis.TopDescriptorsDTO;
import upc.edu.gessi.repo.dto.Analysis.TopFeaturesDTO;
import upc.edu.gessi.repo.dto.Analysis.TopEmotionsDTO;
import upc.edu.gessi.repo.exception.MissingBodyException;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = "Analysis API")
@RequestMapping("/analysis")
public interface AnalysisAPI extends BaseAPI {

    @ApiOperation(value = "Get application statistics")
    @GetMapping(value = "/{appName}/statistics", produces = "application/json")
    @ResponseBody
    List<ApplicationDayStatisticsDTO> getApplicationStatistics(
            @ApiParam(value = "Name of the application") @PathVariable String appName,
            @ApiParam(value = "Start date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(name = "startDate", defaultValue = "2020-01-01") Date startDate,
            @ApiParam(value = "End date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(name = "endDate", required = false) Date endDate);

    @ApiOperation(value = "Get top sentiments by application names")
    @PostMapping(value = "/top-sentiments", produces = "application/json")
    @ResponseBody
    TopEmotionsDTO getTopSentimentsByAppNames(@RequestBody List<String> appNames) throws MissingBodyException;

    @ApiOperation(value = "Get top descriptors")
    @GetMapping(value = "/top-descriptors", produces = "application/json")
    @ResponseBody
    TopDescriptorsDTO getTopDescriptors();

    @ApiOperation(value = "Get top features")
    @GetMapping (value = "/top-features", produces = "application/json")
    @ResponseBody
    TopFeaturesDTO getTopFeatures();


    @ApiOperation(value = "Get top features by application names")
    @PostMapping(value = "/top-features-by-names", produces = "application/json")
    @ResponseBody
    TopFeaturesDTO getTopFeaturesByAppNames(@RequestBody List<String> appNames) throws MissingBodyException;

    @GetMapping("/excel")
    ResponseEntity<byte[]> generateAnalyticalExcel() throws IOException;
}