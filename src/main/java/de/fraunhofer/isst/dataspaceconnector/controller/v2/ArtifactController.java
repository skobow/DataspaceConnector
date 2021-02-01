package de.fraunhofer.isst.dataspaceconnector.controller.v2;

import de.fraunhofer.isst.dataspaceconnector.model.v2.Artifact;
import de.fraunhofer.isst.dataspaceconnector.model.v2.ArtifactDesc;
import de.fraunhofer.isst.dataspaceconnector.services.resources.v2.ArtifactService;
import de.fraunhofer.isst.dataspaceconnector.services.resources.v2.CommonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.UUID;

//@RestController
//@RequestMapping("/artifacts")
//public final class ArtifactController extends BaseResourceController<Artifact,
//        ArtifactDesc, CommonService<Artifact, ArtifactDesc>> {
//    @RequestMapping(value = "{id}/data", method = RequestMethod.GET)
//    public ResponseEntity<Object> getData(@Valid @PathVariable final UUID id) {
//        //final var data = getService().getData(id);
//        final Object data = null;
//        return ResponseEntity.ok(data);
//    }
//}