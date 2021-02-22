package de.fraunhofer.isst.dataspaceconnector.services.resources.v2.backendtofrontend;

import de.fraunhofer.isst.dataspaceconnector.model.Artifact;
import de.fraunhofer.isst.dataspaceconnector.model.ArtifactDesc;
import de.fraunhofer.isst.dataspaceconnector.model.EndpointId;
import de.fraunhofer.isst.dataspaceconnector.model.QueryInput;
import de.fraunhofer.isst.dataspaceconnector.model.view.ArtifactView;
import de.fraunhofer.isst.dataspaceconnector.services.resources.v2.backend.ArtifactService;
import org.springframework.stereotype.Service;

@Service
public final class ArtifactBFFService extends CommonService<Artifact, ArtifactDesc,
        ArtifactView> {
    public Object getData(final EndpointId endpointId) {
        final var service = (ArtifactService)getService();
        return service.getData(getEndpointService().get(endpointId).getInternalId());
    }

    public Object getData(final EndpointId endpointId, final QueryInput queryInput) {
        final var service = (ArtifactService)getService();
        return service.getData(getEndpointService().get(endpointId).getInternalId());
    }

    public void saveData(final EndpointId endpointId, String data) {
        // final var service = (ArtifactService)getService();
        // TODO save the data
        throw new RuntimeException();
    }
}