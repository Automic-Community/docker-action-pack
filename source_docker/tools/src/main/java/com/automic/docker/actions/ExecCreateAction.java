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
 * Action class to sets up an exec instance in a specified running container in the given docker environment. Input
 * parameters are an container id and command to be executed. Returns the id of the Exec instance which could later be
 * used to execute that Exec in Docker system.
 *
 */
public class ExecCreateAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(ExecCreateAction.class);

    private static final int NO_OF_ARGS = 6;

    private static final int CONTAINER_ID_INDEX = 4;
    private static final int COMMAND_ID_INDEX = 5;

    private String containerId;
    private String command;

    public ExecCreateAction() {
        super(NO_OF_ARGS);
    }

    /**
     * This method logs the input parameters
     */
    @Override
    protected void logParameters(String[] args) {
        LOGGER.info("Input parameters -->");
        LOGGER.info("Connection Timeout = "
                + ((args.length > CONNECTION_TIMEOUT_INDEX) ? args[CONNECTION_TIMEOUT_INDEX] : ""));
        LOGGER.info("Read-timeout = " + ((args.length > READ_TIMEOUT_INDEX) ? args[READ_TIMEOUT_INDEX] : ""));
        LOGGER.info("Docker-url = " + ((args.length > DOCKER_URL_INDEX) ? args[DOCKER_URL_INDEX] : ""));
        LOGGER.info("Certificate-path = " + ((args.length > CERTIFICATE_INDEX) ? args[CERTIFICATE_INDEX] : ""));
        LOGGER.info("Container Id = " + ((args.length > CONTAINER_ID_INDEX) ? args[CONTAINER_ID_INDEX] : ""));
        LOGGER.info("Command = " + ((args.length > COMMAND_ID_INDEX) ? args[COMMAND_ID_INDEX] : ""));

    }

    @Override
    protected void initialize(String[] args) throws DockerException {
        containerId = args[CONTAINER_ID_INDEX];
        command = args[COMMAND_ID_INDEX];

    }

    /**
     * This method validate the input parameters
     */
    @Override
    protected void validateInputs() throws DockerException {
        if (!Validator.checkNotEmpty(containerId)) {
            LOGGER.error(ExceptionConstants.EMPTY_CONTAINER_ID);
            throw new DockerException(ExceptionConstants.EMPTY_CONTAINER_ID);
        }
        if (!Validator.checkNotEmpty(command)) {
            LOGGER.error(ExceptionConstants.EMPTY_COMMAND);
            throw new DockerException(ExceptionConstants.EMPTY_COMMAND);
        }
    }

    /**
     * This method execute the request by calling docker 'exec create' remote API and return the response in json format
     */
    @Override
    protected ClientResponse executeSpecific(Client client) throws DockerException {

        ClientResponse response = null;

        WebResource webResource = client.resource(dockerUrl).path("containers").path(containerId).path("exec");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("AttachStdin", false);
        jsonObject.put("AttachStdout", true);
        jsonObject.put("AttachStderr", true);

        String[] commands = command.split(",");
        if (commands.length != 0) {
            JSONArray arr = new JSONArray();
            for (int i = 0; i < commands.length; i++) {
                String temp = commands[i].trim();
                if (Validator.checkNotEmpty(temp)) {
                    arr.put(temp);
                }
            }
            jsonObject.put("Cmd", arr);
        }

        LOGGER.info("Calling url " + webResource.getURI());
        LOGGER.info("Passed json : 	" + jsonObject);

        response = webResource.entity(jsonObject.toString(), MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class);

        return response;
    }

    @Override
    protected String getErrorMessage(int errorCode) {
        return (HttpStatus.SC_NOT_FOUND == errorCode) ? "no such container " : Constants.UNKNOWN_ERROR;

    }

    /**
     * {@inheritDoc ExecCreateAction#prepareOutput(ClientResponse)} Outputs the Exec instance id to standard console
     * which would later be read by AE.
     */
    @Override
    protected void prepareOutput(ClientResponse response) throws DockerException {
        JSONObject json = DockerUtility.jsonResponse(response.getEntityInputStream());
        LOGGER.info("Json response " + json.toString());
        String execInstanceId = (String) json.get("Id");
        if (!Validator.checkNotEmpty(execInstanceId)) {
            LOGGER.error(ExceptionConstants.EMPTY_EXEC_ID);
            throw new DockerException(ExceptionConstants.EMPTY_EXEC_ID);
        }
        System.out.println("EXEC_INSTANCE_ID::=" + execInstanceId);
        if (json.has("Warnings")) {
            System.out.println("Warnings ::= " + json.get("Warnings"));
        }

        LOGGER.info("Exec instance id : " + execInstanceId);
    }

}
