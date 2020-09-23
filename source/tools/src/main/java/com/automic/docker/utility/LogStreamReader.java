package com.automic.docker.utility;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.docker.constants.ExceptionConstants;
import com.automic.docker.exceptions.DockerException;

/**
 * Utility class to read the inputstream given by docker remote API.
 * 
 */
public class LogStreamReader {

    private static final Logger LOGGER = LogManager.getLogger(LogStreamReader.class);
    private static final int HEADER_SIZE = 8;
    private static final int SIZE_INDEX = 4;
    private DataInputStream dis = null;

    public LogStreamReader(InputStream in) {
        this.dis = new DataInputStream(in);
    }

    /**
     * Method to read a chunk of Data and return it as a {@link LogMessage}
     * 
     * @return an instance of {@link LogMessage}
     * @throws DockerException
     */
    public LogMessage next() throws DockerException {
        byte[] headerByte = new byte[HEADER_SIZE];
        int totalBytes = 0;
        int i = 0;
        while (totalBytes < HEADER_SIZE) {
            LOGGER.info("reading first 8 bytes from inputstream..");
            try {
                i = dis.read(headerByte, totalBytes, HEADER_SIZE - totalBytes);
            } catch (IOException e) {
                LOGGER.error(ExceptionConstants.UNABLE_TO_READ_INPUTSTREAM, e);
                throw new DockerException(ExceptionConstants.UNABLE_TO_READ_INPUTSTREAM, e);
            }
            if (i == -1) {
                break;
            }
            totalBytes += i;
        }

        if (totalBytes < HEADER_SIZE) {
            LOGGER.error("Could not found first 8 bytes..so terminating the flow!!");
            return null;
        }

        ByteBuffer bb = ByteBuffer.wrap(headerByte);
        byte streamId = bb.get();

        bb.position(SIZE_INDEX);
        int dataSize = bb.getInt();
        byte[] message = new byte[dataSize];

        try {
            dis.readFully(message);
        } catch (IOException e) {
            LOGGER.error(ExceptionConstants.UNABLE_TO_READ_INPUTSTREAM, e);
            throw new DockerException(ExceptionConstants.UNABLE_TO_READ_INPUTSTREAM, e);
        }
        return new LogMessage(streamId, message);
    }

    /**
     * Method to close the underlying input stream.
     * 
     * @throws DockerException
     *             if there was an error closing stream
     */
    public void close() throws DockerException {
        if (dis != null) {
            try {
                dis.close();
            } catch (IOException e) {
                LOGGER.error(ExceptionConstants.UNABLE_TO_CLOSE_INPUTSTREAM, e);
                throw new DockerException(ExceptionConstants.UNABLE_TO_CLOSE_INPUTSTREAM, e);
            }
        }
    }

}
