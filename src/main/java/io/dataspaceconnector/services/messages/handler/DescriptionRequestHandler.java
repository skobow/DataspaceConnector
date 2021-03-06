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
package io.dataspaceconnector.services.messages.handler;

import de.fraunhofer.iais.eis.DescriptionRequestMessageImpl;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import io.dataspaceconnector.exceptions.InvalidResourceException;
import io.dataspaceconnector.exceptions.MessageBuilderException;
import io.dataspaceconnector.exceptions.MessageEmptyException;
import io.dataspaceconnector.exceptions.ResourceNotFoundException;
import io.dataspaceconnector.exceptions.SelfLinkCreationException;
import io.dataspaceconnector.exceptions.VersionNotSupportedException;
import io.dataspaceconnector.model.messages.DescriptionResponseMessageDesc;
import io.dataspaceconnector.services.EntityResolver;
import io.dataspaceconnector.services.ids.ConnectorService;
import io.dataspaceconnector.services.messages.MessageResponseService;
import io.dataspaceconnector.services.messages.types.DescriptionResponseService;
import io.dataspaceconnector.utils.ErrorMessages;
import io.dataspaceconnector.utils.MessageUtils;
import de.fraunhofer.isst.ids.framework.messaging.model.messages.MessageHandler;
import de.fraunhofer.isst.ids.framework.messaging.model.messages.MessagePayload;
import de.fraunhofer.isst.ids.framework.messaging.model.messages.SupportedMessageType;
import de.fraunhofer.isst.ids.framework.messaging.model.responses.BodyResponse;
import de.fraunhofer.isst.ids.framework.messaging.model.responses.MessageResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
     * Service for handling response messages.
     */
    private final @NonNull DescriptionResponseService messageService;

    /**
     * Service for building and sending message responses.
     */
    private final @NonNull MessageResponseService responseService;

    /**
     * Service for the current connector configuration.
     */
    private final @NonNull ConnectorService connectorService;

    /**
     * Service for resolving entities.
     */
    private final @NonNull EntityResolver entityResolver;

    /**
     * This message implements the logic that is needed to handle the message. As it just returns
     * the input as string the messagePayload-InputStream is converted to a String.
     *
     * @param message The ids request message as header.
     * @param payload The request message payload.
     * @return The response message.
     */
    @Override
    public MessageResponse handleMessage(final DescriptionRequestMessageImpl message,
                                         final MessagePayload payload) {
        // Validate incoming message.
        try {
            messageService.validateIncomingMessage(message);
        } catch (MessageEmptyException exception) {
            return responseService.handleMessageEmptyException(exception);
        } catch (VersionNotSupportedException exception) {
            return responseService.handleInfoModelNotSupportedException(exception,
                    message.getModelVersion());
        }

        // Read relevant parameters for message processing.
        final var requested = MessageUtils.extractRequestedElement(message);
        final var issuer = MessageUtils.extractIssuerConnector(message);
        final var messageId = MessageUtils.extractMessageId(message);

        // Check if a specific resource has been requested.
        if (requested == null) {
            return constructSelfDescription(issuer, messageId);
        } else {
            return constructResourceDescription(requested, issuer, messageId);
        }
    }

    /**
     * Constructs the response message for a given resource description request message.
     *
     * @param requested The requested element.
     * @param issuer    The issuer connector extracted from the incoming message.
     * @param messageId The message id of the incoming message.
     * @return The response message to the passed request.
     */
    public MessageResponse constructResourceDescription(final URI requested,
                                                        final URI issuer,
                                                        final URI messageId) {
        try {
            final var entity = entityResolver.getEntityById(requested);

            if (entity == null) {
                throw new ResourceNotFoundException(ErrorMessages.EMTPY_ENTITY.toString());
            } else {
                // If the element has been found, build the ids response message.
                final var desc = new DescriptionResponseMessageDesc(issuer, messageId);
                final var header = messageService.buildMessage(desc);
                final var payload = entityResolver.getEntityAsRdfString(entity);

                // Send ids response message.
                return BodyResponse.create(header, payload);
            }
        } catch (ResourceNotFoundException | InvalidResourceException e) {
            return responseService.handleResourceNotFoundException(e, requested, issuer, messageId);
        } catch (MessageBuilderException | IllegalStateException | ConstraintViolationException e) {
            return responseService.handleResponseMessageBuilderException(e, issuer, messageId);
        } catch (SelfLinkCreationException exception) {
            return responseService.handleSelfLinkCreationException(exception, requested);
        }
    }

    /**
     * Constructs a resource catalog description message for the connector.
     *
     * @param issuer    The issuer connector extracted from the incoming message.
     * @param messageId The message id of the incoming message.
     * @return A response message containing the resource catalog of the connector.
     */
    public MessageResponse constructSelfDescription(final URI issuer, final URI messageId) {
        try {
            // Get self-description.
            // TODO Only return contract offers that have no or the right pre-defined consumer
            final var connector = connectorService.getConnectorWithOfferedResources();

            // Build ids response message.
            final var desc = new DescriptionResponseMessageDesc(issuer, messageId);
            final var header = messageService.buildMessage(desc);

            // Send ids response message.
            return BodyResponse.create(header, connector.toRdf());
        } catch (MessageBuilderException | IllegalStateException | ConstraintViolationException e) {
            return responseService.handleResponseMessageBuilderException(e, issuer, messageId);
        }
    }
}
