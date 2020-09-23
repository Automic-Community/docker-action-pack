/**
 * 
 */
package com.automic.docker.client;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.docker.actions.AbstractAction;
import com.automic.docker.actions.ActionFactory;
import com.automic.docker.constants.Action;
import com.automic.docker.constants.ExceptionConstants;
import com.automic.docker.exceptions.DockerException;


/**
 * Helper class to delegate request to specific Action based on input arguments .
 * */
public final class DockerClientHelper {

    private static final Logger LOGGER = LogManager.getLogger(DockerClientHelper.class);
    
    private DockerClientHelper() {
    }

    /**
     * Method to delegate parameters to an instance of {@link AbstractAction} based on the value of Action parameter. 
     * @param args array of String args
     * @throws DockerException 
     */
    public static void executeAction(String[] args) throws DockerException {
        String action = args[0].trim();
        if (action.isEmpty()) {
            LOGGER.error(ExceptionConstants.INVALID_ACTION);
            throw new DockerException(ExceptionConstants.INVALID_ACTION);
        }
        action = action.toUpperCase();
        LOGGER.info("Execution starts for action [" + action + "]...");
        AbstractAction useraction = ActionFactory.getAction(Action.valueOf(action));
        useraction.executeAction(Arrays.copyOfRange(args, 1, args.length));
    }
}
