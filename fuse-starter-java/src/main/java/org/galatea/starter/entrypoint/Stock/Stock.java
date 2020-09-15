package org.galatea.starter.entrypoint.Stock;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import lombok.Data;
import org.galatea.starter.entrypoint.Stock.MetaData;
import java.util.Map;
import org.springframework.http.ResponseEntity;


@Data
public class Stock {

  @JsonProperty("Meta Data")
  private MetaData metadata;

  public MetaData getMetadata() {
    return metadata;
  }

  @JsonProperty("Time Series (Daily)")
  private JsonNode timeSeries;

  public JsonNode  getTimeSeries() {
    return timeSeries;
  }

  public void setTimeSeries(JsonNode timeSeries) {
    this.timeSeries = timeSeries;
  }

  public JsonNode responseToJSON() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode allJSONData = mapper.valueToTree(this);

    return allJSONData;
  }

  }



