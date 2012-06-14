package se.vgregion.notifications.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;
import se.vgregion.notifications.domain.NotificationServiceName;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class which helps decide whether services should be called or not.
 *
 * Many methods are exposed as {@link ManagedOperation}s and property are exposed as {@link ManagedAttribute}s, to
 * enable access via a JMX client.
 *
 * @author Patrik Bergström
 */
@Service
@ManagedResource
public class NotificationCallManager {

    private static Logger logger = LoggerFactory.getLogger(NotificationCallManager.class);

    private static final int MAXIMUM_ENTRIES_IN_MEMORY = 10000;
    private static final int LIMIT = 256;

    @Value("${notification.banned.services}")
    private String bannedServicesString;
    // Represents the number of consecutive null responses in a row for a specified key (serviceName + screenName)
    private ConcurrentHashMap<String, Integer> consecutiveNullsMap = new ConcurrentHashMap<String, Integer>();
    private ConcurrentHashMap<String, Date> earliestAllowedDates = new ConcurrentHashMap<String, Date>();

    private Set<String> bannedServices = Collections.synchronizedSet(new HashSet<String>());
    // This Queue is for deciding when the two Maps should be cleared of elements.
    private ConcurrentLinkedQueue<String> currentKeysInCache = new ConcurrentLinkedQueue<String>();


    /**
     * Constructor.
     */
    public NotificationCallManager() {

    }

    /**
     * Called by Spring after the instance is created.
     */
    @PostConstruct
    public void init() {
        // The bannedServicesString should be a comma-separated String with service names.
        if (bannedServicesString != null && !"".equals(bannedServicesString)) {
            bannedServicesString = bannedServicesString.replaceAll(" ", "");
            bannedServices = Collections.synchronizedSet(new HashSet<String>(Arrays.asList(
                    bannedServicesString.split(","))));
        }
    }

    /**
     * Call this method to let the instance record it. The calls to this method are decision foundation for the
     * {@link NotificationCallManager#shouldICallThisService(java.lang.String, java.lang.String)} method. Basically
     * the time before {@link NotificationCallManager#shouldICallThisService(java.lang.String, java.lang.String)}
     * returns <code>true</code> increases as the number of <code>null</code> values for a given serviceName and
     * screenName increases. The increase is in time is exponential to the number of consecutive <code>null</code>
     * values, with a limit of 256 minutes.
     *
     * @param serviceName the serviceName
     * @param value       the value
     * @param screenName  the screenName
     */
    public void notifyValue(String serviceName, Integer value, String screenName) {
        String key = getKey(serviceName, screenName);

        if (value == null) {
            Integer numberNull = consecutiveNullsMap.get(key);
            if (numberNull == null) {
                numberNull = 1; // Null for first time
                consecutiveNullsMap.put(key, numberNull);
                currentKeysInCache.add(key);

                // Remove elements if there are to many
                while (consecutiveNullsMap.size() > MAXIMUM_ENTRIES_IN_MEMORY) {
                    String keyToRemove = currentKeysInCache.poll();
                    // Remove from the two maps
                    consecutiveNullsMap.remove(keyToRemove);
                    earliestAllowedDates.remove(keyToRemove);
                }
                logger.warn(key + " has resulted in null value for the first time.");
            } else {
                numberNull++;
                consecutiveNullsMap.put(key, numberNull);
                logger.warn(key + " has resulted in null " + numberNull + " times.");
            }
            double minutesDelayToNextCheck = Math.pow(2, numberNull); // 2, 4, 8, 16, 32, 64, 128, 256, 512

            if (minutesDelayToNextCheck > LIMIT) {
                minutesDelayToNextCheck = LIMIT;
            }

            Calendar earliestAllowedDateToCheckAgain = Calendar.getInstance();
            earliestAllowedDateToCheckAgain.add(Calendar.MINUTE, (int) minutesDelayToNextCheck);

            earliestAllowedDates.put(key, earliestAllowedDateToCheckAgain.getTime());
        } else {
            consecutiveNullsMap.remove(key); // Reset - zero consecutive nulls
            earliestAllowedDates.remove(key); // No date limit
            currentKeysInCache.remove(key);
        }
    }

    /**
     * The method determines whether a service should be called based on prior calls to the
     * {@link NotificationCallManager#notifyValue(java.lang.String, java.lang.Integer, java.lang.String)} method.
     *
     * @see NotificationCallManager#notifyValue(java.lang.String, java.lang.Integer, java.lang.String)
     * @param serviceName the serviceName
     * @param screenName the screenName
     * @return <code>true</code> if the serviceName should be called or <code>false</code> otherwise
     */
    public boolean shouldICallThisService(String serviceName, String screenName) {
        if (bannedServices.contains(serviceName)) {
            logger.trace(serviceName + " is banned. Do not call.");
            return false;
        }

        String key = getKey(serviceName, screenName);
        Date date = earliestAllowedDates.get(key);
        if (date == null) {
            logger.trace(key + " should result in a call.");
            return true; // No date limit for this key
        } else {
            if (date.after(new Date())) {
                logger.trace(key + " should NOT result in a call.");
                return false; // We're not there yet
            } else {
                logger.trace(key + " should result in a call.");
                return true;
            }
        }
    }

    @ManagedAttribute
    public Map<String, Integer> getConsecutiveNullsMap() {
        return consecutiveNullsMap;
    }

    @ManagedAttribute
    public Map<String, Date> getEarliestAllowedDates() {
        return earliestAllowedDates;
    }

    @ManagedAttribute
    public ConcurrentLinkedQueue<String> getCurrentKeysInCache() {
        return currentKeysInCache;
    }

    /**
     * Reset the state, meaning that all that is affected by calls to the
     * {@link se.vgregion.notifications.service.NotificationCallManager#notifyValue
     * (java.lang.String, java.lang.Integer, java.lang.String)} method is reset.
     */
    @ManagedOperation
    public void resetState() {
        consecutiveNullsMap = new ConcurrentHashMap<String, Integer>();
        earliestAllowedDates = new ConcurrentHashMap<String, Date>();
        currentKeysInCache = new ConcurrentLinkedQueue<String>();
    }

    /**
     * Make calls to {@link NotificationCallManager#shouldICallThisService(java.lang.String, java.lang.String)} return
     * false for every call with serviceName equal to NotificationServiceName.ALFRESCO.getName().
     */
    @ManagedOperation
    public void banAlfresco() {
        logger.info("Ban Alfresco");
        bannedServices.add(NotificationServiceName.ALFRESCO.getName());
    }

    /**
     * Make calls to {@link NotificationCallManager#shouldICallThisService(java.lang.String, java.lang.String)} return
     * false for every call with serviceName equal to NotificationServiceName.EMAIL.getName().
     */
    @ManagedOperation
    public void banEmail() {
        logger.info("Ban Email");
        bannedServices.add(NotificationServiceName.EMAIL.getName());
    }

    /**
     * Make calls to {@link NotificationCallManager#shouldICallThisService(java.lang.String, java.lang.String)} return
     * false for every call with serviceName equal to NotificationServiceName.INVOICES.getName().
     */
    @ManagedOperation
    public void banInvoices() {
        logger.info("Ban Invoices");
        bannedServices.add(NotificationServiceName.INVOICES.getName());
    }

    /**
     * Make calls to {@link NotificationCallManager#shouldICallThisService(java.lang.String, java.lang.String)} return
     * false for every call with serviceName equal to NotificationServiceName.MED_CONTROL_CASES.getName().
     */
    @ManagedOperation
    public void banMedControlCases() {
        logger.info("Ban MedControlCases");
        bannedServices.add(NotificationServiceName.MED_CONTROL_CASES.getName());
    }

    /**
     * Make calls to {@link NotificationCallManager#shouldICallThisService(java.lang.String, java.lang.String)} return
     * <code>false</code> for every call with serviceName equal to NotificationServiceName.USD_ISSUES.getName().
     */
    @ManagedOperation
    public void banUsdIssues() {
        logger.info("Ban USD Issues");
        bannedServices.add(NotificationServiceName.USD_ISSUES.getName());
    }

    @ManagedAttribute
    public Set<String> getBanList() {
        return bannedServices;
    }

    /**
     * Reset the ban list.
     */
    @ManagedOperation
    public void resetBanList() {
        logger.info("Resetting ban list.");
        bannedServices = Collections.synchronizedSet(new HashSet<String>());
    }

    private String getKey(String serviceName, String screenName) {
        return serviceName + "_" + screenName;
    }

}
