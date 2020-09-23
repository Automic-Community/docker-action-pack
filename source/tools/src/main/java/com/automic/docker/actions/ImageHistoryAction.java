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
 *
 *
 * This Action accepts request to fetch history of Image from Docker. Docker api expects us to provide an Image name and
 * an optional tag depending on whether the tag has default value of "latest" or not. For instance:
 * <ul>
 * <li>Image with name "ubuntu" and tag "latest" the request url for image history is /images/ubuntu/history or
 * /images/ubuntu:latest/history</li>
 * <li>Image with name "ubuntu" and tag "10.0.4" tag is mandatory and the request url for image history is
 * /images/ubuntu:10.0.4/history</li>
 * </ul>
 *
 */
public class ImageHistoryAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(ImageHistoryAction.class);

    private static final int NO_OF_ARGS = 6;

    private static final int IMAGE_NAME_TAG_INDEX = 4;

    private static final int OUTPUT_FILE_PATH_INDEX = 5;

    private static final String IMAGE_HISTORY_ROOT_TAG = "ImageHistory";

    private static final String IMAGE_HISTORY_CHILD_TAG = "History";

    /**
     * Image name and optional tag
     */
    private String imageNameTag;

    /**
     * path to output file
     */
    private String outputFile;

    public ImageHistoryAction() {
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
        LOGGER.info("Image name and tag = " + ((args.length > IMAGE_NAME_TAG_INDEX) ? args[IMAGE_NAME_TAG_INDEX] : ""));
        LOGGER.info("File Path = " + ((args.length > OUTPUT_FILE_PATH_INDEX) ? args[OUTPUT_FILE_PATH_INDEX] : ""));
    }

    @Override
    protected void initialize(String[] args) throws DockerException {
        imageNameTag = args[IMAGE_NAME_TAG_INDEX];
        outputFile = args[OUTPUT_FILE_PATH_INDEX];
    }

    @Override
    protected void validateInputs() throws DockerException {
        if (!Validator.checkFileFolderExists(outputFile)) {
            LOGGER.error("Invalid file-path : " + outputFile);
            throw new DockerException(String.format(ExceptionConstants.INVALID_DIRECTORY, outputFile));
        }
        if (!Validator.checkNotEmpty(imageNameTag)) {
            LOGGER.error(ExceptionConstants.EMPTY_IMAGE_NAME);
            throw new DockerException(ExceptionConstants.EMPTY_IMAGE_NAME);
        }
    }

    @Override
    protected ClientResponse executeSpecific(Client client) {

        ClientResponse response = null;

        WebResource webResource = client.resource(dockerUrl).path("images").path(imageNameTag).path("history");
        LOGGER.info("Calling url " + webResource.getURI());

        response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

        return response;
    }

    @Override
    protected String getErrorMessage(int errorCode) {
        String msg = null;
        switch (errorCode) {
            case HttpStatus.SC_NOT_FOUND:
                msg = "no such image";
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
     * json response into xml which is then written to the file at the path provided.
     */
    @Override
    protected void prepareOutput(ClientResponse response) throws DockerException {
        DockerUtility.jsonArrayResponse2xml(response.getEntityInputStream(), outputFile, IMAGE_HISTORY_ROOT_TAG,
                IMAGE_HISTORY_CHILD_TAG);

    }

}
