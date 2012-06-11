package se.vgregion.notifications.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;
import se.vgregion.notifications.domain.NotificationServiceName;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Patrik Bergström
 */
@Service
@ManagedResource
public class NotificationCallManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationCallManager.class);

    @Value("${notification.banned.services}")
    private String bannedServicesString;

    private Map<String, Integer> consecutiveNullsMap = new ConcurrentHashMap<String, Integer>();
    private Map<String, Date> earliestAllowedDates = new ConcurrentHashMap<String, Date>();
    private Set<String> bannedServices = Collections.synchronizedSet(new HashSet<String>());

    // This Queue is for deciding when the two Maps should be cleared of elements.
    private ConcurrentLinkedQueue<String> currentKeysInCache = new ConcurrentLinkedQueue<String>();

    private Object lock = new Object();

    public NotificationCallManager() {

    }

    @PostConstruct
    public void init() {
        // The bannedServicesString should be a comma-separated String with service names.
        if (bannedServicesString != null && !"".equals(bannedServicesString)) {
            bannedServicesString.replaceAll(" ", "");
            bannedServices = Collections.synchronizedSet(new HashSet<String>(Arrays.asList(
                    bannedServicesString.split(","))));
        }
    }

    public void notifyValue(String serviceName, Integer value, String screenName) {
        String key = getKey(serviceName, screenName);

        synchronized (lock) {
            if (value == null) {
                Integer numberNull = consecutiveNullsMap.get(key);
                if (numberNull == null) {
                    numberNull = 1; // Null for first time
                    consecutiveNullsMap.put(key, numberNull);
                    currentKeysInCache.add(key);

                    // Remove elements if there are to many
                    while (currentKeysInCache.size() > 10000) {
                        String keyToRemove = currentKeysInCache.poll();
                        // Remove from the two maps
                        consecutiveNullsMap.remove(keyToRemove);
                        earliestAllowedDates.remove(keyToRemove);
                    }
                    LOGGER.debug(getKey(key, " has resulted in null value for the first time."));
                } else {
                    numberNull++;
                    consecutiveNullsMap.put(key, numberNull);
                    LOGGER.debug(key + " has resulted in null " + numberNull + " times.");
                }
                double minutesDelayToNextCheck = Math.pow(2, numberNull); // 2, 4, 8, 16, 32, 64, 128, 256, 512

                if (minutesDelayToNextCheck > 256) {
                    minutesDelayToNextCheck = 256;
                }

                Calendar earliestAllowedDateToCheckAgain = Calendar.getInstance();
                earliestAllowedDateToCheckAgain.add(Calendar.MINUTE, (int) minutesDelayToNextCheck);

                earliestAllowedDates.put(key, earliestAllowedDateToCheckAgain.getTime());
            } else {
                consecutiveNullsMap.remove(key); // Reset - zero consecutive nulls
                earliestAllowedDates.remove(key); // No date limit
            }
        }
    }

    public boolean shouldICallThisService(String serviceName, String screenName) {
        if (bannedServices.contains(serviceName)) {
            LOGGER.debug(serviceName + " is banned. Do not call.");
            return false;
        }

        String key = getKey(serviceName, screenName);
        Date date = earliestAllowedDates.get(key);
        if (date == null) {
            LOGGER.info(key + " should result in a call.");
            return true; // No date limit for this key
        } else {
            if (date.after(new Date())) {
                LOGGER.info(key + " should NOT result in a call.");
                return false; // We're not there yet
            } else {
                LOGGER.info(key + " should result in a call.");
                return true;
            }
        }
    }

    private String getKey(String serviceName, String screenName) {
        return serviceName + "_" + screenName;
    }

    @ManagedOperation
    public Map<String, Integer> getConsecutiveNullsMap() {
        return consecutiveNullsMap;
    }

    @ManagedOperation
    public Map<String, Date> getEarliestAllowedDates() {
        return earliestAllowedDates;
    }

    @ManagedOperation
    public ConcurrentLinkedQueue<String> getCurrentKeysInCache() {
        return currentKeysInCache;
    }

    @ManagedOperation
    public void resetState() {
        synchronized (lock) {
            consecutiveNullsMap = new HashMap<String, Integer>();
            earliestAllowedDates = new HashMap<String, Date>();
            currentKeysInCache = new ConcurrentLinkedQueue<String>();
        }
    }

    @ManagedOperation
    public void banAlfresco() {
        LOGGER.info("Ban Alfresco");
        bannedServices.add(NotificationServiceName.ALFRESCO.getName());
    }

    @ManagedOperation
    public void banEmail() {
        LOGGER.info("Ban Email");
        bannedServices.add(NotificationServiceName.EMAIL.getName());
    }

    @ManagedOperation
    public void banInvoices() {
        LOGGER.info("Ban Invoices");
        bannedServices.add(NotificationServiceName.INVOICES.getName());
    }

    @ManagedOperation
    public void banMedControlCases() {
        LOGGER.info("Ban MedControlCases");
        bannedServices.add(NotificationServiceName.MED_CONTROL_CASES.getName());
    }

    @ManagedOperation
    public void banUsdIssues() {
        LOGGER.info("Ban USD Issues");
        bannedServices.add(NotificationServiceName.USD_ISSUES.getName());
    }

    @ManagedOperation
    public Set<String> getBanList() {
        return bannedServices;
    }

    @ManagedOperation
    public void resetBanList() {
        LOGGER.info("Resetting ban list.");
        bannedServices = Collections.synchronizedSet(new HashSet<String>());
    }

}
