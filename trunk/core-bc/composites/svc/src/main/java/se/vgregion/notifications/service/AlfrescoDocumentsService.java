package se.vgregion.notifications.service;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.vgregion.alfrescoclient.domain.Document;
import se.vgregion.alfrescoclient.domain.Site;
import se.vgregion.alfrescoclient.service.AlfrescoService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlfrescoDocumentsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlfrescoDocumentsService.class);

    private AlfrescoService alfrescoService;

    @Autowired
    public AlfrescoDocumentsService(AlfrescoService alfrescoService) {
        this.alfrescoService = alfrescoService;
    }

    public String getRecentlyModified(final String userId) {
        if (userId == null || "".equals(userId)) return "";

        List<Site> sitesByUser = alfrescoService.getSitesByUser(userId, "", "");

        Map<String, List<Document>> siteWithRecentlyModified = new HashMap<String, List<Document>>();
        for (Site site : sitesByUser) {
            List<Document> recentlyModified = alfrescoService.getRecentlyModified(userId, site.getShortName());
            siteWithRecentlyModified.put(site.getTitle(), recentlyModified);
        }
        ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();
        try {
            return writer.writeValueAsString(siteWithRecentlyModified);
        } catch (IOException e) {
            LOGGER.error("Failed to format object as JSON.", e);
            return "Internt fel.";
        }
    }

}
