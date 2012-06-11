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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
class AlfrescoDocumentsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlfrescoDocumentsService.class);

    private AlfrescoService alfrescoService;

    @Value("${portlet-instance}")
    private String portletInstance;
    @Value("${csiframe-page}")
    private String csiframePage;

    /**
     * Constructor.
     */
    public AlfrescoDocumentsService() {
        // Empty constructor is needed to make CGLIB happy
    }

    /**
     * Constructor.
     *
     * @param alfrescoService the {@link AlfrescoService}
     */
    @Autowired
    public AlfrescoDocumentsService(AlfrescoService alfrescoService) {
        this.alfrescoService = alfrescoService;
    }

    /**
     * Get recently modified {@link Document}s in a list of {@link Site}s. The {@link Document}s are accessible by
     * calling {@link se.vgregion.alfrescoclient.domain.Site#getRecentModifiedDocuments()}.
     *
     * @param userId       the user id
     * @param cachedResult whether cached results are allowed
     * @return a list of {@link Site}s containing the recently modified {@link Document}s
     */
    public List<Site> getRecentlyModified(final String userId, boolean cachedResult) {
        if (userId == null || "".equals(userId)) {
            return null;
        }

        List<Site> sitesByUser = alfrescoService.getSitesByUser(userId, csiframePage, portletInstance);

        for (Site site : sitesByUser) {
            List<Document> recentlyModified = alfrescoService.getRecentlyModified(userId, site.getShortName());

            site.setRecentModifiedDocuments(recentlyModified);
        }

        return sitesByUser;
    }

    /**
     * Like {@link AlfrescoDocumentsService#getRecentlyModified(java.lang.String, boolean)} except that the response
     * is returned as JSON. Also, caching is not implemented.
     *
     * @param userId the user id
     * @return a list of {@link Site}s containing the recently modified {@link Document}s as a JSON string
     */
    public String getRecentlyModifiedJson(final String userId) {
        // The caching aspect won't work here anyway, since we make a call within the same class, so set false
        List<Site> siteWithRecentlyModified = getRecentlyModified(userId, false);

        ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();
        try {
            return writer.writeValueAsString(siteWithRecentlyModified);
        } catch (IOException e) {
            LOGGER.error("Failed to format object as JSON.", e);
            return "Internt fel.";
        }
    }

}
