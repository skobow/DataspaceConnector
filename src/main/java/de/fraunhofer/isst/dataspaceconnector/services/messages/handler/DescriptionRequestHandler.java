package de.fraunhofer.isst.dataspaceconnector.services.messages.handler;

import de.fraunhofer.iais.eis.DescriptionRequestMessageImpl;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.InvalidResourceException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.controller.ResourceNotFoundException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.handled.MessageBuilderException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.handled.MessageEmptyException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.handled.VersionNotSupportedException;
import de.fraunhofer.isst.dataspaceconnector.model.messages.DescriptionResponseDesc;
import de.fraunhofer.isst.dataspaceconnector.services.EntityResolver;
import de.fraunhofer.isst.dataspaceconnector.services.ids.IdsConnectorService;
import de.fraunhofer.isst.dataspaceconnector.services.messages.DescriptionResponseService;
import de.fraunhofer.isst.dataspaceconnector.services.messages.MessageExceptionService;
import de.fraunhofer.isst.dataspaceconnector.utils.IdsUtils;
import de.fraunhofer.isst.dataspaceconnector.utils.MessageUtils;
import de.fraunhofer.isst.ids.framework.messaging.model.messages.MessageHandler;
import de.fraunhofer.isst.ids.framework.messaging.model.messages.MessagePayload;
import de.fraunhofer.isst.ids.framework.messaging.model.messages.SupportedMessageType;
import de.fraunhofer.isst.ids.framework.messaging.model.responses.BodyResponse;
import de.fraunhofer.isst.ids.framework.messaging.model.responses.MessageResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * This @{@link DescriptionRequestHandler} handles all incoming messages that have a
 * {@link de.fraunhofer.iais.eis.DescriptionRequestMessageImpl} as part one in the multipart
 * message. This header must have the correct '@type' reference as defined in the
 * {@link de.fraunhofer.iais.eis.DescriptionRequestMessageImpl} JsonTypeName annotation.
 */
@Component
@RequiredArgsConstructor
@SupportedMessageType(DescriptionRequestMessageImpl.class)
public class DescriptionRequestHandler implements MessageHandler<DescriptionRequestMessageImpl> {

    /**
     * Class level logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(DescriptionRequestHandler.class);

    /**
     * Service for handling response messages.
     */
    private final @NonNull DescriptionResponseService messageService;

    /**
     * Service for the message exception handling.
     */
    private final @NonNull MessageExceptionService exceptionService;

    /**
     * Service for the current connector configuration.
     */
    private final @NonNull IdsConnectorService connectorService;

    /**
     * Service for resolving entities.
     */
    private final @NonNull EntityResolver entityResolver;

    /**
     * This message implements the logic that is needed to handle the message. As it just returns
     * the input as string the messagePayload-InputStream is converted to a String.
     *
     * @param message The ids request message as header.
     * @param payload The request message payload
     * @return The message response
     * @throws RuntimeException if the response body failed to be build or requestMessage is null.
     */
    @Override
    public MessageResponse handleMessage(final DescriptionRequestMessageImpl message,
                                         final MessagePayload payload) {
        // Validate incoming message.
        try {
            MessageUtils.checkForEmptyMessage(message);
            exceptionService.checkForVersionSupport(message.getModelVersion());
        } catch (MessageEmptyException exception) {
            return exceptionService.handleMessageEmptyException(exception);
        } catch (VersionNotSupportedException exception) {
            return exceptionService.handleInfoModelNotSupportedException(exception,
                    message.getModelVersion());
        }

        // Read relevant parameters for message processing.
        final var requestedElement = MessageUtils.extractRequestedElementFromMessage(message);
        final var issuerConnector = MessageUtils.extractIssuerConnectorFromMessage(message);
        final var messageId = MessageUtils.extractMessageIdFromMessage(message);

        MessageResponse response;
        // Check if a specific resource has been requested.
        if (requestedElement == null) {
            response = constructConnectorSelfDescription(issuerConnector, messageId);
        } else {
            response = constructResourceDescription(requestedElement, issuerConnector, messageId);
        }

        return response;
    }

    /**
     * Constructs the response message for a given resource description request message.
     *
     * @param requestedElement The requested element.
     * @param issuerConnector  The issuer connector extracted from the incoming message.
     * @param messageId        The message id of the incoming message.
     * @return The response message to the passed request.
     */
    public MessageResponse constructResourceDescription(final URI requestedElement,
                                                        final URI issuerConnector,
                                                        final URI messageId) {
        try {
            final var entity = entityResolver.getEntityById(requestedElement);

            if (entity == null) {
                return exceptionService.handleResourceNotFoundException(requestedElement,
                        issuerConnector, messageId);
            } else {
                // If the element has been found, build the ids response message.
                final var params = new DescriptionResponseDesc(messageId);
                final var header = messageService.buildMessage(issuerConnector, params);
                final var payload = entityResolver.getEntityAsIdsRdfString(entity);

                // Send ids response message.
                return BodyResponse.create(header, payload);
            }
        } catch (ResourceNotFoundException exception) {
            return exceptionService.handleResourceNotFoundException(exception, requestedElement,
                    issuerConnector, messageId);
        } catch (InvalidResourceException exception) {
            return exceptionService.handleInvalidResourceException(exception, requestedElement,
                    issuerConnector, messageId);
        } catch (MessageBuilderException exception) {
            return exceptionService.handleResponseMessageBuilderException(exception);
        } catch (IllegalStateException exception) {
            return exceptionService.handleIllegalStateException(exception);
        } catch (ConstraintViolationException exception) {
            return exceptionService.handleConstraintViolationException(exception);
        }
    }

    /**
     * Constructs a resource catalog description message for the connector.
     *
     * @param issuerConnector The issuer connector extracted from the incoming message.
     * @param messageId       The message id of the incoming message.
     * @return A response message containing the resource catalog of the connector.
     */
    public MessageResponse constructConnectorSelfDescription(final URI issuerConnector,
                                                             final URI messageId) {
        try {
            // Get self-description.
            final var selfDescription = connectorService.getConnectorWithOfferedResources();

            // Build ids response message.
            final var desc = new DescriptionResponseDesc(messageId);
            final var header = messageService.buildMessage(issuerConnector, desc);
            final var payload = IdsUtils.convertConnectorToRdf(selfDescription);

            // Send ids response message.
            return BodyResponse.create(header, payload);
        } catch (ConstraintViolationException exception) {
            return exceptionService.handleConstraintViolationException(exception);
        } catch (MessageBuilderException exception) {
            return exceptionService.handleResponseMessageBuilderException(exception);
        } catch (IllegalStateException exception) {
            return exceptionService.handleIllegalStateException(exception);
        }
    }
}
