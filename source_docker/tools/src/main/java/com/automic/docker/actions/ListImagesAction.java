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
 * Action class to list all the existing images on docker system. Output of this action will be an XML path consisting
 * all the existing images. Moreover, XML file will be created on the same host where agent is running. In case there is
 * no image on docker system , Action will be ended normally and an empty XML file will be created.
 *
 */
public class ListImagesAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(ListImagesAction.class);

    private static final int NO_OF_ARGS = 5;

    private static final int FILE_PATH_INDEX = 4;

    private static final String LIST_IMAGE_ROOT_TAG = "LIST_IMAGES";
    private static final String LIST_IMAGE_CHILD_TAG = "IMAGE";

    private String outputFilePath;

    public ListImagesAction() {
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
        LOGGER.info("File Path = " + ((args.length > FILE_PATH_INDEX) ? args[FILE_PATH_INDEX] : ""));
    }

    @Override
    protected void initialize(String[] args) throws DockerException {

        outputFilePath = args[FILE_PATH_INDEX];
    }

    @Override
    protected void validateInputs() throws DockerException {
        if (!Validator.checkFileFolderExists(outputFilePath)) {
            LOGGER.error("Invalid file-path : " + outputFilePath);
            throw new DockerException(String.format(ExceptionConstants.INVALID_DIRECTORY, outputFilePath));
        }
    }

    @Override
    protected ClientResponse executeSpecific(Client client) {

        ClientResponse response = null;

        WebResource webResource = client.resource(dockerUrl).path("images").path("json").queryParam("all", "0");
        LOGGER.info("Calling url " + webResource.getURI());

        response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

        return response;
    }

    @Override
    protected String getErrorMessage(int errorCode) {
        return (HttpStatus.SC_INTERNAL_SERVER_ERROR == errorCode) ? "server error " : Constants.UNKNOWN_ERROR;
    }

    /**
     * {@inheritDoc ExecStartAction#prepareOutput(ClientResponse)} This method prepare the output xml by converting the
     * json response into xml which is then written to the file at the path provided
     */
    @Override
    protected void prepareOutput(ClientResponse response) throws DockerException {
        DockerUtility.jsonArrayResponse2xml(response.getEntityInputStream(), outputFilePath, LIST_IMAGE_ROOT_TAG,
                LIST_IMAGE_CHILD_TAG);
    }

}
