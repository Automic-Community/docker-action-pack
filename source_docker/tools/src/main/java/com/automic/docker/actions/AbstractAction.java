/**
 * 
 */
package com.automic.docker.actions;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.docker.config.HttpClientConfig;
import com.automic.docker.constants.ExceptionConstants;
import com.automic.docker.exceptions.DockerException;
import com.automic.docker.utility.DockerUtility;
import com.automic.docker.utility.URLValidator;
import com.automic.docker.utility.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

/**
 * 
 * 
 * An abstract action which defines common flow of processes to interact with Docker API. Provides default
 * implementation to initialize arguments, validate parameters, prepare API response and exception handling.
 * 
 */
public abstract class AbstractAction {

    /**
     * index for value of connection timeout in parameter array
     */
    protected static final int CONNECTION_TIMEOUT_INDEX = 0;

    /**
     * index for value of read timeout in parameter array
     */
    protected static final int READ_TIMEOUT_INDEX = 1;

    /**
     * index for value of docker URL in parameter array
     */
    protected static final int DOCKER_URL_INDEX = 2;

    /**
     * index for value of certificate path in parameter array
     */
    protected static final int CERTIFICATE_INDEX = 3;

    private static final Logger LOGGER = LogManager.getLogger(AbstractAction.class);

    private static final int BEGIN_HTTP_CODE = 200;
    private static final int END_HTTP_CODE = 300;

    /**
     * Docker URL
     */
    protected String dockerUrl;

    /**
     * Certificate file path
     */
    protected String certFilePath;

    /**
     * Connection timeout in milliseconds
     */
    private int connectionTimeOut;

    /**
     * Read timeout in milliseconds
     */
    private int readTimeOut;

    /**
     * Argument count as expected by an implementation of {@link AbstractAction}
     */
    private int argsCount;

    public AbstractAction(int argsCount) {
        this.argsCount = argsCount;
    }

    /**
     * This method acts as template and decides how an action should proceed.It starts with logging of parameters ,then
     * checking the number of arguments,then initialize the variables like docker URL, read and connection timeouts and
     * filepath.Then it will call the REST API of docker and gets the response which then validated and at last prepares
     * the out either in the form of xml or just a simple sysout.
     * 
     * @param args
     *            Array of arguments
     * @throws DockerException
     *             exception while executing an action
     */
    public final void executeAction(String[] args) throws DockerException {
        Client client = null;
        try {
            logParameters(args);
            checkNoOfargs(args.length);
            trim(args);
            initializeArguments(args);
            validateInputs();
            client = getClient();
            ClientResponse response = executeSpecific(client);
            validateResponse(response);
            prepareOutput(response);
        } finally {
            if (client != null) {
                client.destroy();
            }
        }
    }

    /**
     * Method to log input parameters to the given action
     * 
     * @param args
     *            Array of arguments
     */
    protected abstract void logParameters(String[] args);

    /**
     * Method to trim parameters
     * 
     * @param args
     */
    protected void trim(String[] args) {
        for (int i = 0; i < args.length; i++) {
            args[i] = args[i].trim();
        }
    }

    /**
     * This method is used to initialize the arguments
     * 
     * @param args
     * @throws DockerException
     */
    protected abstract void initialize(String[] args) throws DockerException;

    /**
     * This method is used to validate the inputs to the action. Override this method to validate action specific inputs
     * 
     * @throws DockerException
     */
    protected abstract void validateInputs() throws DockerException;

    /**
     * Method to write action specific logic.
     * 
     * @param client
     *            an instance of {@link Client}
     * @return an instance of {@link ClientResponse}
     * @throws DockerException
     */
    protected abstract ClientResponse executeSpecific(Client client) throws DockerException;

    /**
     * Method to generate Error Message based on error code
     * 
     * @param errorCode
     *            error code
     * @return Error message that describes error code
     */
    protected abstract String getErrorMessage(int errorCode);

    /**
     * Method to prepare output based on Response of an HTTP request to client.
     * 
     * @param response
     *            an instance of {@link ClientResponse}
     * @throws DockerException
     */
    protected abstract void prepareOutput(ClientResponse response) throws DockerException;

    /**
     * Method to check no of arguments are sufficient or not. Throws an exception if count is less than the argument
     * expected.
     * 
     * @param count
     *            No of arguments
     * @throws DockerException
     */
    private void checkNoOfargs(int count) throws DockerException {
        if (count < argsCount) {
            LOGGER.error(ExceptionConstants.INSUFFICIENT_ARGUMENTS);
            throw new DockerException(ExceptionConstants.INSUFFICIENT_ARGUMENTS);
        }
    }

    /**
     * Method to initialize arguments before processing them in an action
     * 
     * @param args
     *            String array of arguments
     * @throws DockerException
     */
    private void initializeArguments(String[] args) throws DockerException {
        connectionTimeOut = DockerUtility.getAndCheckUnsignedValue(args[AbstractAction.CONNECTION_TIMEOUT_INDEX]);
        readTimeOut = DockerUtility.getAndCheckUnsignedValue(args[AbstractAction.READ_TIMEOUT_INDEX]);
        dockerUrl = args[AbstractAction.DOCKER_URL_INDEX];
        certFilePath = (args.length > AbstractAction.CERTIFICATE_INDEX) ? args[AbstractAction.CERTIFICATE_INDEX] : "";
        validateGeneralInputs();
        initialize(args);
    }

    /**
     * Method to validate Input parameters
     * 
     * @throws DockerException
     */
    private void validateGeneralInputs() throws DockerException {
        if (this.connectionTimeOut < 0) {
            LOGGER.error(ExceptionConstants.INVALID_CONNECTION_TIMEOUT);
            throw new DockerException(ExceptionConstants.INVALID_CONNECTION_TIMEOUT);
        }

        if (this.readTimeOut < 0) {
            LOGGER.error(ExceptionConstants.INVALID_READ_TIMEOUT);
            throw new DockerException(ExceptionConstants.INVALID_READ_TIMEOUT);
        }

        if (!URLValidator.validateURL(dockerUrl)) {
            String msg = String.format(ExceptionConstants.INVALID_DOCKER_URL, dockerUrl);
            LOGGER.error(msg);
            throw new DockerException(msg);
        }
    }

    /**
     * Method to create an instance of {@link Client} using docker URL, certificate file path, connection timeout and
     * read timeout.
     * 
     * @return an instance of {@link Client}
     * @throws DockerException
     */
    private Client getClient() throws DockerException {
        try {
            return HttpClientConfig.getClient(new URL(dockerUrl).getProtocol(), this.certFilePath, connectionTimeOut,
                    readTimeOut);
        } catch (MalformedURLException ex) {
            String msg = String.format(ExceptionConstants.INVALID_DOCKER_URL, dockerUrl);
            LOGGER.error(msg, ex);
            throw new DockerException(msg);
        }
    }

    /**
     * Method to validate response from a HTTP client Request. If response is not in range of {@link HttpStatus#SC_OK}
     * and {@link HttpStatus#SC_MULTIPLE_CHOICES}, it throws {@link DockerException} else prints response on console.
     * 
     * @param response
     * @throws DockerException
     */
    private void validateResponse(ClientResponse response) throws DockerException {
        LOGGER.info("Response code for action " + response.getStatus());
        if (!(response.getStatus() >= BEGIN_HTTP_CODE && response.getStatus() < END_HTTP_CODE)) {
            throw new DockerException(getHttpErrorMsg(response));
        }
    }

    /**
     * Method to build docker response from status code and message.
     * 
     * @param status
     *            Status code
     * @param message
     *            Status message
     * @return Docker response code
     */
    private String buildDockerResponse(int status, String message) {
        StringBuilder responseBuilder = new StringBuilder("Docker Response: ");
        responseBuilder.append("StatusCode: [");
        responseBuilder.append(status).append("]");
        if (Validator.checkNotEmpty(message)) {
            responseBuilder.append(" Message: ").append(message);
        }
        return responseBuilder.toString();
    }

    /**
     * Method to get HTTP error message from an intance of {@link ClientResponse}
     * 
     * @param response
     *            an instance of {@link ClientResponse}
     * @return a String of error message
     */
    private String getHttpErrorMsg(ClientResponse response) {
        String msg = response.getEntity(String.class);
        String errMsg = buildDockerResponse(response.getStatus(), msg);
        System.err.println(errMsg);
        LOGGER.error(errMsg);
        return getErrorMessage(response.getStatus());
    }

}
