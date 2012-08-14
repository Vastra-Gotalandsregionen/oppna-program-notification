package se.vgregion.notifications.service;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import se.vgregion.notifications.service.mock.InboxWebServiceMock;
import se.vgregion.portal.raindancenotifier.ws.domain.InboxResponse;
import se.vgregion.portal.raindancenotifier.ws.domain.RDConfiguration;
import se.vgregion.raindancenotifier.domain.InvoiceNotification;
import se.vgregion.raindancenotifier.services.RaindanceInvoicesService;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Patrik Bergström
 */
public class RaindanceInvoiceServiceTest {

    private RaindanceInvoiceService service;

    private String testXML = "<invoices><invoice><invoiceId>30019063</invoiceId><supplierName>"
            + "Fujitsu Services AB</supplierName><currency>SEK</currency><invoiceAmount>9562500.</invoiceAmount>"
            + "<waited>209</waited><message></message><status>1</status><type>1</type><expires>-190</expires>"
            + "</invoice><invoice><invoiceId>30019063</invoiceId><supplierName>"
            + "Schmujitsu Schmervices AB</supplierName><currency>SEK</currency><invoiceAmount>9500.</invoiceAmount>"
            + "<waited>209</waited><message></message><status>1</status><type>1</type><expires>10</expires>"
            + "</invoice></invoices>";


    @Before
    public void setUp() throws Exception {
        InboxWebServiceMock inboxWebServiceMock = new InboxWebServiceMock("userId", "password", "unit");
        RaindanceInvoicesService raindanceInvoicesService = new RaindanceInvoicesService();
        raindanceInvoicesService.setInboxWebService(inboxWebServiceMock);
        raindanceInvoicesService.setInboxConfiguration(new RDConfiguration());

        // Fill data.
        InboxResponse ir = new InboxResponse();
        ir.setInvoicesXML(testXML);
        ir.setCentral("100");
        inboxWebServiceMock.getInvoices().add(ir);

        ReflectionTestUtils.setField(raindanceInvoicesService, "urgentLimit", 5);
        service = new RaindanceInvoiceService(raindanceInvoicesService);

    }

    @Test
    public void testGetInvoices() throws Exception {
        List<InvoiceNotification> result = service.getInvoices("userId", false);
        assertEquals(2, result.size());
    }

    @Test
    public void testGetInvoicesJson() throws Exception {
        String invoicesJson = service.getInvoicesJson("userId");
        ObjectMapper mapper = new ObjectMapper();
        List<InvoiceNotification> invoices = mapper.readValue(invoicesJson, new TypeReference<List<InvoiceNotification>>() {
        });
        assertEquals(2, invoices.size());
    }
}
