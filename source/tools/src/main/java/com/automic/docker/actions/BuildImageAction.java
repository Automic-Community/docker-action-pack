package com.automic.docker.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.automic.docker.constants.Constants;
import com.automic.docker.constants.ExceptionConstants;
import com.automic.docker.exceptions.DockerException;
import com.automic.docker.utility.DockerUtility;
import com.automic.docker.utility.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Action class to Build Image from provide Docker File. It will Create docker image and throw exception if required tar
 * file is missing and required image name is not available.
 */
public class BuildImageAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(BuildImageAction.class);

    private static final int NO_OF_ARGS = 6;
    private static final int TAR_FILE_INDEX = 4;
    private static final int IMAGE_NAME_INDEX = 5;
    private static final int QUERY_STRING_INDEX = 6;

    private static final String QUERY_DELIMETER = ",";
    private static final String VAL_DELIMETER = "=";
    private static final String ERROR_KEY = "error";
    private static final String APPLICATION_TAR = "application/tar";
    private static final String PROGRESS_KEY = "progress";

    private String imageName;
    private String tarFilePath;
    private String queryString;

    public BuildImageAction() {
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
        LOGGER.info("tar file-path = " + ((args.length > TAR_FILE_INDEX) ? args[TAR_FILE_INDEX] : ""));
        LOGGER.info("Image-name = " + ((args.length > IMAGE_NAME_INDEX) ? args[IMAGE_NAME_INDEX] : ""));
        LOGGER.info("Query-String = " + ((args.length > QUERY_STRING_INDEX) ? args[QUERY_STRING_INDEX] : ""));

    }

    @Override
    protected void initialize(String[] args) throws DockerException {

        imageName = args[IMAGE_NAME_INDEX];
        tarFilePath = args[TAR_FILE_INDEX];
        if (args.length > QUERY_STRING_INDEX) {
            queryString = args[QUERY_STRING_INDEX];
        }

    }

    @Override
    protected void validateInputs() throws DockerException {

        if (!Validator.checkNotEmpty(imageName)) {
            LOGGER.error(ExceptionConstants.EMPTY_IMAGE_NAME);
            throw new DockerException(ExceptionConstants.EMPTY_IMAGE_NAME);
        }

        if (!Validator.checkFileExistsAndIsFile(tarFilePath)) {
            String msg = String.format(ExceptionConstants.INVALID_FILE, tarFilePath);
            LOGGER.error(msg);
            throw new DockerException(msg);
        }

    }

    @Override
    protected ClientResponse executeSpecific(Client client) throws DockerException {
        ClientResponse response = null;
        InputStream fileInStream = null;

        WebResource webResource = client.resource(dockerUrl).path("build");

        // Preparing Query String
        if (Validator.checkNotEmpty(queryString)) {
            Map<String, String> paramMap = prepareQueryParamsMap(queryString);
            String temp = "Using advanced options: " + paramMap.toString();
            System.out.println(temp);
            LOGGER.info(temp);
            for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                webResource = webResource.queryParam(entry.getKey(), entry.getValue());
            }
        }
        webResource = webResource.queryParam("t", imageName);
        try {
            fileInStream = new FileInputStream(new File(tarFilePath));
        } catch (IOException e) {
            LOGGER.error("Error while reading tar file for Build Image ", e);
            throw new DockerException(String.format(ExceptionConstants.UNABLE_TO_READ_FILE, tarFilePath), e);
        }

        LOGGER.info("Calling url " + webResource.getURI());

        response = webResource.entity(fileInStream, APPLICATION_TAR).accept(MediaType.APPLICATION_OCTET_STREAM)
                .post(ClientResponse.class);

        return response;
    }

    /**
     * Method to prepare a map of query arguments. Splits a string using {@link BuildImageAction#QUERY_DELIMETER} and
     * {@link BuildImageAction#VAL_DELIMETER} and create key value pair of query arguments. Example
     * "dockerfile=./img/Dockerfile,q=true" is split into a map containing (dockerfile,./img/Dockerfile) and (q,true).
     *
     * @param queryArgs
     *            String of query arguments as input
     * @return a map
     */
    private Map<String, String> prepareQueryParamsMap(String queryArgs) {
        Map<String, String> paramMap = new HashMap<String, String>();
        String[] splitArgs = queryArgs.split(QUERY_DELIMETER);
        if (splitArgs != null && splitArgs.length > 0) {
            for (String str : splitArgs) {
                String[] queryParam = str.split(VAL_DELIMETER);
                if (queryParam != null && queryParam.length == 2) {
                    String key = queryParam[0].trim();
                    key = key.toLowerCase();
                    String value = queryParam[1].trim();
                    if (Validator.checkNotEmpty(key) && Validator.checkNotEmpty(value)) {
                        switch (key) {
                            case "dockerfile":
                                paramMap.put(key, value);
                                break;
                            case "t":
                                imageName = value;
                                break;
                            case "remote":
                                paramMap.put(key, value);
                                break;
                            case "q":
                                paramMap.put(key, String.valueOf(DockerUtility.convert2Bool(value)));
                                break;
                            case "nocache":
                                paramMap.put(key, String.valueOf(DockerUtility.convert2Bool(value)));
                                break;
                            case "pull":
                                paramMap.put(key, String.valueOf(DockerUtility.convert2Bool(value)));
                                break;
                            case "rm":
                                paramMap.put(key, String.valueOf(DockerUtility.convert2Bool(value)));
                                break;
                            case "forcerm":
                                paramMap.put(key, String.valueOf(DockerUtility.convert2Bool(value)));
                                break;
                            case "memory":
                                paramMap.put(key, value);
                                break;
                            case "memswap":
                                paramMap.put(key, value);
                                break;
                            case "cpushares":
                                paramMap.put(key, value);
                                break;
                            case "cpusetcpus":
                                paramMap.put(key, value);
                                break;
                            default:
                        }
                    }
                }
            }
        }
        return paramMap;
    }

    @Override
    protected String getErrorMessage(int errorCode) {
        return (HttpStatus.SC_INTERNAL_SERVER_ERROR == errorCode) ? "server error " : Constants.UNKNOWN_ERROR;
    }

    @Override
    protected void prepareOutput(ClientResponse response) throws DockerException {
        String errorMsg = null;
        boolean isError = false;

        JSONTokener token = new JSONTokener(response.getEntityInputStream());

        while (!token.end()) {
            char ch;
            do {
                ch = token.next();
                if (ch == '{') {
                    token.back();
                    JSONObject obj = new JSONObject(token);
                    if (!obj.has(PROGRESS_KEY)) {
                        if (obj.has(ERROR_KEY)) {
                            isError = true;
                            errorMsg = obj.getString(ERROR_KEY);
                        } else {
                            LOGGER.info(obj);
                            System.out.println(obj);
                        }
                    }
                }
            } while (ch != 0);
        }
        if (isError) {
            LOGGER.error(errorMsg);
            System.err.println(errorMsg);
            throw new DockerException(ExceptionConstants.GENERIC_ERROR_MSG);
        }

    }

}
