package se.vgregion.notifications.service;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.helpers.NOPLogger;
import org.springframework.test.util.ReflectionTestUtils;
import se.vgregion.notifications.domain.CountResult;
import se.vgregion.notifications.domain.NotificationServiceName;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Patrik Bergström
 */
public class NotificationCallManagerTest {

    private NotificationCallManager notificationCallManager = new NotificationCallManager();

    @Test
    public void testInit() throws Exception {
        // Given
        ReflectionTestUtils.setField(notificationCallManager, "bannedServicesString", "service1, service2");

        // When
        notificationCallManager.init();

        // Then
        Set<String> banList = notificationCallManager.getBanList();
        assertEquals(2, banList.size());
        assertTrue(banList.contains("service1"));
        assertTrue(banList.contains("service2"));

    }

    @Test
    public void testNotifyValue_NullValue() throws Exception {

        // When
        notificationCallManager.notifyValue("service1", null, "name1");

        // Then
        boolean b = notificationCallManager.shouldICallThisService("service1", "name1");
        assertFalse(b);

        Map<String,Integer> consecutiveNullsMap = notificationCallManager.getConsecutiveNullsMap();

        assertEquals(1, consecutiveNullsMap.size());
        Map.Entry<String, Integer> entry = consecutiveNullsMap.entrySet().iterator().next();
        assertEquals("service1_name1", entry.getKey());
        assertEquals(1, (int) entry.getValue());
    }

    @Test
    public void testNotifyValue_NonNullValue() throws Exception {

        // When
        notificationCallManager.notifyValue("service1", CountResult.createWithCount(3), "name1");

        // Then
        boolean b = notificationCallManager.shouldICallThisService("service1", "name1");
        assertTrue(b);
    }

    @Test
    public void testNotifyValue_QueueSizeLimit() throws Exception {

        // So we don't need to log 10000 rows in the console
        ReflectionTestUtils.setField(notificationCallManager, "logger", NOPLogger.NOP_LOGGER);

        // When
        // First two which we decide
        notificationCallManager.notifyValue("service1", null, "name1");
        notificationCallManager.notifyValue("service1", null, "name2");
        notificationCallManager.notifyValue("service1", null, "name2"); // This should not result in an extra entry, only add to the previous entry

        // Then 9998 random
        for (int i = 0; i < 9998; i++) {
            notificationCallManager.notifyValue("service1", null, UUID.randomUUID().toString());
        }

        // Since the limit is 10000 the two first should still be present
        assertTrue(notificationCallManager.getConsecutiveNullsMap().containsKey("service1_name1"));
        assertTrue(notificationCallManager.getConsecutiveNullsMap().containsKey("service1_name2"));

        // But if we add just two more the two first entries should not present anymore
        notificationCallManager.notifyValue("service1", null, UUID.randomUUID().toString());
        notificationCallManager.notifyValue("service1", null, UUID.randomUUID().toString());
        assertFalse(notificationCallManager.getConsecutiveNullsMap().containsKey("service1_name1"));
        assertFalse(notificationCallManager.getConsecutiveNullsMap().containsKey("service1_name2"));

        // Verify the size of these are 10000
        int expected = 10000;
        assertEquals(expected, notificationCallManager.getConsecutiveNullsMap().size());
        assertEquals(expected, notificationCallManager.getCurrentKeysInCache().size());
        assertEquals(expected, notificationCallManager.getEarliestAllowedDates().size());

    }

    @Test
    public void testShouldICallThisService_BannedService() throws Exception {

        // Given
        ReflectionTestUtils.setField(notificationCallManager, "bannedServices", new HashSet<String>(Arrays.asList(
                "bannedService")));

        // When
        boolean b = notificationCallManager.shouldICallThisService("bannedService", "anyName");
        assertFalse(b);
    }

    @Test
    public void testShouldICallThisService_NotYetDue() throws Exception {

        // Given
        HashMap<String, Date> map = new HashMap<String, Date>();

        map.put("service1_name1", new Date(System.currentTimeMillis() + 10000)); // ten seconds in the future

        ReflectionTestUtils.setField(notificationCallManager, "earliestAllowedDates",
                new ConcurrentHashMap<String, Date>(map));

        // When
        boolean b = notificationCallManager.shouldICallThisService("service1", "name1");

        // Then
        assertFalse(b);
    }

    @Test
    public void testShouldICallThisService_Due() throws Exception {

        // Given
        HashMap<String, Date> map = new HashMap<String, Date>();

        map.put("service1_name1", new Date(System.currentTimeMillis() - 10000)); // ten seconds ago

        ReflectionTestUtils.setField(notificationCallManager, "earliestAllowedDates",
                new ConcurrentHashMap<String, Date>(map));

        // When
        boolean b = notificationCallManager.shouldICallThisService("service1", "name1");

        // Then
        assertTrue(b);
    }

    @Test
    public void testResetState() throws Exception {

        // When
        notificationCallManager.resetState();

        // Then
        assertEquals(0, notificationCallManager.getConsecutiveNullsMap().size());
        assertEquals(0, notificationCallManager.getCurrentKeysInCache().size());
        assertEquals(0, notificationCallManager.getEarliestAllowedDates().size());
    }

    @Test
    public void testBanAlfresco() throws Exception {
        // When
        notificationCallManager.banAlfresco();

        // Then
        assertTrue(notificationCallManager.getBanList().contains(NotificationServiceName.ALFRESCO.getName()));
    }

    @Test
    public void testBanEmail() throws Exception {
        // When
        notificationCallManager.banEmail();

        // Then
        assertTrue(notificationCallManager.getBanList().contains(NotificationServiceName.EMAIL.getName()));
    }

    @Test
    public void testBanInvoices() throws Exception {
        // When
        notificationCallManager.banInvoices();

        // Then
        assertTrue(notificationCallManager.getBanList().contains(NotificationServiceName.INVOICES.getName()));
    }

    @Test
    public void testBanMedControlCases() throws Exception {
        // When
        notificationCallManager.banMedControlCases();

        // Then
        assertTrue(notificationCallManager.getBanList().contains(NotificationServiceName.MED_CONTROL_CASES.getName()));
    }

    @Test
    public void testBanUsdIssues() throws Exception {
        // When
        notificationCallManager.banUsdIssues();

        // Then
        assertTrue(notificationCallManager.getBanList().contains(NotificationServiceName.USD_ISSUES.getName()));
    }

    @Test
    public void testResetBanList() throws Exception {
        // When
        notificationCallManager.banAlfresco();
        notificationCallManager.resetBanList();

        assertEquals(0, notificationCallManager.getBanList().size());
    }

    @Test
    @Ignore
    // I haven't tried to verify the expected outcome of this execution automatically but only manually checked that the
    // output looks as expected. Remove the @Ignore if you want to test it.
    public void testConcurrency() throws InterruptedException {

        Runnable notify = new Runnable() {
            @Override
            public void run() {
                notificationCallManager.notifyValue("asdf", null, "asdf");
            }
        };

        Runnable reset = new Runnable() {
            @Override
            public void run() {
                notificationCallManager.resetState();
            }
        };

        new Thread(notify).start();
        Thread.sleep(100);
        new Thread(notify).start();
        Thread.sleep(100);
        new Thread(notify).start();
        Thread.sleep(100);
        new Thread(notify).start();
        Thread.sleep(100);
        new Thread(notify).start();
        Thread.sleep(100);
        new Thread(notify).start();
        Thread.sleep(100);
        new Thread(notify).start();
        Thread.sleep(100);
        new Thread(notify).start();
        Thread.sleep(100);
        new Thread(notify).start();
        Thread.sleep(100);
        new Thread(reset).start(); // Reset
        Thread.sleep(100);
        new Thread(notify).start();
        Thread.sleep(100);
        new Thread(notify).start();
        Thread.sleep(100);
        new Thread(notify).start();
        Thread.sleep(100);
        new Thread(notify).start();
        Thread.sleep(100);
        new Thread(notify).start();
        Thread.sleep(100);
        new Thread(notify).start();
        Thread.sleep(100);
        new Thread(notify).start();
        Thread.sleep(100);
        new Thread(notify).start();
        Thread.sleep(100);
        new Thread(notify).start();
        Thread.sleep(100);
        new Thread(notify).start();
        Thread.sleep(100);
        new Thread(notify).start();
        Thread.sleep(100);
        Thread lastThread = new Thread(notify);
        lastThread.start();

        // Wait for the last thread and add a little more to be safe
        lastThread.join();
        Thread.sleep(100);

    }
}
