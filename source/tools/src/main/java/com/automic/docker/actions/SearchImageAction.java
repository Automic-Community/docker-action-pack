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
 * Action class to search base images from docker public hub registry based on specified image name text. Output of this
 * action will be an XML path consisting all the images based on the input and total number of records fetched.
 * Moreover, XML file will be created on the same host where agent is running.
 */
public class SearchImageAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(SearchImageAction.class);
    private static final int NO_OF_ARGS = 6;

    private static final String SEARCH_ROOT_NODE = "SEARCH_IMAGES";
    private static final String SEARCH_CHILD_NODE = "SEARCH_IMAGE";

    private static final int SEARCH_TERM_INDEX = 4;
    private static final int FILE_PATH_INDEX = 5;

    private String searchTerm;
    private String outputFilePath;

    public SearchImageAction() {
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
        LOGGER.info("Search term = " + ((args.length > SEARCH_TERM_INDEX) ? args[SEARCH_TERM_INDEX] : ""));
        LOGGER.info("File Path = " + ((args.length > FILE_PATH_INDEX) ? args[FILE_PATH_INDEX] : ""));
    }

    @Override
    protected void initialize(String[] args) throws DockerException {

        searchTerm = args[SEARCH_TERM_INDEX];
        outputFilePath = args[FILE_PATH_INDEX];
    }

    @Override
    protected void validateInputs() throws DockerException {

        if (!Validator.checkNotEmpty(searchTerm)) {
            String msg = String.format(ExceptionConstants.MISSING_REQUIRED_PARAM, "Search term");
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

        WebResource webResource = client.resource(dockerUrl).path("images").path("search")
                .queryParam("term", searchTerm);

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
        DockerUtility.jsonArrayResponse2xml(response.getEntityInputStream(), outputFilePath, SEARCH_ROOT_NODE,
                SEARCH_CHILD_NODE);
    }

}
