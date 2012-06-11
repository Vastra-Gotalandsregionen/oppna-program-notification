package se.vgregion.notifications.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.vgregion.portal.medcontrol.domain.DeviationCase;
import se.vgregion.portal.medcontrol.services.MedControlDeviationService;
import se.vgregion.portal.medcontrol.services.MedControlDeviationServiceException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Patrik Bergström
 */
@Service
public class MedControlService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MedControlService.class);

    private MedControlDeviationService medControlDeviationService;

    public MedControlService() {
    }

    @Autowired
    public MedControlService(MedControlDeviationService medControlDeviationService) {
        this.medControlDeviationService = medControlDeviationService;
    }

    public List<DeviationCase> listDeviationCases(String screenName, boolean cachedResult) {
//        DeviationCase case1 = new DeviationCase();
//        case1.setActingRole(true);
//        case1.setPhaseName("phaseName");
//        case1.setCaseNumber("AD23");
//        case1.setDescription("Lorem ipsum doro mio.");
//        return Arrays.asList(case1);
        List<DeviationCase> deviationCases = null;
        try {
            deviationCases = medControlDeviationService.listDeviationCases(screenName);
        } catch (MedControlDeviationServiceException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return deviationCases;
    }
}
