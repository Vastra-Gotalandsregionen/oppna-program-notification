package se.vgregion.notifications.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.vgregion.portal.medcontrol.domain.DeviationCase;
import se.vgregion.portal.medcontrol.services.MedControlDeviationService;

import java.util.List;

/**
 * @author Patrik Bergström
 */
@Service
public class MedControlService {

    private MedControlDeviationService medControlDeviationService;

    @Autowired
    public MedControlService(MedControlDeviationService medControlDeviationService) {
        this.medControlDeviationService = medControlDeviationService;
    }

    public List<DeviationCase> listDeviationCases(String screenName, boolean cachedResult) {
        /*DeviationCase case1 = new DeviationCase();
        case1.setActingRole(true);
        case1.setPhaseName("phaseName");
        case1.setCaseNumber("AD23");
        case1.setDescription("Lorem ipsum doro mio.");
        return Arrays.asList(case1);*/
        return medControlDeviationService.listDeviationCases(screenName);
    }
}
