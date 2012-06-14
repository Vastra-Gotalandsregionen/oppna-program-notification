package se.vgregion.notifications.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.vgregion.portal.medcontrol.domain.DeviationCase;
import se.vgregion.portal.medcontrol.services.MedControlDeviationService;
import se.vgregion.portal.medcontrol.services.MedControlDeviationServiceException;

import java.util.List;

/**
 * Service class for fetching MedControl DeviationCases.
 *
 * @author Patrik Bergström
 */
@Service
public class MedControlService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MedControlService.class);

    private MedControlDeviationService medControlDeviationService;

    /**
     * Constructor.
     */
    public MedControlService() {
    }

    /**
     * Constructor.
     *
     * @param medControlDeviationService medControlDeviationService
     */
    @Autowired
    public MedControlService(MedControlDeviationService medControlDeviationService) {
        this.medControlDeviationService = medControlDeviationService;
    }

    /**
     * Fetches {@link DeviationCase}s for a given user.
     *
     * @param screenName   the screenName of the user
     * @param cachedResult whether cached result is satisfactory
     * @return a list of {@link DeviationCase}s
     */
    public List<DeviationCase> listDeviationCases(String screenName, boolean cachedResult) {
        List<DeviationCase> deviationCases = null;
        try {
            deviationCases = medControlDeviationService.listDeviationCases(screenName);
        } catch (MedControlDeviationServiceException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return deviationCases;
    }
}
