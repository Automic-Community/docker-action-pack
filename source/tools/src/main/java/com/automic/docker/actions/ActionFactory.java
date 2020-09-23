package com.automic.docker.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.docker.constants.Action;
import com.automic.docker.exceptions.DockerException;

/**
 * Factory class to create instances of implementations of {@link AbstractAction}. This class will create the instances
 * based on {@link Action} parameter. It throws an Exception if no matching implementation could be found.
 */
public final class ActionFactory {

    private static final Logger LOGGER = LogManager.getLogger(AbstractAction.class);

    private ActionFactory() {
    }

    /**
     * Method to return instance of implementation of {@link AbstractAction} based on value of enum {@link Action}
     * passed.
     * 
     * @param enumAction
     * @return an implementation of {@link AbstractAction}
     * @throws DockerException
     *             if no matching implementation could be found
     */
    public static AbstractAction getAction(Action enumAction) throws DockerException {

        AbstractAction action = null;

        switch (enumAction) {
            case SEARCH_IMAGE:
                action = new SearchImageAction();
                break;
            case GET_VERSION:
                action = new GetVersionAction();
                break;
            case INSPECT_IMAGE:
                action = new InspectImageAction();
                break;
            case LIST_IMAGES:
                action = new ListImagesAction();
                break;
            case CREATE_IMAGE:
                action = new CreateImageAction();
                break;
            case CREATE_CONTAINER:
                action = new CreateContainerAction();
                break;
            case START_CONTAINER:
                action = new StartContainerAction();
                break;
            case STOP_CONTAINER:
                action = new StopContainerAction();
                break;
            case LIST_CONTAINERS:
                action = new ListContainersAction();
                break;
            case REMOVE_CONTAINER:
                action = new RemoveContainerAction();
                break;
            case QUIET_REMOVE_CONTAINER:
                action = new QuietRemoveContainerAction();
                break;
            case RESTART_CONTAINER:
                action = new RestartContainerAction();
                break;
            case KILL_CONTAINER:
                action = new KillContainerAction();
                break;
            case PAUSE_CONTAINER:
                action = new PauseConatinerAction();
                break;
            case UNPAUSE_CONTAINER:
                action = new UnpauseContainerAction();
                break;
            case WAIT_CONTAINER:
                action = new WaitContainerAction();
                break;
            case EXPORT_CONTAINER:
                action = new ExportContainerAction();
                break;
            case INSPECT_CONTAINER:
                action = new InspectContainerAction();
                break;
            case EXEC_CREATE:
                action = new ExecCreateAction();
                break;
            case START_EXEC:
                action = new ExecStartAction();
                break;
            case REMOVE_IMAGE:
                action = new RemoveImageAction();
                break;
            case BUILD_IMAGE:
                action = new BuildImageAction();
                break;
            case IMAGE_HISTORY:
                action = new ImageHistoryAction();
                break;
            case IMPORT_CONTAINER:
                action = new ImportContainerAction();
                break;
            default:
                String msg = "Invalid Action.. Please enter valid action " + Action.getActionNames();
                LOGGER.error(msg);
                throw new DockerException(msg);
        }
        return action;
    }

}
