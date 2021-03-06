package se.vgregion.notifications.service;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.vgregion.usdservice.USDService;
import se.vgregion.usdservice.domain.Issue;

import java.io.IOException;
import java.util.List;

/**
 * @author Patrik Bergström
 */
@Service
class UsdIssuesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsdIssuesService.class);

    private USDService usdService;

    public UsdIssuesService() {
    }

    @Autowired
    public UsdIssuesService(USDService usdService) {
        this.usdService = usdService;
        initCxf();
    }

    private void initCxf() {
        // Init cxf bus to enable ssl/tls configuration.
        SpringBusFactory bf = new SpringBusFactory();
        Bus bus = bf.createBus("META-INF/spring/cxf-context.xml");
        bf.setDefaultBus(bus);
    }


    public String getUsdIssuesJson(String userId) {

        List<Issue> issues = usdService.lookupIssues(userId, -1, true);

        ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();
        try {
            return writer.writeValueAsString(issues);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return "Internt fel.";
        }
    }

    // The cachedResult is used by NotificationsCacheAspect
    public List<Issue> getUsdIssues(String userId, boolean cachedResult) {
        List<Issue> issues = usdService.lookupIssues(userId, -1, true);

        return issues;
    }

    public String getBopsId(String userId) {
        return usdService.getBopsId(userId);
    }
}
