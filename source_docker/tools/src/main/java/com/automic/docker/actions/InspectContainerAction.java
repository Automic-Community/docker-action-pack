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
 * Action class to inspect container as per the specified container id in the given docker environment. Output of this
 * action will be an XML path consisting all the low level information about specified container. Moreover, XML file
 * will be created on the same host where agent is running.
 */
public class InspectContainerAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(InspectContainerAction.class);

    private static final int NO_OF_ARGS = 6;

    private static final int CONTAINER_ID_INDEX = 4;
    private static final int FILE_PATH_INDEX = 5;

    private static final String INSPECT_CONTAINER_ROOT_TAG = "INSPECT_CONTAINER";

    private String containerId;
    private String outputFilePath;

    public InspectContainerAction() {
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
        LOGGER.info("Container Id = " + ((args.length > CONTAINER_ID_INDEX) ? args[CONTAINER_ID_INDEX] : ""));
        LOGGER.info("File-Path = " + ((args.length > FILE_PATH_INDEX) ? args[FILE_PATH_INDEX] : ""));
    }

    @Override
    protected void initialize(String[] args) throws DockerException {
        containerId = args[CONTAINER_ID_INDEX];
        outputFilePath = args[FILE_PATH_INDEX];
    }

    @Override
    protected void validateInputs() throws DockerException {
        if (!Validator.checkNotEmpty(containerId)) {
            LOGGER.error(ExceptionConstants.EMPTY_CONTAINER_ID);
            throw new DockerException(ExceptionConstants.EMPTY_CONTAINER_ID);
        }

        if (!Validator.checkFileFolderExists(outputFilePath)) {
            LOGGER.error("Invalid Directory : " + outputFilePath);
            throw new DockerException(String.format(ExceptionConstants.INVALID_DIRECTORY, outputFilePath));
        }
    }

    /**
     * {@inheritDoc ExecStartAction#executeSpecific(Client)} This method execute the request by calling docker 'inspect
     * container' remote API and return the response in json format
     */
    @Override
    protected ClientResponse executeSpecific(Client client) throws DockerException {

        ClientResponse response = null;

        WebResource webResource = client.resource(dockerUrl).path("containers").path(containerId).path("json");

        LOGGER.info("Calling url " + webResource.getURI());

        response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
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
     * {@inheritDoc ExecStartAction#prepareOutput(ClientResponse)} This method prepare the output xml by converting the
     * json response into xml which is then written to the file at the path provided
     */
    @Override
    protected void prepareOutput(ClientResponse response) throws DockerException {
        DockerUtility.jsonResponse2xml(response.getEntityInputStream(), outputFilePath, INSPECT_CONTAINER_ROOT_TAG);
    }

}
