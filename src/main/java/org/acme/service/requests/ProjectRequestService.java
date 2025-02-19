package org.acme.service.requests;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dao.settings.InventoryItemDAO;
import org.acme.model.enums.projects.ProjectStatus;
import org.acme.model.enums.requests.RequestStatus;
import org.acme.model.requests.base.BaseRequest;
import org.acme.model.requests.common.RequestCommand;
import org.acme.model.requests.common.RequestFilter;
import org.acme.model.requests.project.ProjectRequest;
import org.acme.model.response.requests.RequestListResponse;
import org.acme.dao.requests.ProjectRequestDAO;
import org.acme.model.virtual_warehouse.item.InventoryItem;
import org.acme.pattern.context.BaseTransactionContext;
import org.acme.pattern.handlers.DatabaseHandler;
import org.acme.pattern.pipeline.Pipeline;
import org.acme.service.pipeline.ProjectAllocationCall;
import org.acme.service.pipeline.ProjectRequestApprovalPipeline;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.acme.model.enums.requests.RequestType.VIEW_ALL_LIST;

@ApplicationScoped
public class ProjectRequestService implements RequestInterface{
    @Inject
    ProjectRequestDAO projectRequestDAO;
    @Inject
    InventoryItemDAO inventoryItemDAO;


    @Override
    public List<? extends BaseRequest> getList(RequestFilter filter) {
        if (filter == null || filter.isEmpty() || filter.getRequestType().equals(VIEW_ALL_LIST)) {
            var list = projectRequestDAO.findAll().list();
            return list;
        }

        // Build the query dynamically using a Map
        Map<String, Object> queryMap = new HashMap<>();

        addFilter(queryMap, "associationId", filter.getAssociationId(), true);
//        addFilter(queryMap, "associationConfirmed", filter.getAssociationConfirmed(), true);
        addFilter(queryMap, "status", filter.getStatus().toString(), false);

        // Tags filter (Array field)
//        if (filter.getTags() != null && !filter.getTags().isEmpty()) {
//            queryMap.put("tags", Map.of("$in", filter.getTags()));
//        }

        // Date range filter
        if (filter.getDateFrom() != null && filter.getDateTo() != null) {
            queryMap.put("date", Map.of("$gte", filter.getDateFrom(), "$lte", filter.getDateTo()));
        }

        Bson bsonQuery = new Document(queryMap);

        return projectRequestDAO.find(bsonQuery).list();
    }

    @Override
    public RequestListResponse add(Class<? extends BaseRequest> request) {
//          WE NEED TO ADD A PROJECT REQUEST, GIVEN THAT WE HAVE ITEMS + QUANTITIES NEEDED
//          AND REQUEST ISSUER
        return null;
    }

    @Override
    public BaseRequest findByRequestId(String id) {
        var req = projectRequestDAO.findByRequestId(id);
        return req;
    }

    @Override
    public String approveRequest(RequestCommand command) {
        final BaseTransactionContext context = new BaseTransactionContext();

        DatabaseHandler<String, ProjectRequest, ProjectRequestService> mongoGetProjectRequestHandler =
                new DatabaseHandler<>(this,
                        (reqId, repository) -> {
                            var base = findByRequestId(reqId);
                            context.logStep("Fetched Request "+ base.requestId);
                            context.put("projectRequest", base);
                            return (ProjectRequest) base;
                        }, () -> {});

        DatabaseHandler<ProjectRequest, List<InventoryItem>, InventoryItemDAO> fetchInventoryItemsList =
                new DatabaseHandler<>(inventoryItemDAO,
                        (request, repository) -> {
                            var base = inventoryItemDAO.findAllAvailable();
                            context.logStep("Found " + base.size() + " available items");
                            return base;
                        }, () -> {});

        DatabaseHandler<ProjectRequest, String, ProjectRequestService> mongoPersistRequestHandler =
                new DatabaseHandler<>(this,
                        (req, repository) -> {
                            repository.persist(req);
                            context.put("rollback_mongoRequest", req);
                            return req.requestId;
                        },
                        () -> {
                            ProjectRequest req = context.get("projectRequest");
                            if (req != null) {
//                                projectRequestService.persist(req);
                                context.logStep("Rolled back Mongo request.");
                            }
                        });


        //1. fetch request from mongo
        var pipe = new Pipeline<>(mongoGetProjectRequestHandler, context)
        //2. check association is verified
        .addHandler(new ProjectRequestApprovalPipeline.CheckAssociationVerified())
        //3. fetch List<InventoryItem> and save it in context
        .addHandler(fetchInventoryItemsList)
        //4. create allocation request
        .addHandler(new ProjectRequestApprovalPipeline.CreateResourceAllocationRequest())
        //6. call allocationService, wait for allocationService response, in the meantime keep polling for execution status and send it to frontend (????)
        .addHandler(new ProjectAllocationCall())
        //7. parse the response and update the projects entity
        .addHandler(new ProjectRequestApprovalPipeline.ProcessAllocationResponseSingleProject())
        //8. update the projectResource allocation map
        .addHandler(mongoPersistRequestHandler);

        return pipe.execute(command.getRequestMongoId());
    }

    public String persist(ProjectRequest req) {
        projectRequestDAO.persistOrUpdate(req);
        return req.getRequestId();
    }

    public void delete(ProjectRequest req) {
        projectRequestDAO.delete(req);
    }

    public List<ProjectRequest> getListByStatus(ProjectStatus projectStatus){
        return projectRequestDAO.find("status = ?1 and projectStatus = ?2", RequestStatus.APPROVED, projectStatus).list();
    }
}
