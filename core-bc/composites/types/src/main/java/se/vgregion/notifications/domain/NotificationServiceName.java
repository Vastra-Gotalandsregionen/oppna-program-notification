package se.vgregion.notifications.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Patrik Bergström
 */
public enum NotificationServiceName {
    ALFRESCO("alfresco"), EMAIL("email"), INVOICES("invoices"), USD_ISSUES("usdIssues"),
    /*SOCIAL_REQUEST("socialRequest"), */MED_CONTROL_CASES("medControlCases");

    private NotificationServiceName(String name) {
        this.name = name;
    }

    public static List<String> allNamesAsList() {
        List<String> list = new ArrayList<String>();
        for (NotificationServiceName n : NotificationServiceName.values()) {
            list.add(n.getName());
        }
        return list;
    }

    public String getName() {
        return name;
    }

    private String name;

}
