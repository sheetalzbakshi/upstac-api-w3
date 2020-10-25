package org.upgrad.upstac.testrequests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Any;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithUserDetails;
//mport org.springframework.validation.Validator;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.config.security.UserLoggedInService;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.testrequests.lab.*;
import org.upgrad.upstac.users.User;
import org.upgrad.upstac.users.models.Gender;

import javax.imageio.event.IIOReadUpdateListener;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.awt.*;
import java.awt.geom.Area;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@Slf4j
class LabRequestControllerTest {

    @Qualifier("defaultValidator")
    @Autowired
    Validator validator;

    @InjectMocks
    LabRequestController labRequestController;

    @Mock
    UserLoggedInService userLoggedInService;

    @Mock
    private TestRequestUpdateService testRequestUpdateService;

    @Mock
    TestRequestQueryService testRequestQueryService;

    @Mock
    LabResultService labResultService;


    @Test
    @WithUserDetails(value = "tester")
    public void calling_assignForLabTest_with_valid_test_request_id_should_update_the_request_status(){


        //Implement this method

        //Create another object of the TestRequest method and explicitly assign this object for Lab Test using assignForLabTest() method
        // from labRequestController class. Pass the request id of testRequest object.

        //Use assertThat() methods to perform the following two comparisons
        //  1. the request ids of both the objects created should be same
        //  2. the status of the second object should be equal to 'INITIATED'
        // make use of assertNotNull() method to make sure that the lab result of second object is not null
        // use getLabResult() method to get the lab result
        User user= createUser();
        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(user);
        Mockito.when(testRequestQueryService.findBy(RequestStatus.INITIATED)).thenReturn(getMockedResponsesFrom(createTestRequest()));

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.INITIATED);
        Mockito.when(testRequestUpdateService.assignForLabTest(testRequest.requestId, user)).thenReturn(testRequest);
        TestRequest returnRequest = labRequestController.assignForLabTest(testRequest.requestId);
        assertNotNull(returnRequest);
        assertEquals(testRequest.requestId, returnRequest.requestId);
        assertEquals(returnRequest.getStatus(), RequestStatus.INITIATED);
    }

    public TestRequest getTestRequestByStatus(RequestStatus status) {
        return testRequestQueryService.findBy(status).stream().findFirst().get();
    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_assignForLabTest_with_valid_test_request_id_should_throw_exception(){

        Long InvalidRequestId= -34L;

        //Implement this method


        // Create an object of ResponseStatusException . Use assertThrows() method and pass assignForLabTest() method
        // of labRequestController with InvalidRequestId as Id


        //Use assertThat() method to perform the following comparison
        //  the exception message should be contain the string "Invalid ID"

        User user= createUser();
        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(user);
        Mockito.when(testRequestUpdateService.assignForLabTest(InvalidRequestId, user)).thenThrow(new AppException("Invalid data")) ;

        ResponseStatusException result = assertThrows(ResponseStatusException.class,()->{
            TestRequest r = labRequestController.assignForLabTest(InvalidRequestId);
        });
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
        assertEquals("Invalid data",result.getReason());
    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_valid_test_request_id_should_update_the_request_status_and_update_test_request_details(){


        //Implement this method
        //Create an object of CreateLabResult and call getCreateLabResult() to create the object. Pass the above created object as the parameter

        //Create another object of the TestRequest method and explicitly update the status of this object
        // to be 'LAB_TEST_IN_PROGRESS'. Make use of updateLabTest() method from labRequestController class (Pass the previously created two objects as parameters)

        //Use assertThat() methods to perform the following three comparisons
        //  1. the request ids of both the objects created should be same
        //  2. the status of the second object should be equal to 'LAB_TEST_COMPLETED'
        // 3. the results of both the objects created should be same. Make use of getLabResult() method to get the results.

        User user= createUser();
        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(user);
        Mockito.when(testRequestQueryService.findBy(RequestStatus.LAB_TEST_IN_PROGRESS)).thenReturn(getMockedResponsesFrom(createTestRequest(), RequestStatus.LAB_TEST_IN_PROGRESS));
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);
        CreateLabResult labResult = getCreateLabResult(testRequest);
        Mockito.when(testRequestUpdateService.updateLabTest(testRequest.getRequestId(), labResult, user)).thenReturn(updateLabTestAndSetStatus(testRequest, labResult, RequestStatus.COMPLETED));

        TestRequest returned = labRequestController.updateLabTest(testRequest.getRequestId(), labResult);

        assertNotNull(returned);
        assertEquals(testRequest.getRequestId(), returned.getRequestId());
        assertEquals(returned.getStatus(), RequestStatus.COMPLETED);
        assertEquals(testRequest.getLabResult(), returned.getLabResult());
    }


    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_invalid_test_request_id_should_throw_exception(){

        Long InvalidRequestId= -34L;

        User user= createUser();
        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(user);
        Mockito.when(testRequestQueryService.findBy(RequestStatus.LAB_TEST_IN_PROGRESS)).thenReturn(getMockedResponsesFrom(createTestRequest(), RequestStatus.LAB_TEST_IN_PROGRESS));
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);


        //Implement this method

        //Create an object of CreateLabResult and call getCreateLabResult() to create the object. Pass the above created object as the parameter
        CreateLabResult labResult = getCreateLabResult(testRequest);

        Mockito.when(testRequestUpdateService.updateLabTest(InvalidRequestId, labResult, user)).thenThrow( new AppException("Invalid ID or State"));
        // Create an object of ResponseStatusException . Use assertThrows() method and pass updateLabTest() method
        // of labRequestController with a negative long value as Id and the above created object as second parameter
        //Refer to the TestRequestControllerTest to check how to use assertThrows() method
        ResponseStatusException result = assertThrows(ResponseStatusException.class,()->{
            TestRequest r = labRequestController.updateLabTest(InvalidRequestId, labResult);
        });

        assertNotNull(result);
        assertTrue(result.getReason().startsWith("Invalid ID"));
        //Use assertThat() method to perform the following comparison
        //  the exception message should be contain the string "Invalid ID"

    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_invalid_empty_status_should_throw_exception(){

        User user= createUser();
        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(user);
        Mockito.when(testRequestQueryService.findBy(RequestStatus.LAB_TEST_IN_PROGRESS)).thenReturn(getMockedResponsesFrom(createTestRequest(), RequestStatus.LAB_TEST_IN_PROGRESS));
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);

        //Implement this method

        //Create an object of CreateLabResult and call getCreateLabResult() to create the object. Pass the above created object as the parameter
        // Set the result of the above created object to null.
        CreateLabResult labresult = getCreateLabResult(testRequest);
        labresult.setResult(null);

        Mockito.when(testRequestUpdateService.updateLabTest(testRequest.getRequestId(), labresult, user)).then((something)->{
            Set<ConstraintViolation<CreateLabResult>> constraintViolations =
                    validator.validate(labresult);
            if(constraintViolations.size()> 0) {
                throw new ConstraintViolationException(constraintViolations);
            }
            return null;
        });

        // Create an object of ResponseStatusException . Use assertThrows() method and pass updateLabTest() method
        // of labRequestController with request Id of the testRequest object and the above created object as second parameter
        //Refer to the TestRequestControllerTest to check how to use assertThrows() method

        ResponseStatusException result = assertThrows(ResponseStatusException.class,()->{
            TestRequest r = labRequestController.updateLabTest(testRequest.getRequestId(), labresult);
        });

        //Use assertThat() method to perform the following comparison
        //  the exception message should be contain the string "ConstraintViolationException"

        assertNotNull(result);
        assertTrue(result.getMessage().contains("ConstraintViolationException"));
    }

    public CreateLabResult getCreateLabResult(TestRequest testRequest) {

        //Create an object of CreateLabResult and set all the values
        // Return the object

        CreateLabResult request = new CreateLabResult();
        request.setBloodPressure("100");
        request.setHeartBeat("78");
        request.setTemperature("38");
        request.setOxygenLevel("Low");
        request.setComments("OK");
        request.setResult(TestStatus.NEGATIVE);

        return request;
    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setUserName("someuser");
        return user;
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

    private TestRequest updateLabTestAndSetStatus(TestRequest testRequest, CreateLabResult createLabResult, RequestStatus status) {

        //Implement this method to update the lab test
        // create an object of LabResult and make use of setters to set Blood Pressure, Comments,
        // HeartBeat, OxygenLevel, Temperature, Result and UpdatedOn values
        // make use of the saveLabResult() method to return the object of LabResult

        LabResult lresult = new LabResult();
        lresult.setBloodPressure(createLabResult.getBloodPressure());
        lresult.setComments(createLabResult.getComments());
        lresult.setOxygenLevel(createLabResult.getOxygenLevel());
        lresult.setTemperature(createLabResult.getTemperature());
        lresult.setResult(createLabResult.getResult());
        lresult.setUpdatedOn(LocalDate.now());
        testRequest.setStatus(status);
        return testRequest;


    }
}