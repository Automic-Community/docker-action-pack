/**
 *
 */
package com.automic.docker.actions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.docker.constants.Constants;
import com.automic.docker.constants.ExceptionConstants;
import com.automic.docker.exceptions.DockerException;
import com.automic.docker.utility.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Action class to export the existing container as zip/tar file.It creates the zip/tar at the specified valid location.
 * It will throw error if container id does not exists or file path is invalid
 */
public class ExportContainerAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(ExportContainerAction.class);

    private static final int NO_OF_ARGS = 7;
    private static final int CONTAINER_ID_INDEX = 4;
    private static final int TAR_FOLDER_INDEX = 5;
    private static final int TAR_FILE_INDEX = 6;

    private String containerId;
    private String tarFolderPath;
    private String tarFileName;
    private Path tarFilePath;

    public ExportContainerAction() {
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
        LOGGER.info("Container-id = " + ((args.length > CONTAINER_ID_INDEX) ? args[CONTAINER_ID_INDEX] : ""));
        LOGGER.info("Folder-to-export = " + ((args.length > TAR_FOLDER_INDEX) ? args[TAR_FOLDER_INDEX] : ""));
        LOGGER.info("File-name = " + ((args.length > TAR_FILE_INDEX) ? args[TAR_FILE_INDEX] : ""));

    }

    @Override
    protected void initialize(String[] args) throws DockerException {
        containerId = args[CONTAINER_ID_INDEX];
        tarFolderPath = args[TAR_FOLDER_INDEX];
        tarFileName = args[TAR_FILE_INDEX];
        tarFilePath = Paths.get(tarFolderPath, tarFileName);
    }

    @Override
    protected void validateInputs() throws DockerException {
        if (!Validator.checkNotEmpty(containerId)) {
            LOGGER.error(ExceptionConstants.EMPTY_CONTAINER_ID);
            throw new DockerException(ExceptionConstants.EMPTY_CONTAINER_ID);
        }
        if (!Validator.checkIfValidDirectory(tarFolderPath)) {
            String msg = String.format(ExceptionConstants.INVALID_DIRECTORY, tarFolderPath);
            LOGGER.error(msg);
            throw new DockerException(msg);
        }
        if (!Validator.checkNotEmpty(tarFileName)) {
            String msg = String.format(ExceptionConstants.INVALID_FILE, tarFileName);
            LOGGER.error(msg);
            throw new DockerException(msg);
        }

        if (Validator.checkFileExists(tarFilePath.toString())) {
            String msg = String.format(ExceptionConstants.FILE_ALREADY_EXISTS, tarFilePath);
            LOGGER.error(msg);
            throw new DockerException(msg);
        }

    }

    @Override
    protected ClientResponse executeSpecific(Client client) throws DockerException {

        ClientResponse response = null;

        WebResource webResource = client.resource(dockerUrl).path("containers").path(containerId).path("export");
        LOGGER.info("Calling url " + webResource.getURI());

        response = webResource.accept(MediaType.APPLICATION_OCTET_STREAM).get(ClientResponse.class);

        return response;
    }

    @Override
    protected String getErrorMessage(int errorCode) {

        String msg = null;
        switch (errorCode) {
            case HttpStatus.SC_NOT_FOUND:
                msg = "no such container";
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
     * {@inheritDoc ExecStartAction#prepareOutput(ClientResponse)} It will print the location of .tar file to standard
     * console.
     *
     */
    @Override
    protected void prepareOutput(ClientResponse response) throws DockerException {

        try (InputStream is = response.getEntityInputStream()) {
            Files.copy(is, tarFilePath);
            System.out.println("UC4RB_DKR_EXPORT_FILE ::= " + tarFilePath.toString());
        } catch (IOException e) {
            LOGGER.error("Error while creating tar file for export container ", e);
            throw new DockerException(String.format(ExceptionConstants.UNABLE_TO_WRITE_FILE, tarFilePath.toString()), e);
        }

    }

}
