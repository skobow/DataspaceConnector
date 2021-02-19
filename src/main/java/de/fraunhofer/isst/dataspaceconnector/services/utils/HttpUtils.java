package de.fraunhofer.isst.dataspaceconnector.services.utils;

import de.fraunhofer.isst.dataspaceconnector.model.QueryInput;
import de.fraunhofer.isst.ids.framework.configuration.ConfigurationContainer;
import de.fraunhofer.isst.ids.framework.util.ClientProvider;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * This class builds up HTTP or HTTPS endpoint connections and sends GET requests.
 */
@Service
public class HttpUtils {

    private final ClientProvider clientProvider;

    /**
     * Constructor for HttpUtils.
     *
     * @throws IllegalArgumentException if any of the parameters is null.
     * @throws GeneralSecurityException if the framework has an error.
     */
    @Autowired
    public HttpUtils(ConfigurationContainer configurationContainer)
        throws IllegalArgumentException, GeneralSecurityException {
        if (configurationContainer == null) {
            throw new IllegalArgumentException("The ConfigurationContainer cannot be null");
        }

        this.clientProvider = new ClientProvider(configurationContainer);
    }

    /**
     * Sends a GET request to an external HTTP endpoint
     *
     * @param address the URL.
     * @param queryInput Header and params for data request from backend.
     * @return the HTTP response if HTTP code is OK (200).
     * @throws MalformedURLException if the input address is not a valid URL.
     * @throws RuntimeException if an error occurred when connecting or processing the HTTP
     *                               request.
     */
    public String sendHttpGetRequest(String address, QueryInput queryInput) throws MalformedURLException,
        RuntimeException {
        try {
            if(queryInput != null) {
                address = replacePathVariablesInUrl(address, queryInput.getPathVariables());
                address = addQueryParamsToURL(address, queryInput.getParams());
            }

            final var url = new URL(address);

            var con = (HttpURLConnection) url.openConnection();
            if(queryInput != null) {
                addHeadersToURL(con, queryInput.getHeaders());
            }
            con.setRequestMethod("GET");

            final var responseCodeOk = 200;
            final var responseCodeUnauthorized = 401;
            final var responseMalformed = -1;

            final var responseCode = con.getResponseCode();

            if (responseCode == responseCodeOk) {
                // Request was ok, read the response
                try (var in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    var content = new StringBuilder();
                    var inputLine = "";
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }

                    return content.toString();
                }
            } else if (responseCode == responseCodeUnauthorized) {
                // The request is not authorized
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
            } else if (responseCode == responseMalformed) {
                // The response code could not be read
                throw new HttpClientErrorException(HttpStatus.EXPECTATION_FAILED);
            } else {
                // This function should never be thrown
                throw new NotImplementedException("Unsupported return value " +
                    "from getResponseCode.");
            }

        } catch (MalformedURLException exception) {
            // The parameter address is not an url.
            throw exception;
        } catch (Exception exception) {
            // Catch all the HTTP, IOExceptions
            throw new RuntimeException("Failed to send the http get request.", exception);
        }
    }

    /**
     * Sends a GET request to an external HTTPS endpoint
     *
     * @param address the URL.
     * @param queryInput Header and params for data request from backend.
     * @return the HTTP body of the response when HTTP code is OK (200).
     * @throws MalformedURLException if the input address is not a valid URL.
     * @throws RuntimeException if an error occurred when connecting or processing the HTTP
     *                               request.
     */
    public String sendHttpsGetRequest(String address, QueryInput queryInput)
        throws MalformedURLException, RuntimeException {
        try {
            if(queryInput != null) {
                address = replacePathVariablesInUrl(address, queryInput.getPathVariables());
                address = addQueryParamsToURL(address, queryInput.getParams());
            }

            final Request request;
            if (queryInput != null && queryInput.getHeaders() != null) {
                Headers headerBuild = Headers.of(queryInput.getHeaders());
                request = new Request.Builder().url(address).get().headers(headerBuild).build();
            } else {
                request = new Request.Builder().url(address).get().build();
            }

            var client = clientProvider.getClient();
            Response response = client.newCall(request).execute();

            if (response.code() < 200 || response.code() >= 300) {
                response.close();
                // Not the expected response code
                throw new HttpClientErrorException(HttpStatus.EXPECTATION_FAILED);
            } else {
                // Read the response
                final var rawResponseString =
                    new String(response.body().byteStream().readAllBytes());
                response.close();

                return rawResponseString;
            }
        } catch (MalformedURLException exception) {
            // The parameter address is not an url.
            throw exception;
        } catch (Exception exception) {
            // Catch all the HTTP, IOExceptions
            throw new RuntimeException("Failed to send the http get request.", exception);
        }
    }

    /**
     * Sends a GET request with basic authentication to an external HTTPS endpoint.
     *
     * @param address the URL.
     * @param username The username.
     * @param password The password.
     * @param queryInput Header and params for data request from backend.
     * @return The HTTP response when HTTP code is OK (200).
     * @throws MalformedURLException if the input address is not a valid URL.
     * @throws RuntimeException if an error occurred when connecting or processing the HTTP
     *                               request.
     */
    public String sendHttpsGetRequestWithBasicAuth(String address, String username,
        String password, QueryInput queryInput) throws MalformedURLException, RuntimeException {
        final var auth = username + ":" + password;
        final var encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
        final var authHeader = "Basic " + new String(encodedAuth);

        try {
            if(queryInput != null) {
                address = replacePathVariablesInUrl(address, queryInput.getPathVariables());
                address = addQueryParamsToURL(address, queryInput.getParams());
            }

            final Request request;
            if (queryInput != null && queryInput.getHeaders() != null) {
                queryInput.getHeaders().put(HttpHeaders.AUTHORIZATION, authHeader);
                Headers headerBuild = Headers.of(queryInput.getHeaders());
                request = new Request.Builder().url(address).get().headers(headerBuild).build();
            } else {
                request = new Request.Builder().url(address).header(HttpHeaders.AUTHORIZATION, authHeader).get().build();
            }

            final var client = clientProvider.getClient();
            final var response = client.newCall(request).execute();

            if (response.code() < 200 || response.code() >= 300) {
                response.close();
                // Not the expected response code
                throw new HttpClientErrorException(HttpStatus.EXPECTATION_FAILED);
            } else {
                String rawResponseString = new String(response.body().byteStream().readAllBytes());
                response.close();

                return rawResponseString;
            }
        } catch (MalformedURLException exception) {
            // The parameter address is not an url.
            throw exception;
        } catch (Exception exception) {
            // Catch all the HTTP, IOExceptions
            throw new RuntimeException("Failed to send the http get request.", exception);
        }
    }

    /**
     * Replaces all parts of a given URL that are marked as path variables, if any, using the values
     * supplied in the path variables map.
     *
     * @param address the URL possibly containing path variables
     * @param pathVariables map containing the values for the path variables by name
     * @return the URL with path variables substituted
     */
    private String replacePathVariablesInUrl(String address, Map<String, String> pathVariables) {
        if (pathVariables != null) {
            long pathVariableCount = address.chars().filter(ch -> ch == '{').count();
            if (pathVariableCount != pathVariables.size()) {
                throw new IllegalArgumentException("The number of supplied path variables does not " +
                        "match the number of path variables in the URL.");
            }

            // http://localhost:8080/{path}/{id}
            for(int i = 1; i <= pathVariableCount; i++) {
                String pathVariableName = address.substring(address.indexOf("{") + 1,
                        address.indexOf("}"));

                String pathVariableValue = pathVariables.get(pathVariableName); // resource

                //should always be first index of braces because all prior should have been replaced
                address = address.substring(0, address.indexOf("{")) // http://localhost:8080/
                        + pathVariableValue // resource
                        + address.substring(address.indexOf("}") + 1); // /{id}
            }

        }

        return address;
    }

    /**
     * Enrich the URL address with given query parameters. If the query parameters are empty, the address remains unchanged.
     *
     * @param address URL address to be enriched.
     * @param queryParams Query parameters that have to be added on the address.
     * @return Address string.
     */
    private String addQueryParamsToURL(String address, Map<String, String> queryParams) {
        if(queryParams != null) {
            if(!queryParams.isEmpty()) {
                address = address.concat("?");
                for (Map.Entry<String, String> param : queryParams.entrySet()) {
                    address = address.concat(URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8)
                                    + "=" + URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8) + "&");
                }

                return StringUtils.removeEnd(address,"&");
            }
        }
        return address;
    }

    /**
     * Enrich the HttpURLConnection with given headers. If the headers are empty, the HttpURLConnection remains unchanged.
     *
     * @param con HttpURLConnection to be enriched.
     * @param headers Headers that have to be added within the address.
     */
    private void addHeadersToURL(HttpURLConnection con, Map<String, String> headers) {
        if(headers != null) {
            if(!headers.isEmpty()) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    con.setRequestProperty(header.getKey(), header.getValue());
                }
            }
        }
    }
}
