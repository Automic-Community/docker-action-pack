/**
 *
 */
package com.automic.docker.actions;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.automic.docker.constants.Constants;
import com.automic.docker.constants.ExceptionConstants;
import com.automic.docker.exceptions.DockerException;
import com.automic.docker.utility.DockerUtility;
import com.automic.docker.utility.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Action class to inspect an existing image from the system where docker is installed based on specified image name
 * text. Output of this action will be an XML path consisting all the low level information about specified image.
 * Moreover, XML file will be created on the same host where agent is running.
 */
public class InspectImageAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(InspectImageAction.class);

    private static final int NO_OF_ARGS = 6;

    private static final int IMAGE_NAME_INDEX = 4;
    private static final int FILE_PATH_INDEX = 5;

    private static final String INESPECT_IMAGE_PREFIX = "images";
    private static final String INESPECT_IMAGE_POSTFIX = "json";

    private static final String INSPECT_TAG = "INSPECT_IMAGE";

    private String imageName;
    private String outputFilePath;

    public InspectImageAction() throws DockerException {
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
        LOGGER.info("Image Name = " + ((args.length > IMAGE_NAME_INDEX) ? args[IMAGE_NAME_INDEX] : ""));
        LOGGER.info("File Path = " + ((args.length > FILE_PATH_INDEX) ? args[FILE_PATH_INDEX] : ""));
    }

    @Override
    protected void initialize(String[] args) throws DockerException {
        imageName = args[IMAGE_NAME_INDEX];
        outputFilePath = args[FILE_PATH_INDEX];
    }

    @Override
    protected void validateInputs() throws DockerException {
        if (!Validator.checkNotEmpty(imageName)) {
            String msg = String.format(ExceptionConstants.MISSING_REQUIRED_PARAM, "Image name");
            LOGGER.error(msg);
            throw new DockerException(msg);
        }
        if (!Validator.checkFileFolderExists(outputFilePath)) {
            LOGGER.error("Invalid file-path : " + outputFilePath);
            throw new DockerException(String.format(ExceptionConstants.INVALID_DIRECTORY, outputFilePath));
        }
    }

    @Override
    protected ClientResponse executeSpecific(Client client) {
        ClientResponse response = null;

        WebResource webResource = client.resource(dockerUrl).path(INESPECT_IMAGE_PREFIX).path(imageName)
                .path(INESPECT_IMAGE_POSTFIX);

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
     * json response into xml which is then written to the file at the path provided
     */
    @Override
    protected void prepareOutput(ClientResponse response) throws DockerException {
        JSONObject json = DockerUtility.jsonResponse(response.getEntityInputStream());
        DockerUtility.json2xml(json, outputFilePath, INSPECT_TAG);
    }

}
