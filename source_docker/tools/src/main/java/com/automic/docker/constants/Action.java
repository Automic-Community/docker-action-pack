package com.automic.docker.constants;

import com.automic.docker.actions.AbstractAction;
import com.automic.docker.actions.ActionFactory;


/**
 * Enum that defines constants which are placeholder for actions. When an implementation of {@link AbstractAction} is
 * created we also create a constant in this enum. Mapping is defined in {@link ActionFactory}
 */
public enum Action {

    SEARCH_IMAGE, GET_VERSION, INSPECT_IMAGE, LIST_IMAGES, CREATE_IMAGE,
    CREATE_CONTAINER, START_CONTAINER, STOP_CONTAINER, LIST_CONTAINERS, 
    REMOVE_CONTAINER, QUIET_REMOVE_CONTAINER, RESTART_CONTAINER, KILL_CONTAINER, PAUSE_CONTAINER,
    UNPAUSE_CONTAINER, WAIT_CONTAINER,INSPECT_CONTAINER,EXEC_CREATE,
    START_EXEC,EXPORT_CONTAINER,REMOVE_IMAGE,BUILD_IMAGE,IMAGE_HISTORY,IMPORT_CONTAINER;

    public static String getActionNames() {
        Action[] actions = Action.values();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < actions.length; i++) {
            sb.append(actions[i].name());
        }
        sb.append("]");
        return sb.toString();
    }

}
