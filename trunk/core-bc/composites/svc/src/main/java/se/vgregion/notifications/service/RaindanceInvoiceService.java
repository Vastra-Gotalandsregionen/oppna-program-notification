package se.vgregion.notifications.service;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.vgregion.raindancenotifier.domain.InvoiceNotification;
import se.vgregion.raindancenotifier.services.RaindanceInvoicesService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Patrik Bergström
 */
@Service
class RaindanceInvoiceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaindanceInvoiceService.class);

    private RaindanceInvoicesService invoicesService;

    public RaindanceInvoiceService() {
        // Empty constructor is needed to make CGLIB happy
    }

    @Autowired
    public RaindanceInvoiceService(RaindanceInvoicesService invoicesService) {
        this.invoicesService = invoicesService;
    }

    public List<InvoiceNotification> getInvoices(final String userId, boolean cachedResult) {
        List<InvoiceNotification> invoices;
        try {
            invoices = invoicesService.getInvoices(userId);
        } catch (RuntimeException ex) {
            LOGGER.error(ex.getMessage()); // Do it this way since the logs would be full with stacktraces otherwise
            logCause(ex);
            return Collections.emptyList();
        }
        return invoices;
    }

    private void logCause(Throwable ex) {
        Throwable cause = ex.getCause();
        if (cause != null) {
            LOGGER.error("Cause: " + cause.getMessage());
            logCause(cause);
        }
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
