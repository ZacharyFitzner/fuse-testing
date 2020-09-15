package org.galatea.starter.entrypoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.aspect4log.Log;
import net.sf.aspect4log.Log.Level;
import org.apache.commons.collections4.IterableUtils;
import org.galatea.starter.entrypoint.Stock.MetaData;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.galatea.starter.entrypoint.Stock.Stock;
import java.util.Date;

/**
 * REST Controller that listens to http endpoints and allows the caller to send text to be
 * processed.
 */
@RequiredArgsConstructor
@Slf4j
@Log(enterLevel = Level.INFO, exitLevel = Level.INFO)
@RestController
public class AlphaVantageController extends BaseRestController {

  /**
   * Send the received text to the HalService to be processed and send the result out.
   */
  // @GetMapping to link http GET request to this method
  // @RequestParam to take a parameter from the url

  @GetMapping(value = "${webservice.alphavantagepath}", produces = {MediaType.APPLICATION_JSON_VALUE})
  public JsonNode alphaVantageEndpoint(
      @RequestParam(value = "stock") final String stock,
      @RequestParam(value = "days", defaultValue = "10") Integer numberOfDays) throws IOException{

    Stock stockResponse = getAlphaVantageResponse(stock).getBody();

    JsonNode returnedJSON = formatStockJSON(stockResponse.getTimeSeries(), numberOfDays, stockResponse, stock);

    log.info("{}", returnedJSON);

    return returnedJSON;

  }

  public ResponseEntity<Stock> getAlphaVantageResponse(String stock){

    //    Create REST call for Alphavantage
    RestTemplate restTemplate = new RestTemplate();
    final String alphaVantageUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&outputsize=full&symbol=";
    final String alphaAPI = "&apikey=randomapikey";

//    GET Instance (Actual Call) to Alphavantage with Stock Data
    ResponseEntity<Stock> stockObject
        = restTemplate.getForEntity(alphaVantageUrl + stock + alphaAPI, Stock.class);

    return stockObject;

  }

  private JsonNode formatStockJSON(JsonNode stockDataJSON, Integer numberOfDays, Stock stockResponse, String stock)
      throws IOException {

    //    Get all stock dates
    Iterator<Map.Entry<String, JsonNode>> dates = stockDataJSON.fields();
    Date currentDate = new Date();

    Map<String, String> stockDateTuple = new HashMap<>();
    String finalNumberOfDays = String.valueOf(numberOfDays);

    Map<String, String> information = new HashMap<String, String>() {{
      put("Symbol", stock);
      put("Number of Days", finalNumberOfDays);
      put("Date Queried", String.valueOf(currentDate));
    }};

//    Find Number of Stock Dates
    Integer datesLength = IterableUtils.size(stockDataJSON);

    if (numberOfDays > datesLength){
      numberOfDays = datesLength;
    }

//    Limit stock dates to numberOfDays
    for (int i = 0; i < numberOfDays; i++) {
      Map.Entry<String, JsonNode> date = dates.next();
      String   fieldName  = date.getKey();
      String stockPrice = stockDataJSON.get(fieldName).get("4. close").asText();
      stockDateTuple.put(fieldName, stockPrice);

    }

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode stockNode = mapper.valueToTree(stockResponse);

    String mapString = mapper.writeValueAsString(stockDateTuple);
    JsonNode mapNode = mapper.readTree(mapString);
    JsonNode informationNode = mapper.readTree(mapper.writeValueAsString(information));

    stockNode.remove("Time Series (Daily)");
    stockNode.remove("Meta Data");

    stockNode.set("Stock Information", informationNode);
    stockNode.set("Daily Close", mapNode);


    JsonNode result = mapper.createObjectNode().set("stock", stockNode);

    return result;
  }


}
