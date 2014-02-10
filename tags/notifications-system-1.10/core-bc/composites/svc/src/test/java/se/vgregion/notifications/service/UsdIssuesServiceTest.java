package se.vgregion.notifications.service;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import se.vgregion.usdservice.USDService;
import se.vgregion.usdservice.domain.Issue;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Patrik Bergström
 */
public class UsdIssuesServiceTest {

    private UsdIssuesService usdIssuesService;

    @Before
    public void setUp() throws Exception {
        USDService usdServiceMock = mock(USDService.class);
        Issue issue = new Issue();
        issue.setDescription("description");
        issue.setUrl("url://asdf");
        issue.setType("N");
        when(usdServiceMock.lookupIssues(anyString(), anyInt(), anyBoolean())).thenReturn(Arrays.asList(issue));
        usdIssuesService = new UsdIssuesService(usdServiceMock);
    }

    @Test
    public void testGetUsdIssuesJson() throws Exception {
        String usdIssuesJson = usdIssuesService.getUsdIssuesJson("asdf");
        ObjectMapper mapper = new ObjectMapper();
        List<Issue> issues = mapper.readValue(usdIssuesJson, new TypeReference<List<Issue>>() {});
        assertEquals(1, issues.size());
    }

    @Test
    public void testGetUsdIssues() throws Exception {
        List<Issue> issues = usdIssuesService.getUsdIssues("asdf", false);
        assertEquals(1, issues.size());
    }

    @Test
    public void testGetBopsId() throws Exception {
        when(usdIssuesService.getBopsId(anyString())).thenReturn("hej");
        String bopsId = usdIssuesService.getBopsId("asdf");
        assertEquals("hej", bopsId);
    }
}
