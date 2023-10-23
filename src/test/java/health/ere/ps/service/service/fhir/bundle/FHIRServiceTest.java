package health.ere.ps.service.fhir.bundle;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import health.ere.ps.config.UserConfig;
import health.ere.ps.event.BundlesEvent;
import health.ere.ps.event.Muster16PrescriptionFormEvent;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.service.fhir.FHIRService;
import health.ere.ps.validation.fhir.bundle.PrescriptionBundleValidator;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

public class FHIRServiceTest {

    @InjectMocks
    private FHIRService fhirService;

    @Mock
    private UserConfig userConfig;

    @Mock
    private PrescriptionBundleValidator prescriptionBundleValidator;

    @Mock
    private Event<BundlesEvent> bundleEvent;

    @Mock
    private Event<Exception> exceptionEvent;

    @BeforeAll
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * 
     */
    @Test
    public void testGeneratePrescriptionBundle() {
        // Create a sample Muster16PrescriptionFormEvent
        Muster16PrescriptionFormEvent event = new Muster16PrescriptionFormEvent(new Muster16PrescriptionForm());

        // Mock the behavior of userConfig
        when(userConfig.getPruefnummer()).thenReturn("123");

        // Mock the behavior of bundleEvent.fireAsync
        doNothing().when(bundleEvent).fireAsync(any());

        // Call the method to be tested
        fhirService.generatePrescriptionBundle(event);

        // Verify that userConfig.getPruefnummer() was called once
        verify(userConfig, times(1)).getPruefnummer();

        // Verify that prescriptionBundleValidator.validate() was called once
        verify(prescriptionBundleValidator, times(1)).validateBundle(any());

        // Verify that bundleEvent.fireAsync() was called once with a BundlesEvent argument
        verify(bundleEvent, times(1)).fireAsync(any(BundlesEvent.class));
    }

    @Test
    public void testGeneratePrescriptionBundleWithException() {
        // Create a sample Muster16PrescriptionFormEvent
        Muster16PrescriptionFormEvent event = new Muster16PrescriptionFormEvent(new Muster16PrescriptionForm());

        // Mock the behavior of userConfig
        when(userConfig.getPruefnummer()).thenReturn("123");

        // Mock the behavior of prescriptionBundleValidator
        when(prescriptionBundleValidator.validateBundle(any())).thenThrow(new RuntimeException("Validation failed"));

        // Call the method to be tested
        fhirService.generatePrescriptionBundle(event);

        // Verify that userConfig.getPruefnummer() was called once
        verify(userConfig, times(1)).getPruefnummer();

        // Verify that prescriptionBundleValidator.validate() was called once
        verify(prescriptionBundleValidator, times(1)).validateBundle(any());

        // Verify that exceptionEvent.fireAsync() was called once with an Exception argument
        verify(exceptionEvent, times(1)).fireAsync(any(Exception.class));
    }
}
