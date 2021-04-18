package de.fraunhofer.isst.dataspaceconnector.utils;

import de.fraunhofer.isst.dataspaceconnector.exceptions.ResourceNotFoundException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.UnreachableLineException;
import de.fraunhofer.isst.dataspaceconnector.model.AbstractEntity;
import de.fraunhofer.isst.dataspaceconnector.model.Agreement;
import de.fraunhofer.isst.dataspaceconnector.model.Artifact;
import de.fraunhofer.isst.dataspaceconnector.model.Catalog;
import de.fraunhofer.isst.dataspaceconnector.model.Contract;
import de.fraunhofer.isst.dataspaceconnector.model.ContractRule;
import de.fraunhofer.isst.dataspaceconnector.model.OfferedResource;
import de.fraunhofer.isst.dataspaceconnector.model.Representation;
import de.fraunhofer.isst.dataspaceconnector.model.RequestedResource;
import de.fraunhofer.isst.dataspaceconnector.model.view.AgreementViewAssembler;
import de.fraunhofer.isst.dataspaceconnector.model.view.ArtifactViewAssembler;
import de.fraunhofer.isst.dataspaceconnector.model.view.CatalogViewAssembler;
import de.fraunhofer.isst.dataspaceconnector.model.view.ContractRuleViewAssembler;
import de.fraunhofer.isst.dataspaceconnector.model.view.ContractViewAssembler;
import de.fraunhofer.isst.dataspaceconnector.model.view.OfferedResourceViewAssembler;
import de.fraunhofer.isst.dataspaceconnector.model.view.RepresentationViewAssembler;
import de.fraunhofer.isst.dataspaceconnector.model.view.RequestedResourceViewAssembler;

import java.net.URI;
import java.util.NoSuchElementException;

/**
 * This is a helper class for retrieving self-links of a database entity.
 */
public final class SelfLinkHelper {

    /**
     * Default constructor.
     */
    private SelfLinkHelper() {
        // not used
    }

    /**
     * This function is a helper function for hiding the problem that the self-link is always
     * received through the concrete assembler.
     *
     * @param entity The entity.
     * @param <T>    Generic type of database entity.
     * @return The abstract entity.
     */
    public static <T extends AbstractEntity> URI getSelfLink(final T entity) {
        if (entity instanceof Catalog) {
            return getSelfLink((Catalog) entity);
        } else if (entity instanceof OfferedResource) {
            return getSelfLink((OfferedResource) entity);
        } else if (entity instanceof RequestedResource) {
            return getSelfLink((RequestedResource) entity);
        } else if (entity instanceof Representation) {
            return getSelfLink((Representation) entity);
        } else if (entity instanceof Artifact) {
            return getSelfLink((Artifact) entity);
        } else if (entity instanceof Contract) {
            return getSelfLink((Contract) entity);
        } else if (entity instanceof ContractRule) {
            return getSelfLink((ContractRule) entity);
        } else if (entity instanceof Agreement) {
            return getSelfLink((Agreement) entity);
        }

        throw new UnreachableLineException(ErrorMessages.UNKNOWN_TYPE);
    }

    /**
     * Get self-link of catalog.
     *
     * @param catalog The catalog.
     * @return The self-link of the catalog.
     * @throws ResourceNotFoundException If the resource could not be loaded.
     */
    public static URI getSelfLink(final Catalog catalog) throws ResourceNotFoundException {
        try {
            final var view = new CatalogViewAssembler().toModel(catalog);
            return view.getLink("self").get().toUri();
        } catch (NoSuchElementException | IllegalStateException exception) {
            throw new ResourceNotFoundException(ErrorMessages.EMTPY_ENTITY.toString(), exception);
        }
    }

    /**
     * Get self-link of offered resource.
     *
     * @param resource The offered resource.
     * @return The self-link of the offered resource.
     * @throws ResourceNotFoundException If the resource could not be loaded.
     */
    public static URI getSelfLink(final OfferedResource resource) throws ResourceNotFoundException {
        try {
            final var view = new OfferedResourceViewAssembler().toModel(resource);
            return view.getLink("self").get().toUri();
        } catch (NoSuchElementException | IllegalStateException exception) {
            throw new ResourceNotFoundException(ErrorMessages.EMTPY_ENTITY.toString(), exception);
        }
    }

    /**
     * Get self-link of requested resource.
     *
     * @param resource The requested resource.
     * @return The self-link of the requested resource.
     * @throws ResourceNotFoundException If the resource could not be loaded.
     */
    public static URI getSelfLink(final RequestedResource resource) throws ResourceNotFoundException {
        try {
            final var view = new RequestedResourceViewAssembler().toModel(resource);
            return view.getLink("self").get().toUri();
        } catch (NoSuchElementException | IllegalStateException exception) {
            throw new ResourceNotFoundException(ErrorMessages.EMTPY_ENTITY.toString(), exception);
        }
    }

    /**
     * Get self-link of representation.
     *
     * @param representation The representation.
     * @return The self-link of the representation.
     * @throws ResourceNotFoundException If the resource could not be loaded.
     */
    public static URI getSelfLink(final Representation representation) throws ResourceNotFoundException {
        try {
            final var view = new RepresentationViewAssembler().toModel(representation);
            return view.getLink("self").get().toUri();
        } catch (NoSuchElementException | IllegalStateException exception) {
            throw new ResourceNotFoundException(ErrorMessages.EMTPY_ENTITY.toString(), exception);
        }
    }

    /**
     * Get self-link of artifact.
     *
     * @param artifact The artifact.
     * @return The self-link of the artifact.
     * @throws ResourceNotFoundException If the resource could not be loaded.
     */
    public static URI getSelfLink(final Artifact artifact) throws ResourceNotFoundException {
        try {
            final var view = new ArtifactViewAssembler().toModel(artifact);
            return view.getLink("self").get().toUri();
        } catch (NoSuchElementException | IllegalStateException exception) {
            throw new ResourceNotFoundException(ErrorMessages.EMTPY_ENTITY.toString(), exception);
        }
    }

    /**
     * Get self-link of contract.
     *
     * @param contract The contract.
     * @return The self-link of the contract.
     * @throws ResourceNotFoundException If the resource could not be loaded.
     */
    public static URI getSelfLink(final Contract contract) throws ResourceNotFoundException {
        try {
            final var view = new ContractViewAssembler().toModel(contract);
            return view.getLink("self").get().toUri();
        } catch (NoSuchElementException | IllegalStateException exception) {
            throw new ResourceNotFoundException(ErrorMessages.EMTPY_ENTITY.toString(), exception);
        }
    }

    /**
     * Get self-link of rule.
     *
     * @param rule The rule.
     * @return The self-link of the rule.
     * @throws ResourceNotFoundException If the resource could not be loaded.
     */
    public static URI getSelfLink(final ContractRule rule) throws ResourceNotFoundException {
        try {
            final var view = new ContractRuleViewAssembler().toModel(rule);
            return view.getLink("self").get().toUri();
        } catch (NoSuchElementException | IllegalStateException exception) {
            throw new ResourceNotFoundException(ErrorMessages.EMTPY_ENTITY.toString(), exception);
        }
    }

    /**
     * Get self-link of agreement.
     *
     * @param agreement The agreement.
     * @return The self-link of the agreement.
     * @throws ResourceNotFoundException If the resource could not be loaded.
     */
    public static URI getSelfLink(final Agreement agreement) throws ResourceNotFoundException {
        try {
            final var view = new AgreementViewAssembler().toModel(agreement);
            return view.getLink("self").get().toUri();
        } catch (NoSuchElementException | IllegalStateException exception) {
            throw new ResourceNotFoundException(ErrorMessages.EMTPY_ENTITY.toString(), exception);
        }
    }
}