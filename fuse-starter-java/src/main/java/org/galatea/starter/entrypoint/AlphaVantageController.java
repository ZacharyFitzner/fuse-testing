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

  @Autowired
  public AlphaVantageController(RestTemplate restTemplate) {this.restTemplate = restTemplate;}

  /**
   * Send the received text to the HalService to be processed and send the result out.
   */
  // @GetMapping to link http GET request to this method
  // @RequestParam to take a parameter from the url

  @GetMapping(value = "${webservice.alphavantagepath}", produces = {MediaType.APPLICATION_JSON_VALUE})
  public JsonNode alphaVantageEndpoint(
      @RequestParam(value = "stock") final String stockSymbol,
      @RequestParam(value = "days", defaultValue = "10") Integer numberOfDaysRequested) throws IOException{

    JsonNode alphaVantageResponseInJson = getAlphaVantageResponse(stockSymbol);
    JsonNode formattedStockJSONResponse = formatStockJSON(getTimeSeriesDaily(alphaVantageResponseInJson), numberOfDaysRequested, stockSymbol);

    return formattedStockJSONResponse;

  }

  public JsonNode getAlphaVantageResponse(String stock){

    final String alphaVantageUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&outputsize=full&symbol=";
    final String alphaAPI = "&apikey=randomapikey";

//    GET Instance (Actual Call) to Alphavantage with Stock Data

    ResponseEntity<JsonNode> responseFromAlphaVantage = restTemplate.getForEntity(alphaVantageUrl + stock + alphaAPI, JsonNode.class);
    JsonNode alphaVantageResponseInJson = responseFromAlphaVantage.getBody();


    return alphaVantageResponseInJson;

  }

  public JsonNode getTimeSeriesDaily(JsonNode alphaVantageResponseInJson){

    JsonNode timeSeriesJSON = alphaVantageResponseInJson.get("Time Series (Daily)");
    assert timeSeriesJSON != null;

    return timeSeriesJSON;
  }


  private JsonNode formatStockJSON(JsonNode stockDataJSON, Integer numberOfDays, String stock)
      throws IOException {

    Map<String, String> filteredIntermediaryMapOfStocks = filterStockDataByNumberOfDays(stockDataJSON, numberOfDays);
    JsonNode formattedStockJSON = createMetaDataAndStockInfoJSON(filteredIntermediaryMapOfStocks, stock, numberOfDays);

    return formattedStockJSON;

  }


  private Map<String, String> filterStockDataByNumberOfDays(JsonNode stockDataJSON, Integer numberOfDays){

    numberOfDays = daysLargerThanJSONFilter(numberOfDays, stockDataJSON);

    Iterator<Map.Entry<String, JsonNode>> stockDates = stockDataJSON.fields();
    Map<String, String> stockDateCloseMap = new TreeMap<>();

    for (int i = 0; i < numberOfDays; i++) {
      Map.Entry<String, JsonNode> specificDate = stockDates.next();

      String   fieldDate  = specificDate.getKey();
      String stockPrice = stockDataJSON.get(fieldDate).get("4. close").asText();

      stockDateCloseMap.put(fieldDate, stockPrice);
    }

    return stockDateCloseMap;

  }


                private Integer daysLargerThanJSONFilter(Integer numberOfDaysRequested, JsonNode stockDataJSON){

                  Integer totalJSONLength = IterableUtils.size(stockDataJSON);

                  if (numberOfDaysRequested > totalJSONLength){
                    numberOfDaysRequested = totalJSONLength;
                  }

                  return numberOfDaysRequested;
                }



  private JsonNode createMetaDataAndStockInfoJSON(Map<String, String> stocksAndDatesHashMap, String stockSymbol, Integer numberOfDaysRequested) throws IOException {

    JsonNode stockAndDatesJSON = createStockAndDatesJSON(stocksAndDatesHashMap);
    JsonNode metaDataJSON = createMetaDataJSON(stockSymbol, numberOfDaysRequested);

    JsonNode formattedFullStockDataJsonFinal = mergeJsonNodes(stockAndDatesJSON, metaDataJSON);

    return formattedFullStockDataJsonFinal;

  }

              private JsonNode mergeJsonNodes(JsonNode stocksAndDatesInJSON, JsonNode metaDataInJSON)
                  throws IOException {

                Map<String, JsonNode> stockJsonMap = new TreeMap<>();

                stockJsonMap.put("Information", metaDataInJSON);
                stockJsonMap.put("Daily Close", stocksAndDatesInJSON);

                JsonNode formattedFullStockDataJsonFinal = mapToJsonHelper(stockJsonMap);

                return formattedFullStockDataJsonFinal;

              }


                                    private JsonNode createMetaDataJSON(String stockSymbol, Integer numberOfDaysRequested)
                                        throws IOException {

                                      Map<String, String> stockMetaData = new HashMap<>() {{
                                        put("Symbol", stockSymbol);
                                        put("Number of Days", String.valueOf(numberOfDaysRequested));
                                      }};

                                      JsonNode metaDataInJSON = mapToJsonHelper(stockMetaData);

                                      return metaDataInJSON;
                                    }


                                    private JsonNode createStockAndDatesJSON(Map<String, String> stocksAndDatesHashMap)
                                        throws IOException {

                                      JsonNode stocksAndDatesInJSON = mapToJsonHelper(stocksAndDatesHashMap);

                                      return stocksAndDatesInJSON;
                                    }


      private JsonNode mapToJsonHelper(Map mapInput) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        String mapString = mapper.writeValueAsString(mapInput);
        JsonNode jsonMapValue = mapper.readTree(mapString);

        return jsonMapValue;
      }


}

