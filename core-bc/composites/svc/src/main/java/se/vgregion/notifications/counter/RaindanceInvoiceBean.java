package se.vgregion.notifications.counter;

import org.springframework.beans.factory.annotation.Autowired;
import se.vgregion.raindancenotifier.domain.InvoiceNotification;
import se.vgregion.raindancenotifier.services.RaindanceInvoicesService;

import java.util.List;

/**
 * @author Patrik Bergström
 */
public class RaindanceInvoiceBean {

    private RaindanceInvoicesService invoicesService;

    @Autowired
    public RaindanceInvoiceBean(RaindanceInvoicesService invoicesService) {
        this.invoicesService = invoicesService;
    }

    public String getCount(final String userId) {
        List<InvoiceNotification> invoices = invoicesService.getInvoices(userId);
        return invoices.size() + "";
    }
}
