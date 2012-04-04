package se.vgregion.notifications.service;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import se.vgregion.alfrescoclient.domain.Document;
import se.vgregion.alfrescoclient.domain.Site;
import se.vgregion.alfrescoclient.service.AlfrescoService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Patrik Bergström
 */
public class AlfrescoDocumentsServiceTest {

    private AlfrescoDocumentsService alfrescoDocumentsService;

    @Before
    public void setUp() throws Exception {
        List<Site> sites = createSitesWithDocuments();

        AlfrescoService alfrescoServiceMock = mock(AlfrescoService.class);
        when((alfrescoServiceMock.getSitesByUser(anyString(), anyString(), anyString()))).thenReturn(sites);

        Document document1 = new Document();
        document1.setTitle("title1");
        document1.setDescription("description1");
        document1.setModifiedOn("2000-01-01 12:12");
        Document document2 = new Document();
        document2.setTitle("title2");
        document2.setDescription("description2");
        document2.setModifiedOn("2000-01-01 12:12");
        Document document3 = new Document();
        document3.setTitle("title3");
        document3.setDescription("description3");
        document3.setModifiedOn("2000-01-01 12:12");

        when(alfrescoServiceMock.getRecentlyModified(anyString(), eq("shortName1"))).thenReturn(Arrays.asList(document1));
        when(alfrescoServiceMock.getRecentlyModified(anyString(), eq("shortName2"))).thenReturn(Arrays.asList(document2, document3));

        alfrescoDocumentsService = new AlfrescoDocumentsService(alfrescoServiceMock);
    }

    private List<Site> createSitesWithDocuments() {
        Site site1 = new Site();
        site1.setShortName("shortName1");

        Site site2 = new Site();
        site2.setShortName("shortName2");

        return Arrays.asList(site1, site2);
    }

    @Test
    public void testGetRecentlyModified() throws Exception {
        List<Site> recentlyModified = alfrescoDocumentsService.getRecentlyModified("userId", false);
        int number = 0;
        for (Site site : recentlyModified) {
            number += site.getRecentModifiedDocuments().size();
        }
        assertEquals(3, number); //document1 + document2 + document3
    }

    @Test
    public void testGetRecentlyModifiedJson() throws Exception {
        String recentlyModifiedJson = alfrescoDocumentsService.getRecentlyModifiedJson("userId", false);
        ObjectMapper mapper = new ObjectMapper();
        List<Site> list = new ArrayList<Site>();
        List<Site> recentlyModified = mapper.readValue(recentlyModifiedJson, new TypeReference<List<Site>>() {});
        int number = 0;
        for (Site site : recentlyModified) {
            number += site.getRecentModifiedDocuments().size();
        }
        assertEquals(3, number); //document1 + document2 + document3
    }

}
