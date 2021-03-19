package de.fraunhofer.isst.dataspaceconnector.services.resources;

import java.util.List;

import de.fraunhofer.isst.dataspaceconnector.model.Contract;
import de.fraunhofer.isst.dataspaceconnector.model.OfferedResource;
import de.fraunhofer.isst.dataspaceconnector.model.RequestedResource;
import de.fraunhofer.isst.dataspaceconnector.model.Resource;
import org.springframework.stereotype.Service;

public abstract class AbstractResourceContractLinker<T extends Resource>
        extends BaseUniDirectionalLinkerService<T, Contract, ResourceService<T, ?>,
                ContractService> {
    protected AbstractResourceContractLinker() {
        super();
    }

    @Override
    protected List<Contract> getInternal(final Resource owner) {
        return owner.getContracts();
    }
}

@Service
class OfferedResourceContractLinker extends AbstractResourceContractLinker<OfferedResource> {
    public OfferedResourceContractLinker() {
        super();
    }
}

@Service
class RequestedResourceContractLinker
        extends AbstractResourceContractLinker<RequestedResource> {
    public RequestedResourceContractLinker() {
        super();
    }
}