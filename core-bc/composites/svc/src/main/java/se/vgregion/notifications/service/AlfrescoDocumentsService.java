package se.vgregion.notifications.service;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.vgregion.alfrescoclient.domain.Document;
import se.vgregion.alfrescoclient.domain.Site;
import se.vgregion.alfrescoclient.service.AlfrescoService;

import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

@Service
public class AlfrescoDocumentsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlfrescoDocumentsService.class);

    private AlfrescoService alfrescoService;
    
    @Value("${portlet-instance}")
    private String portletInstance;
    @Value("${csiframe-page}")
    private String csiframePage;

    public AlfrescoDocumentsService() {
        // Empty constructor is needed to make CGLIB happy
    }

    @Autowired
    public AlfrescoDocumentsService(AlfrescoService alfrescoService) {
        this.alfrescoService = alfrescoService;
    }

    public List<Site> getRecentlyModified(final String userId, boolean cachedResult) {
        if (userId == null || "".equals(userId)) {
            return new ArrayList<Site>();
        }

        List<Site> sitesByUser = alfrescoService.getSitesByUser(userId, csiframePage, portletInstance);

        for (Site site : sitesByUser) {
            List<Document> recentlyModified = alfrescoService.getRecentlyModified(userId, site.getShortName());

            site.setRecentModifiedDocuments(recentlyModified);
        }

        return sitesByUser;
    }

    public String getRecentlyModifiedJson(final String userId, boolean cachedResult) {
        List<Site> siteWithRecentlyModified = getRecentlyModified(userId, cachedResult);

        ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();
        try {
            return writer.writeValueAsString(siteWithRecentlyModified);
        } catch (IOException e) {
            LOGGER.error("Failed to format object as JSON.", e);
            return "Internt fel.";
        }
    }

}
