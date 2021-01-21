package de.fraunhofer.isst.dataspaceconnector.services.messages;

import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.ContractAgreement;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.ContractRequest;
import de.fraunhofer.iais.eis.ContractRequestImpl;
import de.fraunhofer.iais.eis.DutyImpl;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.PermissionImpl;
import de.fraunhofer.iais.eis.ProhibitionImpl;
import de.fraunhofer.iais.eis.Rule;
import de.fraunhofer.isst.dataspaceconnector.exceptions.RequestFormatException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.contract.ContractException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.contract.UnsupportedPatternException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.message.MessageException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.message.MessageNotSentException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.message.MessageResponseException;
import de.fraunhofer.isst.dataspaceconnector.services.messages.implementation.ContractMessageService;
import de.fraunhofer.isst.dataspaceconnector.services.usagecontrol.PolicyHandler;
import de.fraunhofer.isst.ids.framework.configuration.ConfigurationContainer;
import de.fraunhofer.isst.ids.framework.configuration.SerializerProvider;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class NegotiationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NegotiationService.class);

    private final PolicyHandler policyHandler;
    private final ContractMessageService messageService;
    private final SerializerProvider serializerProvider;
    private final ConfigurationContainer configurationContainer;

    private boolean status;

    @Autowired
    public NegotiationService(ContractMessageService contractMessageService,
        PolicyHandler policyHandler, SerializerProvider serializerProvider,
        ConfigurationContainer configurationContainer)
        throws IllegalArgumentException {
        if (contractMessageService == null)
            throw new IllegalArgumentException("The ContractMessageService cannot be null.");

        if (policyHandler == null)
            throw new IllegalArgumentException("The PolicyHandler cannot be null.");

        if (serializerProvider == null)
            throw new IllegalArgumentException("The SerializerProvider cannot be null.");

        if (configurationContainer == null)
            throw new IllegalArgumentException("The ConfigurationContainer cannot be null.");

        this.messageService = contractMessageService;
        this.policyHandler = policyHandler;
        this.status = true;
        this.serializerProvider = serializerProvider;
        this.configurationContainer = configurationContainer;
    }

    /**
     * Deserialize contract and send it as contract request message.
     *
     * @return The http response.
     * @throws IllegalArgumentException - if the contract could not be deserialized.
     */
    public ContractRequest buildContractRequest(String contractAsString, URI artifactId)
        throws IllegalArgumentException {
        Contract contract;
        try {
            // Validate contract input.
            contract = policyHandler.validateContract(contractAsString);
        } catch (RequestFormatException exception) {
            LOGGER.warn("Could not deserialize contract. [exception=({})]",
                exception.getMessage());
            throw new RequestFormatException("Malformed contract. " + exception.getMessage());
        }

        // Get a local copy of the current connector.
        var connector = configurationContainer.getConnector();

        // Build contract request. TODO: Change to curator or maintainer?
        return fillContract(artifactId, connector.getId(),
            messageService.buildContractRequest(contract));
    }

    /**
    * Check if the contract request has been successful. If yes, send a contract agreement message.
    *
    * @return The contract agreement id.
    * @throws ContractException - if the contract could not be read.
    * @throws MessageException - if the contract request message could not be sent.
    */
    public URI contractAccepted(URI recipient, String header, String payload) throws ContractException,
        MessageException {
        if (payload != null && !payload.equals("")) {
            Contract contract;
            try {
                // Validate received contract.
                contract = policyHandler.validateContract(payload);
            } catch (UnsupportedPatternException exception) {
                LOGGER.warn("Could not deserialize contract. [exception=({})]",
                    exception.getMessage());
                throw new UnsupportedPatternException("Malformed contract. " + exception.getMessage());
            }

            Map<String, String> response;
            try {
                // Get correlation message.
                URI correlationMessage;
                try {
                    ContractAgreementMessage message = serializerProvider.getSerializer()
                        .deserialize(header, ContractAgreementMessage.class);
                    correlationMessage = message.getCorrelationMessage();
                } catch (IOException exception) {
                    throw new MessageResponseException("Could not read contract agreement.");
                }

                // Send ContractAgreementMessage to recipient.
                messageService.setResponseParameters(recipient, correlationMessage, contract.getId());
                ContractAgreement agreement = messageService.buildContractAgreement(contract);
                response = messageService.sendMessage(agreement.toRdf());
            } catch (MessageException exception) {
                // Failed to send a contract agreement message.
                LOGGER.warn("Could not send contract agreement message. [exception=({})]",
                    exception.getMessage());
                throw new MessageNotSentException("Could not send contract agreement message. "
                    + exception.getMessage());
            }

            if (response != null) {
                return contract.getId();
            } else {
                // Failed to read the contract response message.
                LOGGER.info("Received invalid ids response.");
                throw new MessageResponseException("Failed to read the ids response message.");
            }
        } else {
            // Failed to read the contract response message.
            LOGGER.info("Received no valid contract.");
            throw new MessageResponseException("Failed to read the ids response message.");
        }
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    /**
     * Add artifact id to every rule in this contract.
     *
     * @return A valid contract request.
     */
    private ContractRequest fillContract(URI artifactId, URI consumer, ContractRequest contract) {
        ContractRequestImpl request = (ContractRequestImpl) contract;

        final var obligations = request.getObligation();
        final var permissions = request.getPermission();
        final var prohibitions = request.getProhibition();

        if (obligations != null && !obligations.isEmpty()) {
            for (Rule r : obligations)
                ((DutyImpl) r).setTarget(artifactId);
        }

        if (permissions != null && !permissions.isEmpty()) {
            for (Rule r : permissions)
                ((PermissionImpl) r).setTarget(artifactId);
        }

        if (prohibitions != null && !prohibitions.isEmpty()) {
            for (Rule r : prohibitions)
                ((ProhibitionImpl) r).setTarget(artifactId);
        }

        // Add consumer to contract.
        request.setConsumer(consumer);
        return request;
    }

    /**
     * Compare the contracts to each other.
     *
     * @return True is the content is equal, false if any difference is detected.
     */
    public boolean compareContracts(Contract request, Contract offer) {
        if (request == null || offer == null)
            return false;

        boolean permissions;
        if (request.getPermission() != null && offer.getPermission() != null) {
            permissions = comparePermissions(request.getPermission(), offer.getPermission());
        } else {
            permissions = true;
        }

        boolean prohibitions;
        if (request.getProhibition() != null && offer.getProhibition() != null) {
            prohibitions = compareRules(request.getProhibition(), offer.getProhibition());
        } else {
            prohibitions = true;
        }

        boolean obligations;
        if (request.getObligation() != null && offer.getObligation() != null) {
            obligations = compareRules(request.getObligation(), offer.getObligation());
        } else {
            obligations = true;
        }

        return permissions && prohibitions && obligations;
    }

    /**
     * Compares the content of two permissions.
     *
     * @return True is the content is equal, false if any difference is detected.
     */
    private boolean comparePermissions(
        ArrayList<? extends Permission> request, ArrayList<? extends Permission> offer) {
        if (request.size() != offer.size())
            return false;

        for (int i = 0; i < request.size(); i++) {
            final var requestPermission = request.get(i);
            final var offerPermission = offer.get(i);

            if (requestPermission.getPostDuty() != null && offerPermission.getPostDuty() != null
                && requestPermission.getPostDuty().size() > 0 && offerPermission.getPostDuty().size() > 0) {
                for (int j = 0; j < requestPermission.getPostDuty().size(); j++) {
                    if (!requestPermission.getPostDuty().get(j).toRdf()
                        .equals(offerPermission.getPostDuty().get(j).toRdf()))
                        return false;
                }
            }

            if (requestPermission.getPreDuty() != null && offerPermission.getPreDuty() != null
                && requestPermission.getPreDuty().size() > 0 && offerPermission.getPreDuty().size() > 0) {
                for (int j = 0; j < requestPermission.getPreDuty().size(); j++) {
                    if (!requestPermission.getPreDuty().get(j).toRdf()
                        .equals(offerPermission.getPreDuty().get(j).toRdf()))
                        return false;
                }
            }

            if (requestPermission.getConstraint() != null && offerPermission.getConstraint() != null
                && requestPermission.getConstraint().size() > 0 && offerPermission.getConstraint().size() > 0) {
                for (int j = 0; j < requestPermission.getConstraint().size(); j++) {
                    if (!requestPermission.getConstraint().get(j).toRdf()
                        .equals(offerPermission.getConstraint().get(j).toRdf()))
                        return false;
                }
            }

            if (!requestPermission.getAction().get(0).toRdf()
                .equals(offerPermission.getAction().get(0).toRdf()))
                return false;
        }

        return true;
    }

    /**
     * Compares the content of two prohibitions or obligations.
     *
     * @return True is the content is equal, false if any difference is detected.
     */
    private boolean compareRules(
        ArrayList<? extends Rule> request, ArrayList<? extends Rule> offer) {
        if (request.size() != offer.size())
            return false;

        for (int i = 0; i < request.size(); i++) {
            final var requestPermission = request.get(i);
            final var offerPermission = offer.get(i);

            if (requestPermission.getConstraint() != null && offerPermission.getConstraint() != null
                && requestPermission.getConstraint().size() > 0 && offerPermission.getConstraint().size() > 0) {
                for (int j = 0; j < requestPermission.getConstraint().size(); j++) {
                    if (!requestPermission.getConstraint().get(j).toRdf()
                        .equals(offerPermission.getConstraint().get(j).toRdf()))
                        return false;
                }
            }

            if (!requestPermission.getAction().get(0).toRdf()
                .equals(offerPermission.getAction().get(0).toRdf()))
                return false;
        }

        return true;
    }
}