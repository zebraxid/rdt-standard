package io.ona.rdt.interactor;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.smartregister.sync.ClientProcessorForJava;

import io.ona.rdt.application.RDTApplication;
import io.ona.rdt.domain.Patient;

import static io.ona.rdt.interactor.PatientRegisterFragmentInteractorTest.expectedPatient;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Vincent Karuri on 05/08/2019
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({RDTApplication.class, ClientProcessorForJava.class})
public class PatientRegisterActivityInteractorTest {

    private PatientRegisterActivityInteractor interactor;
    private static JSONObject formJsonObj;

    @BeforeClass
    public static void init() throws JSONException {
        formJsonObj = new JSONObject(PatientRegisterFragmentInteractorTest.PATIENT_REGISTRATION_JSON_FORM);
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        interactor = new PatientRegisterActivityInteractor();
    }

    @Test
    public void getPatientForRDTReturnsValidPatient() throws JSONException {
        Patient rdtPatient = interactor.getPatientForRDT(formJsonObj);
        assertNotNull(rdtPatient);
        assertEquals(rdtPatient.getPatientName(), expectedPatient.getPatientName());
        assertEquals(rdtPatient.getPatientSex(), expectedPatient.getPatientSex());
        assertEquals(rdtPatient.getBaseEntityId(), expectedPatient.getBaseEntityId());
    }
}