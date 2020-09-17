package org.galatea.starter.entrypoint.alphavantagestock.jsoncomponents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.galatea.starter.entrypoint.alphavantagestock.AlphaVantageStock;

public class TimesDaily{

  private JsonNode fullTimesDaily;
  private Map<String, String> datesAndStockClosePrice;
  private Integer currentDaysRequested;

  public TimesDaily(JsonNode fullJSONResponse, Integer numberOfDaysRequested) {
    this.fullTimesDaily = fullJSONResponse.get("Time Series (Daily)");
    this.currentDaysRequested = numberOfDaysRequested;
  }

  public JsonNode getTimesDaily() {
    return this.fullTimesDaily;
  }

  public Map getFilteredTimesDaily() throws IOException {
    return getFilteredMap();
  }

  private Map<String, String> getFilteredMap() {

    Iterator<Entry<String, JsonNode>> stockDates = this.fullTimesDaily.fields();
    Map<String, String> stockDateCloseMap = new TreeMap<>();

    for (int i = 0; i < this.currentDaysRequested; i++) {
      Entry<String, JsonNode> specificDate = stockDates.next();

      String fieldDate = specificDate.getKey();
      String stockPrice = this.fullTimesDaily.get(fieldDate).get("4. close").asText();

      stockDateCloseMap.put(fieldDate, stockPrice);
    }

    return stockDateCloseMap;

  }

}
