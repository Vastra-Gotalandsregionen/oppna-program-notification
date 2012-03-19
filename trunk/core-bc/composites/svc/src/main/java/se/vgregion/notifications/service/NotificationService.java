package se.vgregion.notifications.service;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import se.vgregion.alfrescoclient.domain.Document;
import se.vgregion.alfrescoclient.domain.Site;
import se.vgregion.usdservice.domain.Issue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Future;

/**
 * @author Patrik Bergström
 */
@Service
public class NotificationService {

    private AlfrescoDocumentsService alfrescoDocumentsService;
    private NotesCalendarCounterService notesCalendarCounterService;
    private NotesEmailCounterService notesEmailCounterService;
    private RaindanceInvoiceService raindanceInvoiceService;
    private UsdIssuesService usdIssuesService;

    public NotificationService() {
        // Empty constructor is needed to make CGLIB happy
    }

    @Autowired
    public NotificationService(AlfrescoDocumentsService alfrescoDocumentsService,
                               NotesCalendarCounterService notesCalendarCounterService,
                               NotesEmailCounterService notesEmailCounterService,
                               RaindanceInvoiceService raindanceInvoiceService,
                               UsdIssuesService usdIssuesService) {
        this.alfrescoDocumentsService = alfrescoDocumentsService;
        this.notesCalendarCounterService = notesCalendarCounterService;
        this.notesEmailCounterService = notesEmailCounterService;
        this.raindanceInvoiceService = raindanceInvoiceService;
        this.usdIssuesService = usdIssuesService;
    }

    @Async
    public Future<Integer> getAlfrescoCount(String screenName) throws IOException {
        List<Site> alfrescoResponse = alfrescoDocumentsService.getRecentlyModified(screenName, false);

        int n = 0;
        for (Site site : alfrescoResponse) {
            n += site.getRecentModifiedDocuments().size();
        }

        return new AsyncResult<Integer>(n);
    }

    @Async
    public Future<Integer> getUsdIssuesCount(String screenName) throws IOException {
        List<Issue> issues = usdIssuesService.getUsdIssues(screenName);

        return new AsyncResult<Integer>(issues.size());
    }

    @Async
    public Future<Integer> getRandomCount() throws InterruptedException {
        return new AsyncResult<Integer>(new Random().nextInt(10000));
    }
    
    @Async
    public Future<Integer> getEmailCount(String screenName) throws IOException {
        Integer count = notesEmailCounterService.getCount(screenName);
        return new AsyncResult<Integer>(count);
    }

    @Async
    public Future<Integer> getInvoicesCount(String screenName) {
        Integer count = raindanceInvoiceService.getCount(screenName);
        return new AsyncResult<Integer>(count);
    }
    
    public List<Document> getAlfrescoDocuments(String screenName) {
        List<Site> sites = alfrescoDocumentsService.getRecentlyModified(screenName, true);
        List<Document> documents = new ArrayList<Document>();
        for (Site site : sites) {
            documents.addAll(site.getRecentModifiedDocuments());
        }
        return documents;
    }
}
