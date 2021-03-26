package de.fraunhofer.isst.dataspaceconnector.repositories;

import de.fraunhofer.isst.dataspaceconnector.model.OfferedResource;
import de.fraunhofer.isst.dataspaceconnector.model.RequestedResource;
import de.fraunhofer.isst.dataspaceconnector.model.Resource;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * The base repository for all objects of type {@link Resource}.
 * @param <T> The resource type.
 */
@NoRepositoryBean
public interface ResourceRepository<T extends Resource> extends BaseEntityRepository<T> {
}

/**
 * The repository containing all objects of type {@link OfferedResource}.
 */
@RepositoryRestResource(collectionResourceRel = "offeredresources", path = "offeredresources")
interface OfferedResourcesRepository extends ResourceRepository<OfferedResource> {
}

/**
 * The repository containing all objects of type {@link RequestedResource}.
 */
@RepositoryRestResource(collectionResourceRel = "requestedresources", path = "requestedresources")
interface RequestedResourcesRepository extends ResourceRepository<RequestedResource> {
}