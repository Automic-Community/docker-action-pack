package com.automic.docker.utility;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.docker.constants.ExceptionConstants;
import com.automic.docker.exceptions.DockerException;

/**
 * Utility class to validate URLs
 * 
 *
 */
public final class URLValidator {

    private static final Logger LOGGER = LogManager.getLogger(URLValidator.class);

    private static final int PORT_RANGE_START = 0;
    private static final int PORT_RANGE_END = 65535;
    
    private URLValidator() {
    }

    /**
     * Method to validate Docker URL. It validates connection protocol, Port numbers if specified. Also throws a
     * {@link MalformedURLException} if URL is invalid.
     * 
     * @param dockerUrl
     *            String representing Docker URL
     * @return true if URL is valid else false
     */
    public static boolean validateURL(String dockerUrl) throws DockerException {
        URI uri = null;
        
        try {
            uri = URI.create(dockerUrl);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Error while validating docker url :: " , e);
            throw new DockerException(String.format("%s ,%s",
                    String.format(ExceptionConstants.INVALID_DOCKER_URL, dockerUrl), e.getMessage()));

        }

        return (uri != null)
                && checkPort(uri.getPort(), dockerUrl)
                && checkProtocol(uri.getScheme()) ;
    }
    
    private static boolean checkPort(int port, String dockerUrl) {
        return ((port == -1 && dockerUrl.indexOf("-1") == -1) || port >= PORT_RANGE_START || port <= PORT_RANGE_END);
    }

    private static boolean checkProtocol(String protocol) {
        return ("http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol));
    }

}
