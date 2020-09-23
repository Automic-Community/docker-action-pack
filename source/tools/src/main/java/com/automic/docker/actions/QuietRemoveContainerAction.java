/**
 *
 */
package com.automic.docker.actions;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;

import com.automic.docker.exceptions.DockerException;
import com.automic.docker.utility.DockerUtility;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Action class to quiet remove an existing container based on the specified container id.
 * Allow Remove container with ENDED_OK if:
 * - Container is not exist (return warning)
 * - Container is paused (try to un-paused then remove it)
 */
public class QuietRemoveContainerAction extends RemoveContainerAction {
    private static final String RESPONSE_INFO = "INFO";
    private static final String RESPONSE_WARNING = "RESPONSE_ERROR";

    private static final int CONTAINER_ID_INDEX = 4;

    private static final String PAUSING_CONTAINER_MESSAGE = "Unpause the container before stopping";

    private String containerId;


    @Override
    protected void initialize(String[] args) throws DockerException {
        super.initialize(args);
        containerId = args[CONTAINER_ID_INDEX];
    }

    @Override
    protected ClientResponse executeSpecific(Client client) throws DockerException {
        ClientResponse response = super.executeSpecific(client);
        
        if (response.getStatus() == HttpStatus.SC_NO_CONTENT)
        	return response;

        //By pass error if it 's on whitelist
        String responseMessage = response.getEntity(String.class).replace("{\"message\":\"", "").replace("\"}", "");
        if (response.getStatus() == HttpStatus.SC_NOT_FOUND) {
            System.out.println(DockerUtility.formatMessage(RESPONSE_WARNING, responseMessage));
            response.setStatus(HttpStatus.SC_OK);
        } else if (response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR &&
                responseMessage.contains(PAUSING_CONTAINER_MESSAGE)) {
            System.out.println(DockerUtility.formatMessage(RESPONSE_WARNING, responseMessage));
            System.out.println(DockerUtility.formatMessage(RESPONSE_INFO, "Try to unpause container first, then remove it again."));

            response = client.resource(dockerUrl).path("containers").path(containerId).path("unpause")
                    .accept(MediaType.APPLICATION_JSON).post(ClientResponse.class);

            if (response.getStatus() >= HttpStatus.SC_OK && response.getStatus() < HttpStatus.SC_MULTIPLE_CHOICES) {
                //retry
                response = super.executeSpecific(client);
            }
        }
        return response;
    }
}
