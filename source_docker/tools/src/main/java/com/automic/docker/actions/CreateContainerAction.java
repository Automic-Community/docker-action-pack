/**
 *
 */
package com.automic.docker.actions;

import java.io.File;

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
 * Action class to create container in a Docker System. It takes certain parameters like container name, json parameter
 * file and output file. Output of this action will be an xml file if container is created successfully.
 *
 */
public class CreateContainerAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(CreateContainerAction.class);

    private static final int NO_OF_ARGS = 7;

    private static final int CONTAINER_NAME_INDEX = 4;
    private static final int PARAMETER_FILEPATH_INDEX = 5;
    private static final int FILE_PATH_INDEX = 6;

    private static final String PATTERN = "/?[a-zA-Z0-9_-]+";

    private static final String CREATE_CONTAINER_TAG = "CREATE_CONTAINER";

    private String containerName;
    private String jsonParameterFile;
    private String outputFilePath;

    public CreateContainerAction() {
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
        LOGGER.info("Container-name = " + ((args.length > CONTAINER_NAME_INDEX) ? args[CONTAINER_NAME_INDEX] : ""));
        LOGGER.info("Parameter-file-path = "
                + ((args.length > PARAMETER_FILEPATH_INDEX) ? args[PARAMETER_FILEPATH_INDEX] : ""));
        LOGGER.info("File-path = " + ((args.length > FILE_PATH_INDEX) ? args[FILE_PATH_INDEX] : ""));
    }

    @Override
    protected void initialize(String[] args) throws DockerException {
        containerName = args[CONTAINER_NAME_INDEX];
        jsonParameterFile = args[PARAMETER_FILEPATH_INDEX];
        outputFilePath = args[FILE_PATH_INDEX];
    }

    @Override
    protected void validateInputs() throws DockerException {
        if (Validator.checkNotEmpty(containerName) && !Validator.isValidText(PATTERN, containerName)) {
            LOGGER.error(ExceptionConstants.INVALID_CONTAINER_NAME);
            throw new DockerException(ExceptionConstants.INVALID_CONTAINER_NAME);
        }

        if (!Validator.checkFileExists(jsonParameterFile)) {
            String msg = String.format(ExceptionConstants.INVALID_FILE, jsonParameterFile);
            LOGGER.error(msg);
            throw new DockerException(msg);
        }

        if (!Validator.checkFileFolderExists(outputFilePath)) {
            LOGGER.error("Invalid file-path : " + outputFilePath);
            throw new DockerException(String.format(ExceptionConstants.INVALID_DIRECTORY, outputFilePath));
        }
    }

    @Override
    protected ClientResponse executeSpecific(Client client) throws DockerException {

        ClientResponse response = null;

        WebResource webResource = client.resource(dockerUrl).path("containers").path("create");
        if (Validator.checkNotEmpty(containerName)) {
            webResource = webResource.queryParam("name", containerName);
        }

        LOGGER.info("Calling url " + webResource.getURI());

        response = webResource.entity(new File(jsonParameterFile), MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).post(ClientResponse.class);

        return response;
    }

    @Override
    protected String getErrorMessage(int errorCode) {
        String msg = null;
        switch (errorCode) {
            case HttpStatus.SC_NOT_FOUND:
                msg = "bad parameter";
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
     * {@inheritDoc AbstractAction#prepareOutput(ClientResponse)}
     *
     * Using an instance of {@link ClientResponse} it creates a output xml file containing response returned by Docker
     * API.
     */
    @Override
    protected void prepareOutput(ClientResponse response) throws DockerException {
        DockerUtility.jsonResponse2xml(response.getEntityInputStream(), outputFilePath, CREATE_CONTAINER_TAG);
    }

}
