package org.acme.controller.requests;

import io.quarkus.test.InjectMock;
import io.quarkus.test.Mock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.acme.dto.ProjectAllocatedResourcesDTO;
import org.acme.dto.ProjectItemDTO;
import org.acme.dto.ProjectRequestDTO;
import org.acme.model.enums.projects.ProjectStatus;
import org.acme.model.enums.requests.RequestCommandType;
import org.acme.model.enums.requests.RequestType;
import org.acme.model.requests.common.RequestCommand;
import org.acme.model.response.requests.AddRequestResponse;
import org.acme.model.response.requests.ApproveRequestResponse;
import org.acme.model.response.requests.RequestCommonData;
import org.acme.service.requests.AssociationRequestService;
import org.acme.service.requests.CommonRequestService;
import org.acme.service.requests.InventoryRequestService;
import org.acme.service.requests.ProjectRequestService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static reactor.core.publisher.Mono.when;

@QuarkusTest
class RequestControllerTest {

//    @Test
//    public void testCommonEndpoint() {
//        given()
//                .when().get("/api/requests/common")
//                .then()
//                .statusCode(200)
//                .body(notNullValue());
//    }
//
////    @Test
////    public void testListEndpoint() {
////        // Minimal filter JSON for a PROJECT_REQUEST
////        String filterJson = "{ \"requestType\": \"PROJECT_REQUEST\" }";
////        given()
////                .contentType("application/json")
////                .body(filterJson)
////                .when().post("/api/requests/list")
////                .then()
////                .statusCode(200)
////                // Expect that the returned JSON contains a non-null property
////                .body("projectRequests", notNullValue());
////    }
////
////    @Test
////    public void testAddRequestEndpoint() {
////        // Build a JSON command for a project request.
////        String commandJson = "{\n" +
////                "  \"requestMongoId\": \"\",\n" +
////                "  \"commandType\": \"INSERT\",\n" +
////                "  \"requestType\": \"PROJECT_REQUEST\",\n" +
////                "  \"request\": {\n" +
////                "    \"projectName\": \"Test Project\",\n" +
////                "    \"description\": \"Test Description\",\n" +
////                "    \"budget\": 100,\n" +
////                "    \"requiredItemsSQLId\": [ { \"sqlId\": 1, \"quantityNeeded\": 5 } ],\n" +
////                "    \"projectPlan\": [],\n" +
////                "    \"projectStatus\": \"ALLOCATED\",\n" +
////                "    \"allocationId\": \"\",\n" +
////                "    \"allocatedResources\": {}\n" +
////                "  }\n" +
////                "}";
////        given()
////                .contentType("application/json")
////                .body(commandJson)
////                .when().post("/api/requests/add")
////                .then()
////                .statusCode(200);
////    }
////
////    @Test
////    public void testGetRequestEndpoint() {
////        // Create a command to get a project request. Here we use a dummy id.
////        String dummyId = UUID.randomUUID().toString();
////        String commandJson = "{\n" +
////                "  \"requestType\": \"PROJECT_REQUEST\",\n" +
////                "  \"requestMongoId\": \"" + dummyId + "\"\n" +
////                "}";
////        given()
////                .contentType("application/json")
////                .body(commandJson)
////                .when().post("/api/requests/get")
////                .then()
////                .statusCode(200)
////                // Assuming that a valid get returns a JSON with a non-null projectRequestDTO property
////                .body("projectRequestDTO", notNullValue());
////    }
////
////    @Test
////    public void testApproveRequestEndpoint() {
////        // Create a command to approve a project request with a dummy id.
////        String dummyId = UUID.randomUUID().toString();ok bu
////        String commandJson = "{\n" +
////                "  \"requestType\": \"PROJECT_REQUEST\",\n" +
////                "  \"requestMongoId\": \"" + dummyId + "\"\n" +
////                "}";
////        given()
////                .contentType("application/json")
////                .body(commandJson)
////                .when().post("/api/requests/approve")
////                .then()
////                .statusCode(200);
////    }
//
//
//    @Test
//    public void testFullLifecycleForProjectRequest() {
//        // 1. Build the ProjectRequestDTO
//        ProjectRequestDTO projectRequestDTO = new ProjectRequestDTO();
//        projectRequestDTO.setProjectName("Test Project");
//        projectRequestDTO.setDescription("Integration test project");
//        projectRequestDTO.setBudget(1000);
//
//        List<ProjectItemDTO> requiredItems = new ArrayList<>();
//        ProjectItemDTO item = new ProjectItemDTO();
//        item.setSqlId(1);
//        item.setQuantityNeeded(5);
//        requiredItems.add(item);
//        projectRequestDTO.setRequiredItemsSQLId(requiredItems);
//
//        // Assume an empty project plan for simplicity
//        projectRequestDTO.setProjectPlan(new ArrayList<>());
//        projectRequestDTO.setProjectStatus(ProjectStatus.ALLOCATED);
//        projectRequestDTO.setAllocationId("");
//        projectRequestDTO.setAllocatedResources(new ProjectAllocatedResourcesDTO());
//
//        // 2. Create a RequestCommand for the add operation
//        RequestCommand addCommand = new RequestCommand();
////        addCommand.setRequestId();
//        addCommand.setCommandType(RequestCommandType.INSERT);
//        addCommand.setRequestType(RequestType.PROJECT_REQUEST);
//        addCommand.setProjectRequestDTO(projectRequestDTO);
//
//        // 3. POST to /api/requests/add and capture the dummy request ID
//        AddRequestResponse addResponse =
//                given()
//                        .contentType(ContentType.JSON)
//                        .body(addCommand)
//                        .when()
//                        .post("/api/requests/add")
//                        .then()
//                        .statusCode(200)
//                        .extract().as(AddRequestResponse.class);
//
//        String createdRequestId = addResponse.getRequestMongoID();
//        assertNotNull(createdRequestId, "Created request ID should not be null");
//
//        // 4. Approve the created request using its ID
//        RequestCommand approveCommand = new RequestCommand();
//        approveCommand.setRequestType(RequestType.PROJECT_REQUEST);
//        approveCommand.setCommandType(RequestCommandType.APPROVE);
//        approveCommand.setRequestId(new ObjectId(createdRequestId));
//
//        ApproveRequestResponse approveResponse =
//                given()
//                        .contentType(ContentType.JSON)
//                        .body(approveCommand)
//                        .when()
//                        .post("/api/requests/approve")
//                        .then()
//                        .statusCode(200)
//                        .extract().as(ApproveRequestResponse.class);
//
//        // Optionally, perform assertions on approveResponse
//
//        // 5. Clean up by deleting the request.
//        // Assuming you have a delete endpoint (e.g., /api/requests/delete) that accepts a similar command.
//        RequestCommand deleteCommand = new RequestCommand();
//        deleteCommand.setRequestType(RequestType.PROJECT_REQUEST);
////        deleteCommand.setCommandType(RequestCommandType.);
//        deleteCommand.setRequestId(new ObjectId(createdRequestId));
//
//        given()
//                .contentType(ContentType.JSON)
//                .body(deleteCommand)
//                .when()
//                .post("/api/requests/delete")
//                .then()
//                .statusCode(200);
//    }
}