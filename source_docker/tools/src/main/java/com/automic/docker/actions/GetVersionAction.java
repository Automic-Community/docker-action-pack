/**
 * 
 */
package com.automic.docker.actions;

import java.util.Iterator;

import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.automic.docker.constants.Constants;
import com.automic.docker.exceptions.DockerException;
import com.automic.docker.utility.DockerUtility;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Action class to print Docker version of a specified Docker system.
 * 
 */
public class GetVersionAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(GetVersionAction.class);

    private static final int NO_OF_ARGS = 3;

    public GetVersionAction() {
        super(NO_OF_ARGS);

    }

    @Override
    protected void logParameters(String[] args) {
        LOGGER.info("Input parameters -->");
        LOGGER.info("Connection Timeout = "
                + ((args.length > CONNECTION_TIMEOUT_INDEX) ? args[CONNECTION_TIMEOUT_INDEX] : ""));
        LOGGER.info("Read-timeout = " + ((args.length > READ_TIMEOUT_INDEX) ? args[READ_TIMEOUT_INDEX] : ""));
        LOGGER.info("Docker-url = " + ((args.length > DOCKER_URL_INDEX) ? args[DOCKER_URL_INDEX] : ""));
        LOGGER.info("Certificate-path = " + ((args.length > CERTIFICATE_INDEX) ? args[CERTIFICATE_INDEX] : ""));
    }

    @Override
    protected void initialize(String[] args) throws DockerException {
    }

    @Override
    protected void validateInputs() throws DockerException {
    }

    @Override
    protected ClientResponse executeSpecific(Client client) throws DockerException {
        ClientResponse response = null;

        WebResource webResource = client.resource(dockerUrl).path("version");

        LOGGER.info("Calling url " + webResource.getURI());

        response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

        return response;
    }

    @Override
    protected String getErrorMessage(int errorCode) {
        return Constants.UNKNOWN_ERROR;
    }

    /**
     * {@inheritDoc ExecStartAction#prepareOutput(ClientResponse)} Prints the Docker version to standard console.
     */
    @Override
    protected void prepareOutput(ClientResponse response) {
        JSONObject jsonObject = DockerUtility.jsonResponse(response.getEntityInputStream());
        @SuppressWarnings("unchecked")
        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            System.out.println(String.format("%s = %s", key, jsonObject.getString(key)));
        }
    }

}
