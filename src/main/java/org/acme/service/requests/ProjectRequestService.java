package org.acme.service.requests;

import io.grpc.ManagedChannelBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dao.settings.InventoryItemDAO;
import org.acme.dto.ProjectRequestDTO;
import org.acme.model.enums.projects.ProjectStatus;
import org.acme.model.enums.requests.RequestStatus;
import org.acme.model.enums.simulation.GreedyOrder;
import org.acme.model.enums.simulation.GreedyStrategy;
import org.acme.model.requests.common.RequestCommand;
import org.acme.model.requests.common.RequestFilter;
import org.acme.model.requests.project.ProjectAllocatedResources;
import org.acme.model.requests.project.ProjectItem;
import org.acme.model.requests.project.ProjectRequest;
import org.acme.dao.requests.ProjectRequestDAO;
import org.acme.model.response.requests.inventory.SimulateRequestResponse;
import org.acme.model.virtual_warehouse.item.InventoryItem;
import org.acme.pattern.context.BaseTransactionContext;
import org.acme.pattern.exceptions.AssociationNotConfirmedException;
import org.acme.pattern.handlers.DatabaseHandler;
import org.acme.pattern.pipeline.Pipeline;
import org.acme.service.auth.UtenteService;
import org.acme.service.ext.ResourceAllocationService;
import org.acme.service.pipeline.ProjectAllocationCall;
import org.acme.service.pipeline.ProjectRequestApprovalPipeline;
import org.acme.service.virtual_warehouse.VirtualWarehouseService;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jboss.resteasy.reactive.common.NotImplementedYet;
import resourceallocation.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.netty.util.internal.StringUtil.isNullOrEmpty;
import static org.acme.service.ext.ResourceAllocationService.createAllocationRequestMPMI;
import static org.acme.service.ext.ResourceAllocationService.processAllocationResponse;

@ApplicationScoped
public class ProjectRequestService{
    @Inject
    ProjectRequestDAO projectRequestDAO;
    @Inject
    InventoryItemDAO inventoryItemDAO;
    @Inject
    CommonRequestService commonRequestService;
    @Inject
    AssociationRequestService associationRequestService;
    @Inject
    ResourceAllocationService resourceAllocationService;
    @Inject
    VirtualWarehouseService virtualWarehouseService;
    @Inject
    UtenteService utenteService;

    public Map<String, List<ProjectRequestDTO>> getList(RequestFilter filter) {
        if (filter == null || filter.isEmpty()) {
            return projectRequestDAO.findAll().list()
                    .stream()
                    .map(ProjectRequestDTO::fromEntity)
                    .collect(Collectors.groupingBy(
                            projReq -> associationRequestService.findByObjectId(projReq.getAssociationReqId()).getAssociationName(),
                            Collectors.toList()
                    ));
        }

        // Build the query dynamically using a Map
        Map<String, Object> queryMap = new HashMap<>();

        addFilter(queryMap, "associationId", filter.getAssociationId(), true);
//        addFilter(queryMap, "associationConfirmed", filter.getAssociationConfirmed(), true);
        addFilter(queryMap, "status", filter.getStatus().toString(), false);


        // Date range filter
        if (filter.getDateFrom() != null && filter.getDateTo() != null) {
            queryMap.put("date", Map.of("$gte", filter.getDateFrom(), "$lte", filter.getDateTo()));
        }

        Bson bsonQuery = new Document(queryMap);

        return projectRequestDAO.find(bsonQuery).list()
                .stream()
                .map(ProjectRequestDTO::fromEntity)
                .collect(Collectors.groupingBy(
                        projReq -> associationRequestService.findByObjectId(projReq.getAssociationReqId()).getAssociationName(),
                        Collectors.toList()
                ));
    }

    public String add(ProjectRequestDTO request) {
        var req = ProjectRequestDTO.toEntity(request);

        var associationRequestId = associationRequestService.getObjectId(String.valueOf(req.getAssociationSqlId()));

        if (associationRequestId != null) {
            req.setAssociationReqId(associationRequestId);
            req.createdAt = String.valueOf(Instant.now());
            req.updatedAt = req.createdAt;
            req.status = RequestStatus.PENDING;

            var associationConfirmed = associationRequestService.checkAssociationConfirmed(req.getAssociationReqId());
            if (!associationConfirmed) {
                return null;
            }

            req.set_id(new ObjectId());

            var availabilitiesMap = virtualWarehouseService.getAvailabilityMap();

            var allocationRequest = createAllocationRequestMPMI(List.of(req), GreedyOrder.LARGEST_FIRST, GreedyStrategy.PROJECT_SIZE, availabilitiesMap);

            var response = resourceAllocationService.allocateResourcesGreedy(allocationRequest);

            SimulateRequestResponse processedRequest = processAllocationResponse(List.of(req), response);

            var id = this.persist(req);
            System.out.println("Saved Request with ID: "+ id);
            return id;
        }else{
            System.out.println("Association not verifided for inventory request: " + req.get_id().toString());
            return null;
        }
    }


    public String approveRequest(RequestCommand command) {

        var req = projectRequestDAO.findById(commonRequestService.getObjectId(command.getRequestId()));
        if (req == null) {
            return null;
        }

        var associationConfirmed = associationRequestService.checkAssociationConfirmed(req.getAssociationReqId());
        if (!associationConfirmed) {
            throw new AssociationNotConfirmedException(req.get_id().toString());
        }

        req.setStatus(RequestStatus.APPROVED);
        req.setApprovedBy(utenteService.getCurrentUtenteName());
        String now = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        req.setUpdatedAt(now);


        try{
            projectRequestDAO.persistOrUpdate(req);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return req.get_id().toString();
    }

    public String persist(ProjectRequest req) {
        projectRequestDAO.persistOrUpdate(req);
        return req.get_id().toString();
    }

    public void delete(ProjectRequest req) {
        projectRequestDAO.delete(req);
    }

    public List<ProjectRequest> getListByRequestStatus(RequestStatus requestStatus) {
        return this.projectRequestDAO.findByRequestStatus(requestStatus);
    }

    private void addFilter(Map<String, Object> queryMap, String field, String value, boolean useRegex) {
        if (!isNullOrEmpty(value)) {
            if (useRegex) {
                queryMap.put(field, Map.of("$regex", value, "$options", "i")); // Case-insensitive regex
            } else {
                queryMap.put(field, value);
            }
        }
    }

    public ProjectRequestDTO findByObjectId(String requestId) {
        var req = projectRequestDAO.findById(commonRequestService.getObjectId(requestId));
        return ProjectRequestDTO.fromEntity(req);
    }

    public List<ProjectRequestDTO> getPendingProjectRequests(String associationRequestId) {
        return this.projectRequestDAO.findPendingRequestsByAssociationID(associationRequestId)
                .stream()
                .map(ProjectRequestDTO::fromEntity)
                .toList();
    }

    public List<ProjectRequest> getListByStatus(RequestStatus requestStatus, ProjectStatus projectStatus) {
        if (projectStatus == null) {
            return this.projectRequestDAO.findByRequestStatus(requestStatus);
        }
        else{
            throw new NotImplementedYet();
        }
    }
}
