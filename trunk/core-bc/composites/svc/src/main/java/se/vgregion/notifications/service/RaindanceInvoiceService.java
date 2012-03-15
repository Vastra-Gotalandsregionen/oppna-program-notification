package se.vgregion.notifications.service;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.vgregion.raindancenotifier.domain.InvoiceNotification;
import se.vgregion.raindancenotifier.services.RaindanceInvoicesService;

import java.io.IOException;
import java.util.List;

/**
 * @author Patrik Bergström
 */
public class RaindanceInvoiceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaindanceInvoiceService.class);

    private RaindanceInvoicesService invoicesService;

    @Autowired
    public RaindanceInvoiceService(RaindanceInvoicesService invoicesService) {
        this.invoicesService = invoicesService;
    }

    public String getCount(final String userId) {
        List<InvoiceNotification> invoices = invoicesService.getInvoices(userId);
        return invoices.size() + "";
    }

    public String getInvoicesJson(final String userId) {
        List<InvoiceNotification> invoices = invoicesService.getInvoices(userId);
        ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();
        try {
            return writer.writeValueAsString(invoices);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return "Internt fel.";
        }
    }
}
