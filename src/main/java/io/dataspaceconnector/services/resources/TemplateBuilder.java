/*
 * Copyright 2020 Fraunhofer Institute for Software and Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dataspaceconnector.services.resources;

import java.util.stream.Collectors;

import io.dataspaceconnector.model.Artifact;
import io.dataspaceconnector.model.Contract;
import io.dataspaceconnector.model.ContractRule;
import io.dataspaceconnector.model.OfferedResource;
import io.dataspaceconnector.model.OfferedResourceDesc;
import io.dataspaceconnector.model.Representation;
import io.dataspaceconnector.model.RequestedResource;
import io.dataspaceconnector.model.RequestedResourceDesc;
import io.dataspaceconnector.model.Resource;
import io.dataspaceconnector.model.ResourceDesc;
import io.dataspaceconnector.model.templates.ArtifactTemplate;
import io.dataspaceconnector.model.templates.ContractTemplate;
import io.dataspaceconnector.model.templates.RepresentationTemplate;
import io.dataspaceconnector.model.templates.ResourceTemplate;
import io.dataspaceconnector.model.templates.RuleTemplate;
import io.dataspaceconnector.utils.ErrorMessages;
import io.dataspaceconnector.utils.Utils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Builds and links entities from templates.
 * @param <T> The resource type.
 * @param <D> The resource description type.
 */
@RequiredArgsConstructor
public abstract class TemplateBuilder<T extends Resource, D extends ResourceDesc<T>> {

    /**
     * The service for resources.
     */
    private final @NonNull ResourceService<T, D> resourceService;

    /**
     * The linker for resource-representation relations.
     */
    private final @NonNull AbstractResourceRepresentationLinker<T> resourceRepresentationLinker;

    /**
     * The linker for resource-contract relations.
     */
    private final @NonNull AbstractResourceContractLinker<T> resourceContractLinker;

    /**
     * The service for representations.
     */
    private final @NonNull RepresentationService representationService;

    /**
     * The linker for representation-artifact relations.
     */
    private final @NonNull
    RelationServices.RepresentationArtifactLinker representationArtifactLinker;

    /**
     * The service for contracts.
     */
    private final @NonNull ContractService contractService;

    /**
     * The linker for contract-rule relations.
     */
    private final @NonNull RelationServices.ContractRuleLinker contractRuleLinker;

    /**
     * The service for artifacts.
     */
    @Autowired
    private ArtifactService artifactService;

    /**
     * The service for rules.
     */
    @Autowired
    private RuleService ruleService;

    /**
     * Build a resource and dependencies from a template.
     * @param template The resource template.
     * @return The new resource.
     * @throws IllegalArgumentException if the passed template is null.
     */
    public T build(final ResourceTemplate<D> template) {
        Utils.requireNonNull(template, ErrorMessages.ENTITY_NULL);

        final var representationIds =
                Utils.toStream(template.getRepresentations()).map(x -> build(x).getId())
                     .collect(Collectors.toSet());
        final var contractIds = Utils.toStream(template.getContracts()).map(x -> build(x).getId())
                                     .collect(Collectors.toSet());
        final var resource = buildResource(template);

        resourceRepresentationLinker.add(resource.getId(), representationIds);
        resourceContractLinker.add(resource.getId(), contractIds);

        return resource;
    }

    /**
     * Creates a resource from a resource template.
     *
     * @param template the template.
     * @return the resource.
     */
    protected abstract T buildResource(ResourceTemplate<D> template);

    /**
     * Build a representation and dependencies from template.
     * @param template The representation template.
     * @return The new representation.
     * @throws IllegalArgumentException if the passed template is null.
     */
    public Representation build(final RepresentationTemplate template) {
        Utils.requireNonNull(template, ErrorMessages.ENTITY_NULL);

        final var artifactIds = Utils.toStream(template.getArtifacts()).map(x -> build(x).getId())
                                     .collect(Collectors.toSet());
        Representation representation;
        final var repId =
                representationService.identifyByRemoteId(template.getDesc().getRemoteId());
        if (repId.isPresent()) {
            representation = representationService.update(repId.get(), template.getDesc());
        } else {
            representation = representationService.create(template.getDesc());
        }

        representationArtifactLinker.add(representation.getId(), artifactIds);

        return representation;
    }

    /**
     * Build a contract and dependencies from a template.
     * @param template The contract template.
     * @return The new contract.
     * @throws IllegalArgumentException if the passed template is null.
     */
    public Contract build(final ContractTemplate template) {
        Utils.requireNonNull(template, ErrorMessages.ENTITY_NULL);

        final var ruleIds = Utils.toStream(template.getRules()).map(x -> build(x).getId())
                                 .collect(Collectors.toSet());
        final var contract = contractService.create(template.getDesc());
        contractRuleLinker.add(contract.getId(), ruleIds);

        return contract;
    }

    /**
     * Build an artifact and dependencies from a template.
     * @param template The artifact template.
     * @return The new artifact.
     * @throws IllegalArgumentException if the passed template is null.
     */
    public Artifact build(final ArtifactTemplate template) {
        Utils.requireNonNull(template, ErrorMessages.ENTITY_NULL);

        Artifact artifact;
        final var contractId = artifactService.identifyByRemoteId(template.getDesc().getRemoteId());
        if (contractId.isPresent()) {
            artifact = artifactService.update(contractId.get(), template.getDesc());
        } else {
            artifact = artifactService.create(template.getDesc());
        }

        return artifact;
    }

    /**
     * Build a rule and dependencies from a template.
     * @param template The rule template.
     * @return The new rule.
     * @throws IllegalArgumentException if the passed template is null.
     */
    public ContractRule build(final RuleTemplate template) {
        Utils.requireNonNull(template, ErrorMessages.ENTITY_NULL);
        return ruleService.create(template.getDesc());
    }

    /**
     * Return the resource service for subclasses.
     * @return The resource service.
     */
    protected ResourceService<T, D> getResourceService() {
        return resourceService;
    }
}


/**
 * Template builder for offered resources.
 */
@Service
final class TemplateBuilderOfferedResource
        extends TemplateBuilder<OfferedResource, OfferedResourceDesc> {
    /**
     * Default constructor.
     * @param resourceService              The resource service.
     * @param resourceRepresentationLinker The resource-representation service.
     * @param resourceContractLinker       The resource-contract service.
     * @param representationService        The representation service.
     * @param representationArtifactLinker The representation-artifact service.
     * @param contractService              The contract service.
     * @param contractRuleLinker           The contract-rule service.
     */
    @Autowired
    TemplateBuilderOfferedResource(
            final ResourceService<OfferedResource, OfferedResourceDesc> resourceService,
            final AbstractResourceRepresentationLinker<OfferedResource>
                    resourceRepresentationLinker,
            final AbstractResourceContractLinker<OfferedResource> resourceContractLinker,
            final RepresentationService representationService,
            final RelationServices.RepresentationArtifactLinker representationArtifactLinker,
            final ContractService contractService,
            final RelationServices.ContractRuleLinker contractRuleLinker) {
        super(resourceService, resourceRepresentationLinker, resourceContractLinker,
              representationService, representationArtifactLinker, contractService,
              contractRuleLinker);
    }

    @Override
    protected OfferedResource buildResource(final ResourceTemplate<OfferedResourceDesc> template) {
        return getResourceService().create(template.getDesc());
    }
}


/**
 * Template builder for requested resources.
 */
@Service
final class TemplateBuilderRequestedResource
        extends TemplateBuilder<RequestedResource, RequestedResourceDesc> {
    /**
     * Default constructor.
     * @param resourceService              The resource service.
     * @param resourceRepresentationLinker The resource-representation service.
     * @param resourceContractLinker       The resource-contract service.
     * @param representationService        The representation service.
     * @param representationArtifactLinker The representation-artifact service.
     * @param contractService              The contract service.
     * @param contractRuleLinker           The contract-rule service.
     */
    @Autowired
    TemplateBuilderRequestedResource(
            final ResourceService<RequestedResource, RequestedResourceDesc> resourceService,
            final AbstractResourceRepresentationLinker<RequestedResource>
                    resourceRepresentationLinker,
            final AbstractResourceContractLinker<RequestedResource> resourceContractLinker,
            final RepresentationService representationService,
            final RelationServices.RepresentationArtifactLinker representationArtifactLinker,
            final ContractService contractService,
            final RelationServices.ContractRuleLinker contractRuleLinker) {
        super(resourceService, resourceRepresentationLinker, resourceContractLinker,
              representationService, representationArtifactLinker, contractService,
              contractRuleLinker);
    }

    @Override
    protected RequestedResource buildResource(
            final ResourceTemplate<RequestedResourceDesc> template) {
        final var resourceService = getResourceService();

        RequestedResource resource;
        if (resourceService instanceof RemoteResolver) {
            final var resourceId = ((RemoteResolver) resourceService)
                    .identifyByRemoteId(template.getOldRemoteId());
            if (resourceId.isPresent()) {
                if (template.getOldRemoteId().equals(template.getDesc().getRemoteId())) {
                    resource = resourceService.update(resourceId.get(), template.getDesc());
                } else {
                    final var doesExist = ((RemoteResolver) resourceService)
                            .identifyByRemoteId(template.getDesc().getRemoteId()).isPresent();
                    if (doesExist) {
                        throw new IllegalStateException();
                    } else {
                        resource = resourceService.update(resourceId.get(), template.getDesc());
                    }
                }

            } else {
                resource = resourceService.create(template.getDesc());
            }
        } else {
            resource = resourceService.create(template.getDesc());
        }

        return resource;
    }
}
