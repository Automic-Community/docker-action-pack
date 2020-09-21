package com.automic.docker.actions;

import java.io.IOException;

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
 * Action class to list all the existing/active containers as per the specified input. Output of this action will be an
 * XML file consisting all the containers based on the input parameters. Moreover, XML file will be created on the same
 * host where agent is running.
 *
 */
public class ListContainersAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(ListContainersAction.class);

    private static final int NO_OF_ARGS = 11;

    private static final int QUERY_PARAM_ALL_INDEX = 4;
    private static final int QUERY_PARAM_LIMIT_INDEX = 5;
    private static final int QUERY_PARAM_SINCE_INDEX = 6;
    private static final int QUERY_PARAM_BEFORE_INDEX = 7;
    private static final int FILTER_FILE_PATH = 8;
    private static final int QUERY_PARAM_SIZE_INDEX = 9;
    private static final int FILE_PATH_INDEX = 10;

    private static final String QUERY_PARAM_ALL = "all";
    private static final String QUERY_PARAM_LIMIT = "limit";
    private static final String QUERY_PARAM_SINCE = "since";
    private static final String QUERY_PARAM_BEFORE = "before";
    private static final String QUERY_PARAM_SIZE = "size";

    private static final String LIST_CONTAINER_ROOT_TAG = "LIST_CONTAINERS";
    private static final String LIST_CONTAINER_CHILD_TAG = "CONTAINER";

    private boolean all;
    private int limit;
    private String since;
    private String before;
    private boolean size;
    private String filterFilePath;
    private String outputFilePath;

    public ListContainersAction() {
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
        LOGGER.info("Query-param-all = " + ((args.length > QUERY_PARAM_ALL_INDEX) ? args[QUERY_PARAM_ALL_INDEX] : ""));
        LOGGER.info("Query-param-limit = "
                + ((args.length > QUERY_PARAM_LIMIT_INDEX) ? args[QUERY_PARAM_LIMIT_INDEX] : ""));
        LOGGER.info("Query-param-since = "
                + ((args.length > QUERY_PARAM_SINCE_INDEX) ? args[QUERY_PARAM_SINCE_INDEX] : ""));
        LOGGER.info("Query-param-before = "
                + ((args.length > QUERY_PARAM_BEFORE_INDEX) ? args[QUERY_PARAM_BEFORE_INDEX] : ""));
        LOGGER.info("Query-param-filters = " + ((args.length > FILTER_FILE_PATH) ? args[FILTER_FILE_PATH] : ""));
        LOGGER.info("Query-param-size = "
                + ((args.length > QUERY_PARAM_SIZE_INDEX) ? args[QUERY_PARAM_SIZE_INDEX] : ""));
        LOGGER.info("File-Path = " + ((args.length > FILE_PATH_INDEX) ? args[FILE_PATH_INDEX] : ""));
    }

    @Override
    protected void initialize(String[] args) throws DockerException {
        all = !DockerUtility.convert2Bool(args[QUERY_PARAM_ALL_INDEX]);

        limit = DockerUtility.getAndCheckUnsignedValue(args[QUERY_PARAM_LIMIT_INDEX]);

        since = args[QUERY_PARAM_SINCE_INDEX];
        before = args[QUERY_PARAM_BEFORE_INDEX];

        size = DockerUtility.convert2Bool(args[QUERY_PARAM_SIZE_INDEX]);

        filterFilePath = args[FILTER_FILE_PATH];
        outputFilePath = args[FILE_PATH_INDEX];
    }

    @Override
    protected void validateInputs() throws DockerException {
        // validating filter file
        if (Validator.checkNotEmpty(filterFilePath) && !Validator.checkFileExists(filterFilePath)) {
            String msg = String.format(ExceptionConstants.INVALID_FILE, filterFilePath);
            LOGGER.error(msg);
            throw new DockerException(msg);
        }

        if (limit < 0) {
            String msg = String.format(ExceptionConstants.INVALID_LIMIT_PARAMETER, limit);
            LOGGER.error(msg);
            throw new DockerException(msg);
        }

        if (!Validator.checkFileFolderExists(outputFilePath)) {
            LOGGER.error("Invalid Directory : " + outputFilePath);
            throw new DockerException(String.format(ExceptionConstants.INVALID_DIRECTORY, outputFilePath));
        }
    }

    /**
     * {@inheritDoc ExecStartAction#executeSpecific(Client)} This method prepare the output xml by converting the json
     * response into xml which is then written to the file at the path provided
     */
    @Override
    protected ClientResponse executeSpecific(Client client) throws DockerException {

        ClientResponse response = null;

        WebResource webResource = client.resource(dockerUrl).path("containers").path("json");

        webResource = webResource.queryParam(QUERY_PARAM_ALL, Boolean.toString(all));

        if (limit > 0) {
            webResource = webResource.queryParam(QUERY_PARAM_LIMIT, Integer.toString(limit));
        }

        if (Validator.checkNotEmpty(since)) {
            webResource = webResource.queryParam(QUERY_PARAM_SINCE, since);
        }

        if (Validator.checkNotEmpty(before)) {
            webResource = webResource.queryParam(QUERY_PARAM_BEFORE, before);
        }

        webResource = webResource.queryParam(QUERY_PARAM_SIZE, Boolean.toString(size));

        if (Validator.checkNotEmpty(filterFilePath)) {
            try {
                String value = DockerUtility.readFileFromPath(filterFilePath, true);
                webResource = webResource.queryParam("filters", value);
            } catch (IOException e) {
                LOGGER.error("Error occured while reading filter file ", e);
                throw new DockerException(String.format(ExceptionConstants.UNABLE_TO_READ_FILE, filterFilePath), e);
            }
        }

        LOGGER.info("Calling url " + webResource.getURI());

        response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        return response;
    }

    @Override
    protected String getErrorMessage(int errorCode) {
        String msg = null;
        switch (errorCode) {
            case HttpStatus.SC_BAD_REQUEST:
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
     * {@inheritDoc ExecStartAction#prepareOutput(ClientResponse)} This method prepare the output xml by converting the
     * json response into xml which is then written to the file at the path provided
     */
    @Override
    protected void prepareOutput(ClientResponse response) throws DockerException {
        DockerUtility.jsonArrayResponse2xml(response.getEntityInputStream(), outputFilePath, LIST_CONTAINER_ROOT_TAG,
                LIST_CONTAINER_CHILD_TAG);
    }

}
