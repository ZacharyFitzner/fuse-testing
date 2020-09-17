package org.galatea.starter.entrypoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.aspect4log.Log;
import net.sf.aspect4log.Log.Level;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.galatea.starter.entrypoint.alphavantagestock.AlphaVantageStock;

/**
 * REST Controller that listens to http endpoints and allows the caller to send text to be
 * processed.
 */
@RequiredArgsConstructor
@Slf4j
@Log(enterLevel = Level.INFO, exitLevel = Level.INFO)
@RestController
public class AlphaVantageController extends BaseRestController {

  RestTemplate restTemplate;
  final String alphaVantageUrl =
      "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&outputsize=full&symbol=";
  final String alphaAPI = "&apikey=randomapikey";

  @Autowired
  public AlphaVantageController(RestTemplate restTemplate) {this.restTemplate = restTemplate;}

  /**
   * Send the received text to the HalService to be processed and send the result out.
   */
  // @GetMapping to link http GET request to this method
  // @RequestParam to take a parameter from the url
  @GetMapping(value = "${webservice.alphavantagepath}",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public JsonNode alphaVantageEndpoint(
      @RequestParam(value = "stock") final String stockSymbol,
      @RequestParam(value = "days", defaultValue = "10") Integer numberOfDaysRequested)
      throws IOException {

    //Get AlphaVantage Response from Queried Stock Symbol
    JsonNode alphaVantageResponseInJson = getAlphaVantageResponse(stockSymbol);

    //Store Data in AlphaVantageStock
    AlphaVantageStock queriedStock =
        new AlphaVantageStock(alphaVantageResponseInJson, numberOfDaysRequested);

    return queriedStock.getFormattedResponse();

  }

  public JsonNode getAlphaVantageResponse(String stock) {

    ResponseEntity<JsonNode> responseFromAlphaVantage =
        restTemplate.getForEntity(alphaVantageUrl + stock + alphaAPI, JsonNode.class);

    JsonNode alphaVantageResponseInJson = responseFromAlphaVantage.getBody();

    return alphaVantageResponseInJson;
  }

}