/**
 * 
 */
package com.automic.docker.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.docker.constants.ExceptionConstants;
import com.automic.docker.exceptions.DockerException;
import com.automic.docker.utility.DockerUtility;
import com.sun.jersey.api.client.ClientHandlerException;

/**
 * Main Class is the insertion point of docker interaction api when called from AE implementation. It delegates the
 * parameters to appropriate action and returns a response code based on output of action.
 * 
 * Following response code are returned by java program 0 - Successful response from Docker API 1 - An exception
 * occurred/Error in response from Docker API 2 - Connection timeout while calling Docker API
 * 
 */
public final class DockerClient {

    private static final Logger LOGGER = LogManager.getLogger(DockerClient.class);

    private static final int RESPONSE_OK = 0;
    private static final int RESPONSE_NOT_OK = 1;
    private static final int RESPONSE_CONNECT_TIMEOUT = 2;

    private static final String RESPONSE_ERROR = "ERROR";

    private static final String ERRORMSG = "Please check the input parameters. For more details refer java logs";

    private static final String CONNECTION_TIMEOUT = "Connection Timeout.";
    private static final String UNABLE_TO_CONNECT = "Unable to connect.";

    private DockerClient() {
    }

    /**
     * Main method which will start the execution of an action on docker. This method will call the DockerClientHelper
     * class which will trigger the execution of specific action and then if action fails this main method will handle
     * the failed scenario and print the error message and system will exit with the respective response code.
     * 
     * @param args
     *            array of Arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("No arguments received...");
            System.exit(1);
        }

        int responseCode = RESPONSE_NOT_OK;

        try {
            DockerClientHelper.executeAction(args);
            responseCode = RESPONSE_OK;
        } catch (ClientHandlerException e) {
            LOGGER.error("Action  FAILED ,possible reason :: ", e);
            responseCode = clientHandlerExceptionHandling(e);
        } catch (DockerException e) {
            System.err.println(DockerUtility.formatMessage(RESPONSE_ERROR, e.getMessage()));
        } catch (Exception e) {
            LOGGER.error("Action  FAILED ,possible reason :: ", e);
            System.err.println(DockerUtility.formatMessage(RESPONSE_ERROR, ExceptionConstants.GENERIC_ERROR_MSG));
        }

        if (responseCode != RESPONSE_OK) {
            System.err.println(DockerUtility.formatMessage(RESPONSE_ERROR, ERRORMSG));
        }

        LOGGER.info("@@@@@@@ Execution ends for action  with response code : " + responseCode);
        System.exit(responseCode);

    }

    /**
     * Method that returns response code based on whether call to Docker API resulted in connection timeout or any other
     * exception. A response code of 2 means connection timeout else for all other exceptions the response code is 1.
     * 
     * @param e
     *            instance of {@link ClientHandlerException}
     * @return the response code
     */
    private static int clientHandlerExceptionHandling(ClientHandlerException e) {
        int responseCode = RESPONSE_NOT_OK;
        Throwable th = e.getCause();

        if (th != null) {
            if (th instanceof java.net.SocketTimeoutException) {
                System.err.println(DockerUtility.formatMessage(RESPONSE_ERROR, CONNECTION_TIMEOUT));
                responseCode = RESPONSE_CONNECT_TIMEOUT;
            } else if (th instanceof java.net.ConnectException) {
                System.err.println(DockerUtility.formatMessage(RESPONSE_ERROR, UNABLE_TO_CONNECT));

            } else {
                String errMsg = (e.getMessage() != null && !e.getMessage().isEmpty() ? e.getMessage()
                        : ExceptionConstants.GENERIC_ERROR_MSG);
                System.err.println(DockerUtility.formatMessage(RESPONSE_ERROR, errMsg));

            }
        } else {
            System.err.println(DockerUtility.formatMessage(RESPONSE_ERROR, ExceptionConstants.GENERIC_ERROR_MSG));

        }
        return responseCode;
    }

}
