package de.fraunhofer.isst.dataspaceconnector.services.messages;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.MalformedHeaderException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.MessageBuilderException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.MessageException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.MessageNotSentException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.MessageResponseException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.UnexpectedMessageType;
import de.fraunhofer.isst.dataspaceconnector.model.messages.MessageDesc;
import de.fraunhofer.isst.dataspaceconnector.services.ids.DeserializationService;
import de.fraunhofer.isst.dataspaceconnector.services.ids.IdsConnectorService;
import de.fraunhofer.isst.dataspaceconnector.utils.ErrorMessages;
import de.fraunhofer.isst.dataspaceconnector.utils.MessageUtils;
import de.fraunhofer.isst.ids.framework.communication.http.IDSHttpService;
import de.fraunhofer.isst.ids.framework.daps.ClaimsException;
import org.apache.commons.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * Abstract class for building, sending, and processing ids messages.
 */
public abstract class AbstractMessageService<D extends MessageDesc> {
    /**
     * The logging service.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMessageService.class);

    /**
     * Service for ids communication.
     */
    @Autowired
    private IDSHttpService idsHttpService;

    /**
     * Service for the current connector configuration.
     */
    @Autowired
    private IdsConnectorService connectorService;

    /**
     * Service for ids deserialization.
     */
    @Autowired
    private DeserializationService deserializationService;

    /**
     * Build ids message with params.
     *
     * @param recipient The message recipient.
     * @param desc      Type-specific message parameter.
     * @return An ids message.
     * @throws ConstraintViolationException If the ids message could not be built.
     */
    public abstract Message buildMessage(URI recipient, D desc) throws ConstraintViolationException;

    /**
     * Return allowed response message type.
     *
     * @return The response message type class.
     */
    protected abstract Class<?> getResponseMessageType();

    /**
     * Build and sent a multipart message with header and payload.
     *
     * @param desc    Type-specific message parameter.
     * @param payload The message's payload.
     * @return The response as map.
     * @throws MessageException If message building, sending, or processing failed.
     */
    public Map<String, String> sendMessage(final D desc, final String payload) throws MessageException {
        try {
            final var recipient = desc.getRecipient();
            final var header = buildMessage(recipient, desc);
            // Exception is handled at a higher level.
            final var body = MessageUtils.buildIdsMultipartMessage(header, payload);
            LOGGER.debug("Built request message:" + body); // TODO Add logging house class

            final var response = idsHttpService.sendAndCheckDat(body, recipient);

            validateResponse(response);

            // Send message and check response.
            return response;
        } catch (MessageResponseException e) {
            LOGGER.warn("Failed to read ids response message. [exception=({})]", e.getMessage());
            throw new MessageBuilderException("Ids message header could not be built.", e);
        } catch (ConstraintViolationException e) {
            LOGGER.warn("Ids message could not be built. [exception=({})]", e.getMessage());
            throw new MessageBuilderException("Ids message header could not be built.", e);
        } catch (ClaimsException e) {
            LOGGER.debug("Invalid DAT in incoming message. [exception=({})]", e.getMessage());
            throw new MessageResponseException("Invalid DAT in incoming message.", e);
        } catch (FileUploadException | IOException e) {
            LOGGER.warn("Message could not be sent. [exception=({})]", e.getMessage());
            throw new MessageNotSentException("Message could not be sent.", e);
        }
    }

    /**
     * Checks if the response message is of the right type.
     *
     * @param message The received message response.
     * @throws MessageResponseException If the response could not be read or the type is incorrect.
     */
    private void validateResponse(final Map<String, String> message) throws MessageResponseException {
        try {
            final var header = MessageUtils.extractHeaderFromMultipartMessage(message);
            final var idsMessage = getDeserializer().deserializeResponseMessage(header);

            final var validType = idsMessage.getClass().equals(getResponseMessageType());
            if (!validType) {
                throw new UnexpectedMessageType(ErrorMessages.UNEXPECTED_RESPONSE_TYPE.toString());
            }
        } catch (MalformedHeaderException e) {
            LOGGER.debug("Failed to read response header. [exception=({})]", e.getMessage());
            throw new MessageResponseException("Missing response header.", e);
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Failed to read response header. [exception=({})]", e.getMessage());
            throw new MessageResponseException("Malformed response header.", e);
        }
    }

    /**
     * Getter for ids connector service.
     *
     * @return The service class.
     */
    public IdsConnectorService getConnectorService() {
        return connectorService;
    }

    /**
     * Getter for ids deserialization service.
     *
     * @return The service class.
     */
    public DeserializationService getDeserializer() {
        return deserializationService;
    }
}