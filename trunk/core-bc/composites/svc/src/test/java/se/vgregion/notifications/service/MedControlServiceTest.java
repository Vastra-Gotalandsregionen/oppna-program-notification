package se.vgregion.notifications.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.vgregion.portal.medcontrol.domain.DeviationCase;
import se.vgregion.portal.medcontrol.services.MedControlDeviationService;
import se.vgregion.portal.medcontrol.services.MedControlDeviationServiceException;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

/**
 * @author Patrik Bergström
 */
public class MedControlServiceTest {

    private MedControlDeviationService medControlDeviationService = mock(MedControlDeviationService.class);

    private MedControlService medControlService = new MedControlService(medControlDeviationService);

    @Before
    public void setup() throws MedControlDeviationServiceException {

    }

    @Test
    public void testListDeviationCases() throws Exception {

        // Given
        DeviationCase case1 = new DeviationCase();
        case1.setActingRole(true);
        case1.setPhaseName("phaseName");
        case1.setCaseNumber("AD23");
        case1.setDescription("Lorem ipsum doro mio.");

        Mockito.when(medControlDeviationService.listDeviationCases(anyString()))
                .thenReturn(Arrays.asList(case1));

        // When
        List<DeviationCase> deviationCases = medControlService.listDeviationCases("asdfaklej", false);

        // Then
        assertEquals(1, deviationCases.size());
    }

    @Test
    public void testListDeviationCasesNullReturn() throws Exception {

        // Given
        Mockito.when(medControlDeviationService.listDeviationCases(anyString())).thenReturn(null);

        // When
        List<DeviationCase> deviationCases = medControlService.listDeviationCases("asdfaklej", false);

        // Then
        assertNull(deviationCases);
    }
}
