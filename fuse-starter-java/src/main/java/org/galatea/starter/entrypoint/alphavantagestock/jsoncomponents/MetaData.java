package org.galatea.starter.entrypoint.alphavantagestock.jsoncomponents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class MetaData{

  private JsonNode metaData;
  private String stockSymbol;
  private Integer currentDaysRequested;

  public MetaData(JsonNode fullJSONResponse, Integer numberOfDaysRequested) {
    this.metaData = fullJSONResponse.get("Meta Data");
    this.currentDaysRequested = numberOfDaysRequested;
    stockSymbol = this.metaData.get("2. Symbol").asText();
  }

  public JsonNode getMetaData() {
    return metaData;
  }

  public Map getFormattedMetaData() throws IOException {

    Map<String, String> stockMetaData = new HashMap<>() {{
      put("Symbol", stockSymbol);
      put("Number of Days", String.valueOf(currentDaysRequested));
    }};

    return stockMetaData;

  }
}
