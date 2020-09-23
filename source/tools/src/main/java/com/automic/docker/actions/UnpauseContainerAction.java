/**
 *
 */
package com.automic.docker.actions;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.docker.constants.Constants;
import com.automic.docker.constants.ExceptionConstants;
import com.automic.docker.exceptions.DockerException;
import com.automic.docker.utility.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Action class to unpause an existing container. It requires a container id as a parameter.
 *
 */
public class UnpauseContainerAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(UnpauseContainerAction.class);

    private static final int NO_OF_ARGS = 5;

    private static final int CONTAINER_ID_INDEX = 4;

    private static final String ACTION = "unpause";

    private String containerid;

    public UnpauseContainerAction() {
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
        LOGGER.info("Container-id" + ((args.length > CONTAINER_ID_INDEX) ? args[CONTAINER_ID_INDEX] : ""));
    }

    @Override
    protected void initialize(String[] args) throws DockerException {
        containerid = args[CONTAINER_ID_INDEX];
    }

    @Override
    protected void validateInputs() throws DockerException {
        if (!Validator.checkNotEmpty(containerid)) {
            LOGGER.error(ExceptionConstants.EMPTY_CONTAINER_ID);
            throw new DockerException(ExceptionConstants.EMPTY_CONTAINER_ID);
        }

    }

    @Override
    protected ClientResponse executeSpecific(Client client) throws DockerException {
        ClientResponse response = null;

        WebResource webResource = client.resource(dockerUrl).path("containers").path(containerid).path(ACTION);
        LOGGER.info("Calling url " + webResource.getURI());
        response = webResource.accept(MediaType.APPLICATION_JSON).post(ClientResponse.class);
        return response;
    }

    @Override
    protected String getErrorMessage(int errorCode) {
        String msg = null;
        switch (errorCode) {
            case HttpStatus.SC_NOT_FOUND:
                msg = "no such container";
                break;
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                msg = "server error ";
                break;
            default:
                msg = Constants.UNKNOWN_ERROR;
                break;
        }
        return msg;
    }

    /**
     *
     * This method does nothing as only a success message is printed on console when container is successfully unpaused
     */
    @Override
    protected void prepareOutput(ClientResponse response) throws DockerException {
    }

}
