package de.fraunhofer.isst.dataspaceconnector.repositories.v2.implementations;

import de.fraunhofer.isst.dataspaceconnector.model.v2.Resource;
import de.fraunhofer.isst.dataspaceconnector.repositories.v2.BaseResourceRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends BaseResourceRepository<Resource> {
}