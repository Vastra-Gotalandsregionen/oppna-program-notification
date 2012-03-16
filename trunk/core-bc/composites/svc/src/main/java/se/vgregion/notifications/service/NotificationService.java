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
    public Future<Integer> getNumberNewAlfresco(String screenName) throws IOException {
        List<Site> alfrescoResponse = alfrescoDocumentsService.getRecentlyModified(screenName);

        return new AsyncResult<Integer>(alfrescoResponse.size());
    }

    @Async
    public Future<Integer> getUsdIssues(String screenName) throws IOException {
        List<Issue> issues = usdIssuesService.getUsdIssues(screenName);

        return new AsyncResult<Integer>(issues.size());
    }

    @Async
    public Future<Integer> getSlowRandom() throws InterruptedException {
        Thread.sleep(3000);
        return new AsyncResult<Integer>(new Random().nextInt(10000));
    }


}
