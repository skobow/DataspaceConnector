package de.fraunhofer.isst.dataspaceconnector.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

/**
 * Contains utility methods for creating ResponseEntities with different status codes and custom
 * messages or exceptions.
 */
public final class ControllerUtils {

    private ControllerUtils() {
        // not used
    }

    /**
     * Class level logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerUtils.class);

    /**
     * Creates a ResponseEntity with status code 500 and a message indicating that an error occurred
     * in the ids communication.
     *
     * @param exception Exception that was thrown during communication.
     * @return ResponseEntity with status code 500.
     */
    public static ResponseEntity<Object> respondIdsMessageFailed(final Exception exception) {
        LOGGER.debug("Ids message handling failed. [exception=({})]", exception.getMessage());
        return new ResponseEntity<>("Ids message handling failed. " + exception.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Creates a ResponseEntity with status code 500 and a message indicating that an error occurred
     * in the ids communication.
     *
     * @param exception Exception that was thrown during communication.
     * @return ResponseEntity with status code 500.
     */
    public static ResponseEntity<Object> respondReceivedInvalidResponse(final Exception exception) {
        LOGGER.debug("Received invalid ids response. [exception=({})]", exception.getMessage());
        return new ResponseEntity<>("Failed to read the ids response message.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Creates a ResponseEntity with status code 500 and a message indicating that the configuration
     * could not be updated.
     *
     * @param exception The exception that was thrown.
     * @return ResponseEntity with status code 500.
     */
    public static ResponseEntity<Object> respondConfigurationUpdateError(final Exception exception) {
        LOGGER.debug("Failed to update the configuration. [exception=({})]",
                exception.getMessage());
        return new ResponseEntity<>("Failed to update configuration.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Creates a ResponseEntity with status code 500 and a message indicating that an input could
     * not be deserialized.
     *
     * @param exception The exception that was thrown.
     * @return ResponseEntity with status code 500.
     */
    public static ResponseEntity<Object> respondDeserializationError(final Exception exception) {
        LOGGER.warn("Failed to deserialize the object. [exception=({})]", exception.getMessage());
        return new ResponseEntity<>("Failed to update.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Creates a ResponseEntity with status code 404 and a message indicating that the no
     * configuration could be found.
     *
     * @return ResponseEntity with status code 404.
     */
    public static ResponseEntity<Object> respondConfigurationNotFound() {
        LOGGER.info("No configuration could be found.");
        return new ResponseEntity<>("No configuration found.", HttpStatus.NOT_FOUND);
    }

    /**
     * Creates a ResponseEntity with status code 404 and a message indicating that a resource
     * could not be found.
     *
     * @param resourceId ID for that no match was found.
     * @return ResponseEntity with status code 404.
     */
    public static ResponseEntity<Object> respondResourceNotFound(final URI resourceId) {
        LOGGER.debug("The resource does not exist. [resourceId=({})]", resourceId);
        return new ResponseEntity<>("Resource not found.", HttpStatus.NOT_FOUND);
    }

    /**
     * Creates a ResponseEntity with status code 500 and a message indicating that a resource
     * could not be loaded.
     *
     * @param resourceId ID of the resource.
     * @return ResponseEntity with status code 500.
     */
    public static ResponseEntity<Object> respondResourceCouldNotBeLoaded(final URI resourceId) {
        LOGGER.debug("Resource not loaded. [resourceId=({})]", resourceId);
        return new ResponseEntity<>("Could not load resource.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Creates a ResponseEntity with status code 500 and a message indicating that no predefined
     * policy pattern has been recognized.
     *
     * @param exception The exception that was thrown.
     * @return ResponseEntity with status code 500.
     */
    public static ResponseEntity<Object> respondPatternNotIdentified(final Exception exception) {
        LOGGER.debug("Failed to identify policy pattern.", exception);
        return new ResponseEntity<>("Could not identify pattern", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Creates a ResponseEntity with status code 500 and a message indicating that the input was
     * invalid.
     *
     * @param exception The exception that was thrown.
     * @return ResponseEntity with status code 500.
     */
    public static ResponseEntity<Object> respondInvalidInput(final Exception exception) {
        LOGGER.warn("Failed to deserialize the input. [exception=({})]", exception.getMessage());
        return new ResponseEntity<>("Invalid input. " + exception.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Creates a ResponseEntity with status code 500 and a message indicating that the contract
     * request could not be built.
     *
     * @param exception The exception that was thrown.
     * @return ResponseEntity with status code 500.
     */
    public static ResponseEntity<Object> respondFailedToBuildContractRequest(final Exception exception) {
        LOGGER.warn("Failed to build contract request. [exception=({})]", exception.getMessage());
        return new ResponseEntity<>("Failed to build contract request.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Creates a ResponseEntity with status code 500 and a message indicating that the connector
     * could not be loaded or deserialized.
     *
     * @param exception The exception that was thrown.
     * @return ResponseEntity with status code 500.
     */
    public static ResponseEntity<Object> respondConnectorNotLoaded(final Exception exception) {
        LOGGER.warn("Connector could not be loaded. [exception=({})]", exception.getMessage());
        return new ResponseEntity<>("Connector could not be loaded.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Creates a ResponseEntity with status code 500 and a message indicating that something went
     * wrong. Note: Should never be thrown.
     *
     * @param exception The exception that was thrown.
     * @return ResponseEntity with status code 500.
     */
    public static ResponseEntity<Object> respondGlobalException(final Exception exception) {
        LOGGER.warn("Something else went wrong. [exception=({})]", exception.getMessage());
        return new ResponseEntity<>("Something else went wrong.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ResponseEntity<Object> respondFailedToStoreEntity(final Exception exception) {
        LOGGER.warn("Failed to store entity. [exception=({})]", exception.getMessage());
        return new ResponseEntity<>("Failed to store entity. " + exception.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}