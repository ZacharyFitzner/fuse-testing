package org.galatea.starter.entrypoint.alphavantagestock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.Json;
import io.swagger.models.auth.In;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.apache.poi.ss.formula.functions.T;
import org.galatea.starter.entrypoint.alphavantagestock.jsoncomponents.MetaData;
import org.galatea.starter.entrypoint.alphavantagestock.jsoncomponents.TimesDaily;

public class AlphaVantageStock {

  private MetaData metaData;
  private TimesDaily timesDaily;

  public AlphaVantageStock(JsonNode alphaVantageResponseInJSON, Integer numberOfDaysRequested) {
    this.metaData = new MetaData(alphaVantageResponseInJSON, numberOfDaysRequested);
    this.timesDaily = new TimesDaily(alphaVantageResponseInJSON, numberOfDaysRequested);
  }

  public JsonNode getMetaData() {
    return metaData.getMetaData();
  }

  public JsonNode getTimesDaily() {
    return timesDaily.getTimesDaily();
  }

  public Map getFormattedResponse() throws IOException {

    Map<String, Map> stockJsonMap = new TreeMap<>();

    stockJsonMap.put("Information", this.metaData.getFormattedMetaData());
    stockJsonMap.put("Daily Close", this.timesDaily.getFilteredTimesDaily());

    return stockJsonMap;

  }

}
