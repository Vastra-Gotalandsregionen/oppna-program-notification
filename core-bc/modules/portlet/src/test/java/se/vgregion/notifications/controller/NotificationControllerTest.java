package se.vgregion.notifications.controller;

import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.ThemeDisplay;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;
import se.vgregion.alfrescoclient.domain.Site;
import se.vgregion.notifications.service.NotificationCallManager;
import se.vgregion.notifications.service.NotificationService;
import se.vgregion.raindancenotifier.domain.InvoiceNotification;
import se.vgregion.usdservice.domain.Issue;

import javax.portlet.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Patrik Bergström
 */
public class NotificationControllerTest {

    private NotificationController controller;
    private final static String SOME_SCREEN_NAME = "someScreenName";
    private final int SERVICES_RETURNED_COUNT = 2;
    private int numberOfServices = 0; // This number is used to verify several tests

    private <T extends PortletRequest> T setUp(Class<T> clazz) throws IOException, InterruptedException {
        NotificationService notificationService = mock(NotificationService.class);
        when(notificationService.getAlfrescoCount(anyString())).thenReturn(new AsyncResult<Integer>(SERVICES_RETURNED_COUNT));
        numberOfServices++;
        when(notificationService.getEmailCount(anyString())).thenReturn(new AsyncResult<Integer>(SERVICES_RETURNED_COUNT));
        numberOfServices++;
        when(notificationService.getInvoicesCount(anyString())).thenReturn(new AsyncResult<Integer>(SERVICES_RETURNED_COUNT));
        numberOfServices++;
        when(notificationService.getUsdIssuesCount(anyString())).thenReturn(new AsyncResult<Integer>(SERVICES_RETURNED_COUNT));
        numberOfServices++;
        when(notificationService.getMedControlCasesCount(anyString())).thenReturn(new AsyncResult<Integer>(null));
        numberOfServices++;
        when(notificationService.getSocialRequestCount(any(User.class))).thenReturn(new AsyncResult<Integer>(SERVICES_RETURNED_COUNT));
        numberOfServices++;

        when(notificationService.getCount(any(String.class), any(User.class))).thenCallRealMethod();

        if (controller == null) {
            controller = new NotificationController(notificationService, new NotificationCallManager());
        }

        T portletRequest = mock(clazz);

        User user = mock(User.class);
        ThemeDisplay themeDisplay = mock(ThemeDisplay.class);
        when(user.getScreenName()).thenReturn(SOME_SCREEN_NAME);
        when(themeDisplay.getUser()).thenReturn(user);
        when(portletRequest.getAttribute(WebKeys.THEME_DISPLAY)).thenReturn(themeDisplay);

        return portletRequest;
    }

    @Test
    public void testViewNotificationsEmptyCache() throws Exception {

        // Given
        RenderRequest renderRequest = setUp(RenderRequest.class);

        // New empty cache
        Cache cache = new Cache("asdf", 100, false, false, 10, 10);
        CacheManager manager = new CacheManager();
        manager.addCache(cache);
        ReflectionTestUtils.setField(controller, "cache", cache);

        // When
        Model model = mock(Model.class);
        controller.viewNotifications(model, renderRequest);

        // Then
        verify(model).addAttribute(eq("interval"), anyInt());
        verify(model, times(0)).addAttribute(eq("alfrescoCount"), anyInt());

        // We also test that the cache is updated after the scheduled update has run
        Thread.sleep(500);

        Map<String, Integer> cachedMap = (Map) cache.get(SOME_SCREEN_NAME).getValue();

        assertEquals(numberOfServices - 1, cachedMap.size()); // -1 since socialRequestsCount is not cached
    }

    @Test
    public void testViewNotificationsNotEmptyCache() throws Exception {

        // Given
        final int THE_COUNT = 234;

        RenderRequest renderRequest = setUp(RenderRequest.class);

        Cache cache = new Cache("asdf", 100, false, false, 30, 30);
        CacheManager manager = new CacheManager();
        manager.addCache(cache);

        Map<String, Integer> values = new HashMap<String, Integer>();
        values.put("alfrescoCount", THE_COUNT);
        values.put("emailCount", THE_COUNT);

        cache.put(new Element(SOME_SCREEN_NAME, values));
        ReflectionTestUtils.setField(controller, "cache", cache);

        // When
        Model model = mock(Model.class);
        controller.viewNotifications(model, renderRequest);

        // Then
        verify(model, times(3)).addAttribute(anyString(), eq(true)); // *HighlightCount will be added three times
        verify(model, times(2)).addAttribute(anyString(), eq(THE_COUNT)); // The respective counts will be added two times
    }

    @Test
    public void testPollNotificationsWithCachedItems() throws Exception {

        // Given
        final int THE_COUNT = 234;

        ResourceRequest resourceRequest = setUp(ResourceRequest.class);

        ResourceResponse response = mock(ResourceResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(response.getWriter()).thenReturn(new PrintWriter(out));

        Cache cache = new Cache("asdf", 100, false, false, 10, 10);
        CacheManager manager = new CacheManager();
        manager.addCache(cache);

        Map<String, Integer> values = new HashMap<String, Integer>();
        values.put("alfrescoCount", THE_COUNT);
        values.put("emailCount", THE_COUNT);

        cache.put(new Element(SOME_SCREEN_NAME, values));
        ReflectionTestUtils.setField(controller, "cache", cache);

        // When
        controller.pollNotifications(resourceRequest, response);

        // Then
        ObjectMapper objectMapper = new ObjectMapper();

        HashMap hashMap = objectMapper.readValue(out.toByteArray(), HashMap.class);

        assertEquals(3, hashMap.size()); // only the cached counts + socialRequestCount should be shown

    }

    @Test
    public void testPollNotificationsWithoutCachedItems() throws Exception {

        // Given
        ResourceRequest resourceRequest = setUp(ResourceRequest.class);
        when(resourceRequest.getParameter("onlyCache")).thenReturn("false");

        ResourceResponse response = mock(ResourceResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(response.getWriter()).thenReturn(new PrintWriter(out));

        Cache cache = new Cache("asdf", 100, false, false, 10, 10);
        CacheManager manager = new CacheManager();
        manager.addCache(cache);

        ReflectionTestUtils.setField(controller, "cache", cache);

        // When
        controller.pollNotifications(resourceRequest, response);

        // Then
        ObjectMapper objectMapper = new ObjectMapper();

        HashMap hashMap = objectMapper.readValue(out.toByteArray(), HashMap.class);

        assertEquals(numberOfServices, hashMap.size());

    }

    @Test
    public void testViewNotificationsNewValues() throws Exception {

        // Given
        RenderRequest renderRequest = setUp(RenderRequest.class);
        Cache cache = new Cache("asdf", 100, false, false, 10, 10);
        CacheManager manager = new CacheManager();
        manager.addCache(cache);

        Map<String, Integer> values = new HashMap<String, Integer>();
        values.put("alfrescoCount", 1);
        values.put("emailCount", 1);

        Set<String> recentlyChecked = new HashSet<String>(Arrays.asList("alfrescoCount", "emailCount"));

        cache.put(new Element(SOME_SCREEN_NAME, values));
        cache.put(new Element(SOME_SCREEN_NAME + "RecentlyChecked", recentlyChecked));
        ReflectionTestUtils.setField(controller, "cache", cache);

        Model model = mock(Model.class);

        // When
        controller.viewNotifications(model, renderRequest);

        // Then
        verify(model, times(1)).addAttribute(anyString(), eq(true)); // Both the alfresco and email count should not be highlighted since both are recently, but socialRequests make it one
        verify(model, times(numberOfServices - 1)).addAttribute(anyString(), eq(false));// All *HighlightCount, except socialRequestCount, are instead false so five here
    }

    @Test
    public void testShowExpandedNotifications() throws IOException, InterruptedException {

        // Given
        RenderRequest renderRequest = setUp(RenderRequest.class);
        when(renderRequest.getParameter("notificationType")).thenReturn("alfresco");

        Cache cache = new Cache("asdf", 100, false, false, 10, 10);
        CacheManager manager = new CacheManager();
        manager.addCache(cache);

        ReflectionTestUtils.setField(controller, "cache", cache);

        // When
        Model model = mock(Model.class);
        String view = controller.showExpandedNotifications(renderRequest, mock(RenderResponse.class), model);

        // Then
        assertEquals("view_alfresco", view);
        verify(model).addAttribute(eq("sites"), anyListOf(Site.class));

        // We test the other notificationTypes directly

        // Email
        when(renderRequest.getParameter("notificationType")).thenReturn("email");
        view = controller.showExpandedNotifications(renderRequest, mock(RenderResponse.class), model);
        assertEquals("view_email", view);

        // USD Issues
        when(renderRequest.getParameter("notificationType")).thenReturn("usdIssues");
        view = controller.showExpandedNotifications(renderRequest, mock(RenderResponse.class), model);
        assertEquals("view_usd_issues", view);
        verify(model).addAttribute(eq("myUsdIssues"), anyListOf(Issue.class));
        verify(model).addAttribute(eq("groupUsdIssues"), anyListOf(Issue.class));

        // Invoices
        when(renderRequest.getParameter("notificationType")).thenReturn("invoices");
        view = controller.showExpandedNotifications(renderRequest, mock(RenderResponse.class), model);
        assertEquals("view_invoices", view);
        verify(model).addAttribute(eq("invoices"), anyListOf(InvoiceNotification.class));

        // Verify the recently checked cache
        Element element = cache.get(SOME_SCREEN_NAME + "RecentlyChecked");
        Set value = (Set) element.getValue();
        assertEquals(4, value.size()); // We have recently checked four notification types
    }

    @Test
    public void testCacheUpdater() throws IOException, InterruptedException {

        // Given
        RenderRequest request = setUp(RenderRequest.class);

        Cache cache = new Cache("asdf", 100, false, false, 10, 10);
        CacheManager manager = new CacheManager();
        manager.addCache(cache);

        Map<String, Integer> values = new HashMap<String, Integer>();
        values.put("alfrescoCount", 1);
        values.put("emailCount", SERVICES_RETURNED_COUNT);

        Set<String> recentlyChecked = new HashSet<String>(Arrays.asList("alfrescoCount", "emailCount"));

        cache.put(new Element(SOME_SCREEN_NAME, values));
        cache.put(new Element(SOME_SCREEN_NAME + "RecentlyChecked", recentlyChecked));
        ReflectionTestUtils.setField(controller, "cache", cache);

        NotificationController.CacheUpdater updater = controller.new CacheUpdater(((ThemeDisplay) request
                .getAttribute(WebKeys.THEME_DISPLAY)).getUser());

        // When
        updater.run();

        // Then (since the cached alfrescoCount differs from what the alfresco service returns it should have been
        // removed as a recently checked value, but we will still have the emailCount as recently checked since that
        // value doesn't change)
        recentlyChecked = (Set<String>) cache.get(SOME_SCREEN_NAME + "RecentlyChecked").getValue();

        assertEquals(1, recentlyChecked.size());
        assertEquals("emailCount", recentlyChecked.iterator().next());
    }
}
