package com.automic.docker.actions;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
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
 * Action class to remove images from the docker based on specified image id.
 *
 */
public class RemoveImageAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(RemoveImageAction.class);

    private static final int NO_OF_ARGS = 5;

    private static final int IMAGE_NAME_INDEX = 4;
    private static final int FORCEFUL_PARAMETER_INDEX = 5;
    private static final int NO_PRUNE_PARAMETER_INDEX = 6;

    private String imageName;
    private boolean forecefully;
    private boolean noPrune;

    public RemoveImageAction() {
        super(NO_OF_ARGS);
    }

    @Override
    protected void logParameters(final String[] args) {
        LOGGER.info("Input parameters -->");
        LOGGER.info("Connection Timeout = "
                + ((args.length > CONNECTION_TIMEOUT_INDEX) ? args[CONNECTION_TIMEOUT_INDEX] : ""));
        LOGGER.info("Read-timeout = " + ((args.length > READ_TIMEOUT_INDEX) ? args[READ_TIMEOUT_INDEX] : ""));
        LOGGER.info("Docker-url = " + ((args.length > DOCKER_URL_INDEX) ? args[DOCKER_URL_INDEX] : ""));
        LOGGER.info("Certificate-path = " + ((args.length > CERTIFICATE_INDEX) ? args[CERTIFICATE_INDEX] : ""));
        LOGGER.info("Image-id = " + ((args.length > IMAGE_NAME_INDEX) ? args[IMAGE_NAME_INDEX] : ""));
        LOGGER.info("Forcefully = " + ((args.length > FORCEFUL_PARAMETER_INDEX) ? args[FORCEFUL_PARAMETER_INDEX] : ""));
        LOGGER.info("No Prune = " + ((args.length > NO_PRUNE_PARAMETER_INDEX) ? args[NO_PRUNE_PARAMETER_INDEX] : ""));
    }

    @Override
    protected void initialize(final String[] args) throws DockerException {
        imageName = args[IMAGE_NAME_INDEX];
        if (args.length > NO_PRUNE_PARAMETER_INDEX) {
            noPrune = DockerUtility.convert2Bool(args[NO_PRUNE_PARAMETER_INDEX]);
        }
        if (args.length > FORCEFUL_PARAMETER_INDEX) {
            forecefully = DockerUtility.convert2Bool(args[FORCEFUL_PARAMETER_INDEX]);
        }

    }

    @Override
    protected void validateInputs() throws DockerException {
        if (!Validator.checkNotEmpty(imageName)) {
            LOGGER.error(ExceptionConstants.EMPTY_IMAGE_NAME);
            throw new DockerException(ExceptionConstants.IMAGE_NAME_EMPTY);
        }
    }

    @Override
    protected ClientResponse executeSpecific(final Client client) throws DockerException {

        ClientResponse response = null;

        WebResource webResource = client.resource(dockerUrl).path("images").path(imageName);

        webResource = webResource.queryParam("force", Boolean.toString(forecefully));

        webResource = webResource.queryParam("noprune", Boolean.toString(noPrune));

        LOGGER.info("Calling url " + webResource.getURI());

        response = webResource.accept(MediaType.APPLICATION_JSON).delete(ClientResponse.class);

        return response;
    }

    @Override
    protected String getErrorMessage(final int errorCode) {
        String msg;
        switch (errorCode) {
            case HttpStatus.SC_NOT_FOUND:
                msg = "no such image";
                break;
            case HttpStatus.SC_CONFLICT:
                msg = "conflict";
                break;
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                msg = "server error";
                break;
            default:
                msg = Constants.UNKNOWN_ERROR;
        }
        return msg;
    }

    /**
     * {@inheritDoc ExecStartAction#prepareOutput(ClientResponse)} This method prepare the output by parsing the json
     * response and printing it to the console.
     */
    @Override
    protected void prepareOutput(final ClientResponse response) throws DockerException {

        JSONArray jsonArray = DockerUtility.jsonArrayResponse(response.getEntityInputStream());
        for (int index = 0; index < jsonArray.length(); index++) {
            JSONObject jsonObject = jsonArray.getJSONObject(index);
            if (jsonObject != null) {
                System.out.println(jsonObject.toString());
            }

        }
    }

}
