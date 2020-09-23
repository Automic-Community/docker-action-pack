package com.automic.docker.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.automic.docker.constants.Constants;
import com.automic.docker.constants.ExceptionConstants;
import com.automic.docker.exceptions.DockerException;
import com.automic.docker.utility.ByteWriter;
import com.automic.docker.utility.DockerUtility;
import com.automic.docker.utility.LogMessage;
import com.automic.docker.utility.LogStreamReader;
import com.automic.docker.utility.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Action class to execute an Exec instance as per the specified Exec id in the given docker environment. Output of this
 * action will be written in a txt file or in job report depending on the inputs provided. If user do not provide folder
 * path then .txt file will be created on the same host where agent is running. This action will fail, if unable to
 * connect with the docker server or Exec instance of corresponding Id does not exist in the docker system.
 */
public class ExecStartAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(ExecStartAction.class);

    private static final int NO_OF_ARGS = 7;

    private static final int EXEC_CONTAINER_ID_INDEX = 4;
    private static final int DETACH_INDEX = 5;
    private static final int SAVE_TO_FILE_INDEX = 6;
    private static final int FOLDER_PATH_INDEX = 7;
    private static final int FILE_NAME_INDEX = 8;

    private String execContainerId;
    private boolean detach;
    private boolean saveToFile;
    private String folderPath;
    private String fileName;

    public ExecStartAction() {
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
        LOGGER.info("Exec Instance Id = "
                + ((args.length > EXEC_CONTAINER_ID_INDEX) ? args[EXEC_CONTAINER_ID_INDEX] : ""));
        LOGGER.info("Detach = " + ((args.length > DETACH_INDEX) ? args[DETACH_INDEX] : ""));
        LOGGER.info("Save to file = " + ((args.length > SAVE_TO_FILE_INDEX) ? args[SAVE_TO_FILE_INDEX] : ""));
        LOGGER.info("Folder Path = " + ((args.length > FOLDER_PATH_INDEX) ? args[FOLDER_PATH_INDEX] : ""));
        LOGGER.info("File Name = " + ((args.length > FILE_NAME_INDEX) ? args[FILE_NAME_INDEX] : ""));
    }

    @Override
    protected void initialize(String[] args) throws DockerException {
        execContainerId = args[EXEC_CONTAINER_ID_INDEX];
        detach = DockerUtility.convert2Bool(args[DETACH_INDEX]);
        saveToFile = DockerUtility.convert2Bool(args[SAVE_TO_FILE_INDEX]);
        if (args.length > FILE_NAME_INDEX) {
            folderPath = args[FOLDER_PATH_INDEX];
            fileName = args[FILE_NAME_INDEX];
        }
    }

    @Override
    protected void validateInputs() throws DockerException {
        if (!Validator.checkNotEmpty(execContainerId)) {
            LOGGER.error(ExceptionConstants.EMPTY_CONTAINER_ID);
            throw new DockerException(ExceptionConstants.EMPTY_CONTAINER_ID);
        }

        if (saveToFile) {
            if (!Validator.checkDirectoryExist(folderPath)) {
                String msg = String.format(ExceptionConstants.DIRECTORY_ALREADY_EXISTS, folderPath);
                LOGGER.error(msg);
                throw new DockerException(msg);
            }

            if (Validator.checkFileExists(new File(folderPath, fileName))) {
                String msg = String.format(ExceptionConstants.FILE_ALREADY_EXISTS,
                        new File(folderPath, fileName).toString());
                LOGGER.error(msg);
                throw new DockerException(msg);
            }
        }
    }

    /**
     * {@inheritDoc ExecStartAction#executeSpecific(Client)} This method execute the request by calling docker 'exec
     * start' remote API and return the response in json format
     */
    @Override
    protected ClientResponse executeSpecific(Client client) throws DockerException {

        ClientResponse response = null;

        WebResource webResource = client.resource(dockerUrl).path("exec").path(execContainerId).path("start");

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("Detach", detach);

        LOGGER.info("Calling url " + webResource.getURI());
        LOGGER.info("Passed json : 	" + jsonObj.toString());

        response = webResource.entity(jsonObj.toString(), MediaType.APPLICATION_JSON_TYPE)
                .accept("application/vnd.docker.raw-stream").post(ClientResponse.class);
        return response;
    }

    @Override
    protected String getErrorMessage(int errorCode) {
        return (HttpStatus.SC_NOT_FOUND == errorCode) ? "no such exec instance" : Constants.UNKNOWN_ERROR;

    }

    /**
     * {@inheritDoc ExecStartAction#prepareOutput(ClientResponse)} This method prepare the output xml by converting the
     * json response into xml which is then written to the file at the path provided
     */
    @Override
    protected void prepareOutput(ClientResponse response) throws DockerException {
        LogStreamReader logStreamReader = new LogStreamReader(response.getEntityInputStream());
        LogMessage logMsg = logStreamReader.next();
        if (logMsg != null) {
            ByteWriter stdErrWriter = new ByteWriter(System.out);
            ByteWriter stdOutWriter = null;
            File fileToWrite = null;
            boolean errorStatus = false;
            try {
                if (saveToFile) {
                    fileToWrite = new File(folderPath, fileName);
                    stdOutWriter = new ByteWriter(new FileOutputStream(fileToWrite));
                } else {
                    stdOutWriter = stdErrWriter;
                }
                do {
                    int streamType = writeMessageToWriter(logMsg, stdOutWriter, stdErrWriter);
                    if (!errorStatus && Constants.STDERR_CODE == streamType) {
                        errorStatus = true;
                    }
                    logMsg = logStreamReader.next();
                } while (logMsg != null);
            } catch (IOException io) {
                LOGGER.error(ExceptionConstants.IO_ERROR, io);
                throw new DockerException(ExceptionConstants.IO_ERROR, io);
            } finally {
                if (stdOutWriter != null) {
                    stdOutWriter.flush();
                }
                stdErrWriter.flush();
                // closing the writer stream
                if (fileToWrite != null) {
                    stdOutWriter.close();
                }
            }
            if (errorStatus) {
                if (fileToWrite != null) {
                    DockerUtility.copyData(fileToWrite, stdErrWriter);
                    if (!fileToWrite.delete()) {
                        LOGGER.warn("Unable to delete the file [" + fileToWrite.toString());
                    }
                    fileToWrite = null;
                }
                String msg = "Received error from Docker.";
                LOGGER.error(msg);
                throw new DockerException(msg);
            } else if (fileToWrite != null) {
                LOGGER.info("OUTPUT_FILE_PATH ::= " + fileToWrite.toString());
                System.out.println("OUTPUT_FILE_PATH ::= " + fileToWrite.toString());
            }
        } else {
            String msg = "No Data found on stdout/stderr streams";
            LOGGER.info(msg);
            System.out.println(msg);
        }
        LOGGER.info("Got the response successfully... ");
    }

    /**
     * Method to write/log response message to an Success Ouput/Errror Stream.
     *
     * @param logMsg
     *            Message to be logged
     * @param out
     *            ByteWriter stream for sucess
     * @param error
     *            ByteWriter stream for error
     * @return 1 if it is a successful response else 2 for error
     * @throws DockerException
     */
    private int writeMessageToWriter(LogMessage logMsg, ByteWriter out, ByteWriter error) throws DockerException {
        String streamType = DockerUtility.getStreamName(logMsg.getStreamId());
        StringBuilder sb = new StringBuilder();
        sb.append(streamType).append(" -> ");
        // writing the response into file
        switch (logMsg.getStreamId()) {
            case Constants.STDOUT_CODE:
                out.write(sb.toString());
                out.write(logMsg.getMessage());
                out.writeNewLine();
                break;
            case Constants.STDERR_CODE:
                error.write(sb.toString());
                error.write(logMsg.getMessage());
                error.writeNewLine();
                break;
            default:
                LOGGER.error("Ignored Stream Id " + logMsg.getStreamId());
                break;
        }
        return logMsg.getStreamId();
    }

}
