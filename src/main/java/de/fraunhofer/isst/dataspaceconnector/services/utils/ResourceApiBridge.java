package de.fraunhofer.isst.dataspaceconnector.services.utils;

import de.fraunhofer.isst.dataspaceconnector.model.v1.BackendSource;
import de.fraunhofer.isst.dataspaceconnector.model.v1.ResourceMetadata;
import de.fraunhofer.isst.dataspaceconnector.model.v1.ResourceRepresentation;
import de.fraunhofer.isst.dataspaceconnector.model.v2.ArtifactDesc;
import de.fraunhofer.isst.dataspaceconnector.model.v2.ContractDesc;
import de.fraunhofer.isst.dataspaceconnector.model.v2.ContractRuleDesc;
import de.fraunhofer.isst.dataspaceconnector.model.v2.RepresentationDesc;
import de.fraunhofer.isst.dataspaceconnector.model.v2.ResourceDesc;
import de.fraunhofer.isst.dataspaceconnector.model.v2.templates.ArtifactTemplate;
import de.fraunhofer.isst.dataspaceconnector.model.v2.templates.ContractTemplate;
import de.fraunhofer.isst.dataspaceconnector.model.v2.templates.RepresentationTemplate;
import de.fraunhofer.isst.dataspaceconnector.model.v2.templates.ResourceTemplate;
import de.fraunhofer.isst.dataspaceconnector.model.v2.templates.RuleTemplate;

import java.util.ArrayList;
import java.util.UUID;

public final class ResourceApiBridge {
    private ResourceApiBridge(){
        // Nothing
    }

    public static ResourceTemplate toResourceTemplate(final UUID uuid, final ResourceMetadata resourceMetadata) {
        final var template = new ResourceTemplate();
        template.setDesc(toResourceDesc(uuid, resourceMetadata));
        template.setRepresentations(new ArrayList<>());
        template.setContracts(new ArrayList<>());

        if(resourceMetadata.getRepresentations() != null) {
            for (final var representation : resourceMetadata.getRepresentations().values()) {
                template.getRepresentations().add(toRepresentationTemplate(representation));
            }
        }

        if(resourceMetadata.getPolicy() != null) {
            template.getContracts().add(toContractTemplate(resourceMetadata.getPolicy()));
        }

        return template;
    }

    public static RepresentationTemplate toRepresentationTemplate(final ResourceRepresentation representation) {
        final var template = new RepresentationTemplate();
        template.setDesc(toRepresentationDesc(representation));
        template.setArtifacts(new ArrayList<>());

        if(representation.getSource() != null) {
            template.getArtifacts().add(toArtifactTemplate(representation.getSource()));
        }

        return template;
    }

    public static ArtifactTemplate toArtifactTemplate(final BackendSource backendSource) {
        final var template = new ArtifactTemplate();
        template.setDesc(toArtifactDesc(backendSource));

        return template;
    }

    public static ContractTemplate toContractTemplate(final String policy) {
        final var template = new ContractTemplate();
        template.setDesc(toContractDesc());
        template.setRules(new ArrayList<>());

        if(policy != null) {
            template.getRules().add(toRuleTemplate(policy));
        }

        return template;
    }

    public static RuleTemplate toRuleTemplate(final String policy) {
        final var template = new RuleTemplate();
        template.setDesc(toRuleDesc(policy));

        return template;
    }

    public static ResourceDesc toResourceDesc(final UUID uuid, final ResourceMetadata resourceMetadata) {
        final var desc = new ResourceDesc();
        desc.setTitle(resourceMetadata.getTitle());
        desc.setDescription(resourceMetadata.getDescription());
        desc.setKeywords(resourceMetadata.getKeywords());
        desc.setLanguage(null);
        desc.setLicence(resourceMetadata.getLicense());
        desc.setStaticId(uuid);
        desc.setPublisher(resourceMetadata.getOwner());

        return desc;
    }

    public static RepresentationDesc toRepresentationDesc(final ResourceRepresentation representation) {
        final var desc = new RepresentationDesc();
        desc.setTitle(representation.getName());
        desc.setLanguage(null);
        desc.setStaticId(representation.getUuid());
        desc.setType(representation.getType());

        return desc;
    }

    public static ContractDesc toContractDesc() {
        final var desc = new ContractDesc();
        desc.setTitle(null);
        desc.setStaticId(null);

        return desc;
    }

    public static ArtifactDesc toArtifactDesc(final BackendSource backendSource) {
        final var desc = new ArtifactDesc();
        desc.setTitle(null);
        //desc.setAccessUrl(backendSource.getUrl());
        desc.setUsername(backendSource.getUsername());
        desc.setPassword(backendSource.getPassword());

        return desc;
    }

    public static ContractRuleDesc toRuleDesc(final String policy) {
        final var desc = new ContractRuleDesc();
        desc.setTitle(null);
        desc.setStaticId(null);
        desc.setRule(policy);

        return desc;
    }
}