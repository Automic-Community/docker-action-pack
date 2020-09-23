package com.automic.docker.constants;

/**
 * Class contains all the constants used in Docker java application.
 * 
 */
public final class Constants {   
    
    /**
     * https string constant
     */
    public static final String HTTPS = "https";
    
    /**
     * http string constant
     */
    public static final String HTTP = "http";
    
    /**
     * "Unknown Error" string constant
     */
    public static final String UNKNOWN_ERROR = "Unknown Error";
    
    /**
     * int constant for STDOUT code 1
     */
    public static final int STDOUT_CODE = 1;
    
    /**
     * "STDOUT" string constant
     */
    public static final String STDOUT_MSG = "STDOUT";
    
    /**
     * int constant for STDERR code 2
     */
    public static final int STDERR_CODE = 2;
    
    /**
     * "STDERR" string constant
     */
    public static final String STDERR_MSG = "STDERR";
    
    /**
     * int constant for IO Buffer used to buffer the data.
     */
    public static final int IO_BUFFER_SIZE = 4 * 1024;

    private Constants() {
    }

}
