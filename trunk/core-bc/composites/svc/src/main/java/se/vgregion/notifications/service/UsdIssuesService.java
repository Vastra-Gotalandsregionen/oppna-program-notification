package se.vgregion.notifications.service;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.SerializationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.vgregion.usdservice.USDService;
import se.vgregion.usdservice.domain.Issue;

import java.io.IOException;
import java.util.List;

/**
 * @author Patrik Bergström
 */
public class UsdIssuesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsdIssuesService.class);

    private USDService usdService;

    @Autowired
    public UsdIssuesService(USDService usdService) {
        this.usdService = usdService;
    }


    public String getUsdIssuesJson(final String userId){

        List<Issue> issues = usdService.lookupIssues(userId, -1, true);

        ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();
        try {
            return writer.writeValueAsString(issues);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return "Internt fel.";
        }
    }
}
