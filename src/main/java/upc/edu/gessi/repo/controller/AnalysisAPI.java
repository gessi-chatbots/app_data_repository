
package upc.edu.gessi.repo.controller;



import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import upc.edu.gessi.repo.dto.Analysis.ApplicationDayStatisticsDTO;
import upc.edu.gessi.repo.dto.Analysis.TopFeaturesDTO;
import upc.edu.gessi.repo.dto.Analysis.TopSentimentsDTO;
import upc.edu.gessi.repo.exception.MissingBodyException;

import java.util.Date;
import java.util.List;


@RequestMapping("/analysis")
public interface AnalysisAPI extends BaseAPI {

    @GetMapping(value = "/{appName}/statistics", produces = "application/json")
    @ResponseBody
    List<ApplicationDayStatisticsDTO> getApplicationStatistics(
            @PathVariable String appName,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(name = "startDate", defaultValue = "2020-01-01") Date startDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(name = "endDate", required = false) Date endDate);

    @PostMapping(value = "/top-sentiments", produces = "application/json")
    @ResponseBody
    TopSentimentsDTO getTopSentimentsByAppNames(@RequestBody List<String> appNames) throws MissingBodyException;

    @PostMapping(value = "/top-features", produces = "application/json")
    @ResponseBody
    TopFeaturesDTO getTopFeaturesByAppNames(@RequestBody List<String> appNames) throws MissingBodyException;

}
