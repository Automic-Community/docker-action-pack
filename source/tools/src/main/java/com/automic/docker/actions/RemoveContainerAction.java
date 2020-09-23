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
import com.automic.docker.utility.DockerUtility;
import com.automic.docker.utility.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Action class to remove an existing container based on the specified container id.
 *
 */
public class RemoveContainerAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(RemoveContainerAction.class);

    private static final int NO_OF_ARGS = 5;

    private static final int CONTAINER_ID_INDEX = 4;
    private static final int ASSOCIATED_VOLUME_INDEX = 5;
    private static final int FORCEFULLY_PARAMTER_INDEX = 6;

    private String containerId;
    private boolean removeVolume;
    private boolean doForceFully;

    public RemoveContainerAction() {
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
        LOGGER.info("Remove-volume = " + ((args.length > ASSOCIATED_VOLUME_INDEX) ? args[ASSOCIATED_VOLUME_INDEX] : ""));
        LOGGER.info("Forcefully = "
                + ((args.length > FORCEFULLY_PARAMTER_INDEX) ? args[FORCEFULLY_PARAMTER_INDEX] : ""));
    }

    @Override
    protected void initialize(String[] args) throws DockerException {
        containerId = args[CONTAINER_ID_INDEX];
        if (args.length > ASSOCIATED_VOLUME_INDEX) {
            removeVolume = DockerUtility.convert2Bool(args[ASSOCIATED_VOLUME_INDEX]);
        }
        if (args.length > FORCEFULLY_PARAMTER_INDEX) {
            doForceFully = DockerUtility.convert2Bool(args[FORCEFULLY_PARAMTER_INDEX]);
        }
    }

    @Override
    protected void validateInputs() throws DockerException {
        if (!Validator.checkNotEmpty(containerId)) {
            LOGGER.error(ExceptionConstants.EMPTY_CONTAINER_ID);
            throw new DockerException(ExceptionConstants.EMPTY_CONTAINER_ID);
        }
    }

    @Override
    protected ClientResponse executeSpecific(Client client) throws DockerException {
        ClientResponse response = null;

        WebResource webResource = client.resource(dockerUrl).path("containers").path(containerId);

        webResource = webResource.queryParam("v", Boolean.toString(removeVolume));

        webResource = webResource.queryParam("force", Boolean.toString(doForceFully));

        LOGGER.info("Calling url " + webResource.getURI());

        response = webResource.accept(MediaType.APPLICATION_JSON).delete(ClientResponse.class);
        return response;
    }

    @Override
    protected String getErrorMessage(int errorCode) {
        String msg = null;
        switch (errorCode) {
            case HttpStatus.SC_BAD_REQUEST:
                msg = "bad parameter";
                break;
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
     * This method does nothing as only a success message is printed on console when container is successfully removed
     * from docker system
     */
    @Override
    protected void prepareOutput(ClientResponse response) throws DockerException {
    }

}
