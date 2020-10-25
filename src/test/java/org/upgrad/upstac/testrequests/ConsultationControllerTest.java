package org.upgrad.upstac.testrequests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.config.security.UserLoggedInService;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.testrequests.consultation.*;
import org.upgrad.upstac.testrequests.flow.TestRequestFlowService;
import org.upgrad.upstac.testrequests.lab.CreateLabResult;
import org.upgrad.upstac.testrequests.lab.LabRequestController;
import org.upgrad.upstac.testrequests.lab.LabResult;
import org.upgrad.upstac.testrequests.lab.TestStatus;
import org.upgrad.upstac.users.User;
import org.upgrad.upstac.users.models.Gender;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;


@ExtendWith(MockitoExtension.class)
@SpringBootTest
@Slf4j
class ConsultationControllerTest {


    @Qualifier("defaultValidator")
    @Autowired
    Validator validator;

    @InjectMocks
    ConsultationController consultationController;

    @Mock
    UserLoggedInService userLoggedInService;


    @Mock
    TestRequestUpdateService testRequestUpdateService;

    @Mock
    TestRequestQueryService testRequestQueryService;

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_assignForConsultation_with_valid_test_request_id_should_update_the_request_status(){


        User user= createUser();
        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(user);
        getTestRequestQueryServieMock(RequestStatus.LAB_TEST_COMPLETED).thenReturn(getMockedResponsesFrom(createTestRequest(), RequestStatus.LAB_TEST_COMPLETED));
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_COMPLETED);
        Mockito.when(testRequestUpdateService.assignForConsultation(testRequest.getRequestId(), user)).thenReturn(assignForConsultation(testRequest, user));


        //Implement this method

        //Create another object of the TestRequest method and explicitly assign this object for Consultation using assignForConsultation() method
        // from consultationController class. Pass the request id of testRequest object.
        TestRequest secondRequest = consultationController.assignForConsultation(testRequest.getRequestId());

        //Use assertThat() methods to perform the following two comparisons
        //  1. the request ids of both the objects created should be same
        //  2. the status of the second object should be equal to 'DIAGNOSIS_IN_PROCESS'
        // make use of assertNotNull() method to make sure that the consultation value of second object is not null
        // use getConsultation() method to get the lab result

        assertNotNull(secondRequest);
        assertEquals(testRequest.getRequestId(), secondRequest.getRequestId());
        assertEquals(secondRequest.getStatus(), RequestStatus.DIAGNOSIS_IN_PROCESS);
        assertNotNull(secondRequest.getConsultation());

    }

    public TestRequest getTestRequestByStatus(RequestStatus status) {
        return testRequestQueryService.findBy(status).stream().findFirst().get();
    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_assignForConsultation_with_valid_test_request_id_should_throw_exception(){

        Long InvalidRequestId= -34L;

        //Implement this method
        User user= createUser();
        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(user);
        Mockito.when(testRequestUpdateService.assignForConsultation(InvalidRequestId, user)).thenThrow(new AppException("Invalid ID or State"));

        // Create an object of ResponseStatusException . Use assertThrows() method and pass assignForConsultation() method
        // of consultationController with InvalidRequestId as Id
        ResponseStatusException result = assertThrows(ResponseStatusException.class,()->{
            TestRequest r = consultationController.assignForConsultation(InvalidRequestId);
        });

        //Use assertThat() method to perform the following comparison
        //  the exception message should be contain the string "Invalid ID"
        assertNotNull(result);
        assertTrue(result.getMessage().contains("Invalid ID"));
    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_valid_test_request_id_should_update_the_request_status_and_update_consultation_details(){

        User user= createUser();
        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(user);
        getTestRequestQueryServieMock(RequestStatus.DIAGNOSIS_IN_PROCESS).thenReturn(getMockedResponsesFrom(createTestRequest(), RequestStatus.DIAGNOSIS_IN_PROCESS));
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);
        LabResult labresult = new LabResult();
        labresult.setResult(TestStatus.NEGATIVE);
        testRequest.setLabResult(labresult);
        Consultation consultation = new Consultation();
        testRequest.setConsultation (consultation);
        //Implement this method
        //Create an object of CreateConsultationRequest and call getCreateConsultationRequest() to create the object. Pass the above created object as the parameter
        CreateConsultationRequest consultationRequest = getCreateConsultationRequest(testRequest);
        //Create another object of the TestRequest method and explicitly update the status of this object
        // to be 'COMPLETED'. Make use of updateConsultation() method from labRequestController class (Pass the previously created two objects as parameters)

        Mockito.when(testRequestUpdateService.updateConsultation(testRequest.getRequestId(), consultationRequest, user)).thenReturn(updateConsultation(testRequest, consultationRequest));

        TestRequest anotherTestRequest = consultationController.updateConsultation(testRequest.getRequestId(), consultationRequest);

        //Use assertThat() methods to perform the following three comparisons
        //  1. the request ids of both the objects created should be same
        //  2. the status of the second object should be equal to 'COMPLETED'
        // 3. the suggestion of both the objects created should be same. Make use of getSuggestion() method to get the results.

        assertNotNull(anotherTestRequest);
        assertEquals(anotherTestRequest.getRequestId(), testRequest.getRequestId());
        assertEquals(anotherTestRequest.getStatus(), RequestStatus.COMPLETED);
        assertEquals(anotherTestRequest.getConsultation().getSuggestion(), testRequest.getConsultation().getSuggestion());
    }


    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_invalid_test_request_id_should_throw_exception(){

        Long InvalidRequestId= -34L;
        User user= createUser();
        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(user);
        getTestRequestQueryServieMock(RequestStatus.DIAGNOSIS_IN_PROCESS).thenReturn(getMockedResponsesFrom(createTestRequest(), RequestStatus.DIAGNOSIS_IN_PROCESS));
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);
        LabResult labresult = new LabResult();
        labresult.setResult(TestStatus.NEGATIVE);
        testRequest.setLabResult(labresult);
        //Implement this method

        //Create an object of CreateConsultationRequest and call getCreateConsultationRequest() to create the object. Pass the above created object as the parameter
        CreateConsultationRequest consultationRequest = getCreateConsultationRequest(testRequest);
        // Create an object of ResponseStatusException . Use assertThrows() method and pass updateConsultation() method
        // of consultationController with a negative long value as Id and the above created object as second parameter
        //Refer to the TestRequestControllerTest to check how to use assertThrows() method

        Mockito.when(testRequestUpdateService.updateConsultation(InvalidRequestId, consultationRequest, user)).thenThrow(new AppException("Invalid ID or State"));

        ResponseStatusException result = assertThrows(ResponseStatusException.class,()->{
            TestRequest r = consultationController.updateConsultation(InvalidRequestId, consultationRequest);
        });

        //Use assertThat() method to perform the following comparison
        //  the exception message should be contain the string "Invalid ID"

        assertNotNull(result);
        assertTrue(result.getMessage().contains("Invalid ID"));
    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_invalid_empty_status_should_throw_exception(){

        User user= createUser();
        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(user);
        getTestRequestQueryServieMock(RequestStatus.DIAGNOSIS_IN_PROCESS).thenReturn(getMockedResponsesFrom(createTestRequest(), RequestStatus.DIAGNOSIS_IN_PROCESS));
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);
        LabResult labresult = new LabResult();
        labresult.setResult(TestStatus.NEGATIVE);
        testRequest.setLabResult(labresult);
        //Implement this method

        //Create an object of CreateConsultationRequest and call getCreateConsultationRequest() to create the object. Pass the above created object as the parameter
        // Set the suggestion of the above created object to null.
        CreateConsultationRequest consultationRequest = getCreateConsultationRequest(testRequest);
        consultationRequest.setSuggestion(null);

        Mockito.when(testRequestUpdateService.updateConsultation(testRequest.getRequestId(), consultationRequest, user)).then((something)->{
            Set<ConstraintViolation<CreateConsultationRequest>> constraintViolations =
                    validator.validate(consultationRequest);
            if(constraintViolations.size()> 0) {
                throw new ConstraintViolationException(constraintViolations);
            }
            return null;
        });

        // Create an object of ResponseStatusException . Use assertThrows() method and pass updateConsultation() method
        // of consultationController with request Id of the testRequest object and the above created object as second parameter
        //Refer to the TestRequestControllerTest to check how to use assertThrows() method
        ResponseStatusException result = assertThrows(ResponseStatusException.class,()->{
            TestRequest r = consultationController.updateConsultation(testRequest.requestId, consultationRequest);
        });

        assertNotNull(result);
        assertTrue(result.getMessage().contains("ConstraintViolationException"));
    }

    public CreateConsultationRequest getCreateConsultationRequest(TestRequest testRequest) {

        //Create an object of CreateLabResult and set all the values
        // if the lab result test status is Positive, set the doctor suggestion as "HOME_QUARANTINE" and comments accordingly
        // else if the lab result status is Negative, set the doctor suggestion as "NO_ISSUES" and comments as "Ok"
        // Return the object

        CreateConsultationRequest request = new CreateConsultationRequest();

        if(testRequest.getLabResult().getResult() == TestStatus.POSITIVE)
        {
            request.setComments("Please quarantine");
            request.setSuggestion(DoctorSuggestion.HOME_QUARANTINE);
        }
        else
        {
            request.setComments("OK");
            request.setSuggestion(DoctorSuggestion.NO_ISSUES);
        }

        return request;

    }

    private org.mockito.stubbing.OngoingStubbing<java.util.List<TestRequest>> getTestRequestQueryServieMock(RequestStatus status)
    {
        return Mockito.when(testRequestQueryService.findBy(status));
    }

    private List<TestRequest> getMockedResponsesFrom(CreateTestRequest createTestRequest)
    {
        List<TestRequest> list = new ArrayList<>();
        list.add(getMockedResponseFrom(createTestRequest()));
        return list;
    }

    private List<TestRequest> getMockedResponsesFrom(CreateTestRequest createTestRequest, RequestStatus status)
    {
        List<TestRequest> list = new ArrayList<>();
        list.add(getMockedResponseFrom(createTestRequest(), status));
        return list;
    }
    private TestRequest getMockedResponseFrom(CreateTestRequest createTestRequest) {
        TestRequest testRequest = new TestRequest();

        testRequest.setName(createTestRequest.getName());
        testRequest.setCreated(LocalDate.now());
        testRequest.setStatus(RequestStatus.INITIATED);
        testRequest.setAge(createTestRequest.getAge());
        testRequest.setEmail(createTestRequest.getEmail());
        testRequest.setPhoneNumber(createTestRequest.getPhoneNumber());
        testRequest.setPinCode(createTestRequest.getPinCode());
        testRequest.setAddress(createTestRequest.getAddress());
        testRequest.setGender(createTestRequest.getGender());
        testRequest.setRequestId(1L);
        testRequest.setCreatedBy(createUser());

        return testRequest;
    }

    private TestRequest getMockedResponseFrom(CreateTestRequest createTestRequest, RequestStatus status) {
        TestRequest testRequest = getMockedResponseFrom(createTestRequest);
        testRequest.setStatus(status);
        return testRequest;
    }
    private CreateTestRequest createTestRequest() {
        CreateTestRequest createTestRequest = new CreateTestRequest();
        createTestRequest.setAddress("some Addres");
        createTestRequest.setAge(98);
        createTestRequest.setEmail("someone" + "123456789" + "@somedomain.com");
        createTestRequest.setGender(Gender.MALE);
        createTestRequest.setName("someuser");
        createTestRequest.setPhoneNumber("123456789");
        createTestRequest.setPinCode(716768);
        return createTestRequest;
    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setUserName("someuser");
        return user;
    }

    private TestRequest assignForConsultation(TestRequest testRequest, User doctor) {

        Consultation c = new Consultation();
        c.setDoctor(doctor);
        c.setRequest(testRequest);

        testRequest.setConsultation(c);
        testRequest.setStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);

        return testRequest;
    }

    public TestRequest updateConsultation(TestRequest testRequest , CreateConsultationRequest createConsultationRequest) {

        Consultation c = testRequest.getConsultation();
        c.setSuggestion(createConsultationRequest.getSuggestion());
        c.setComments(createConsultationRequest.getComments());
        c.setUpdatedOn(LocalDate.now());

        testRequest.setStatus(RequestStatus.COMPLETED);

        return testRequest;
    }




}