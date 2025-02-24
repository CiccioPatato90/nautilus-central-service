package org.acme.service.pipeline;

import jakarta.inject.Inject;
import org.acme.model.enums.projects.ProjectStatus;
import org.acme.model.enums.requests.RequestStatus;
import org.acme.model.requests.project.ProjectAllocatedResources;
import org.acme.model.requests.project.ProjectItem;
import org.acme.model.requests.project.ProjectRequest;
import org.acme.model.virtual_warehouse.item.InventoryItem;
import org.acme.pattern.Handler;
import org.acme.pattern.context.BaseTransactionContext;
import org.acme.pattern.exceptions.AssociationNotConfirmedException;
import org.acme.service.requests.AssociationRequestService;
import resourceallocation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectRequestApprovalPipeline {

    public static class CheckAssociationVerified implements Handler<ProjectRequest, ProjectRequest> {
        private BaseTransactionContext context;

        @Inject
        AssociationRequestService associationRequestService;

        @Override
        public ProjectRequest process(ProjectRequest input) {
            var associationConfirmed = associationRequestService.checkAssociationConfirmed(input.getAssociationReqId());
            if (!associationConfirmed) {
                context.setError(new AssociationNotConfirmedException(input.get_id().toString()));
            }
            return input;
        }

        @Override
        public void setContext(BaseTransactionContext context) {
            this.context = context;
        }
    }

    public static class CreateResourceAllocationRequest implements Handler<List<InventoryItem>, AllocationRequest> {
        private BaseTransactionContext context;

        @Override
        public AllocationRequest process(List<InventoryItem> input) {
//            project requirements arrive from mongoRequest saved in context
//            here I get as input the list of items available
            ProjectRequest req = context.get("projectRequest");

            Map<String, Integer> requiredItems = req.getRequiredItemsSQLId().stream()
                    .collect(Collectors.toMap(
                            item -> String.valueOf(item.getSqlId()),
                            ProjectItem::getQuantityNeeded
                    ));

            context.logStep("Calcolated Required Items.");

            List<Resource> resources = input.stream().map(inventoryItem -> Resource.newBuilder()
                            .setId(String.valueOf(inventoryItem.getId()))
                            .setName(inventoryItem.getName())
                            .setCapacity(0)
                            .setCost(10.0)
                            .build())
                    .collect(Collectors.toList());

            context.logStep("Mapping to Resources List.");

            return AllocationRequest.newBuilder().
                    addProjects(Project.newBuilder()
                            .setId(req.get_id().toString())
                            .setName(req.getProjectName())
                            .putAllRequirements(requiredItems)
                            .setPriority(1)
                            .build())
                    .addAllResources(resources)
                    .setStrategy(AllocationStrategy.newBuilder()
                            .setCriteria(GreedyCriteria.ASSOCIATION_ACTIVITY)
                            .setOrder(GreedyCriteriaOrder.LARGEST_FIRST)
                            .build())
                    .build();
        }

        @Override
        public void setContext(BaseTransactionContext context) {
            this.context = context;
        }
    }

    public static class ProcessAllocationResponseSingleProject implements Handler<AllocationResponse, ProjectRequest>{
        private BaseTransactionContext context;


        @Override
        public ProjectRequest process(AllocationResponse input) {
            ProjectRequest req = context.get("projectRequest");
            req.setAllocationId(input.getAllocationId());

            ProjectAllocatedResources allocatedResources = input.getProjectAllocationsMap().entrySet()
                    .stream()
                    .findFirst()
                    .map(entry -> {
                        ProjectAllocatedResources par = new ProjectAllocatedResources();
                        // Parse the key as the completion percentage.
                        par.setCompletionPercentage(Double.parseDouble(entry.getKey()));
                        // Convert the list of ResourceAllocation messages to a Map<resourceId, allocatedAmount>
                        Map<String, Integer> allocationMap = entry.getValue().getResourceAllocationsList()
                                .stream()
                                .collect(Collectors.toMap(
                                        ResourceAllocation::getResourceId,
                                        ResourceAllocation::getAllocatedAmount
                                ));
                        par.setAllocationMap(allocationMap);
                        return par;
                    })
                    .orElse(null);

            req.setAllocatedResources(allocatedResources);
            req.setStatus(RequestStatus.APPROVED);
            req.setProjectStatus(ProjectStatus.ALLOCATED);
            return req;
        }


        @Override
        public void setContext(BaseTransactionContext context) {
            this.context = context;
        }
    }
}
