package org.galatea.starter.entrypoint.Stock;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class MetaData {

    @JsonProperty(value = "1. Information", defaultValue = "!")
    private String information;

    @JsonProperty("2. Symbol")
    private String symbol;

}



