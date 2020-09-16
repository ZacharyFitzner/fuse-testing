package org.galatea.starter.entrypoint;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import junitparams.JUnitParamsRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.galatea.starter.ASpringTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;


@RequiredArgsConstructor
@Slf4j
// We don't load the entire spring application context for this test.
@WebMvcTest(AlphaVantageController.class)
// Use this runner since we want to parameterize certain tests.
// See runner's javadoc for more usage.
@RunWith(JUnitParamsRunner.class)
public class AlphaVantageControllerTest extends ASpringTest {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private RestTemplate restTemp;

  @Test
  public void testAlphaVantageEndpoint() throws Exception {

    Reader reader = Files.newBufferedReader(Paths.get("C:\\Users\\fitzn\\Desktop\\deleteBranch\\testFuse\\fuse-starter-java\\src\\test\\java\\org\\galatea\\starter\\entrypoint\\responseEntityExample.json"));
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode teslaJson = objectMapper.readTree(reader);

    ResponseEntity<JsonNode> teslaResp = new ResponseEntity(teslaJson, HttpStatus.OK);

    String stock = "TSLA";
    String stockLabel = "stock";

    final String alphaVantageUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&outputsize=full&symbol=";
    final String alphaAPI = "&apikey=randomapikey";

    given(this.restTemp.getForEntity(alphaVantageUrl + stock + alphaAPI, JsonNode.class)).willReturn(teslaResp);

    this.mvc.perform(
        get("/avp").param(stockLabel, stock).accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(jsonPath("$", is("test-value")));
  }
}
