package com.automic.docker.utility;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.docker.constants.Constants;
import com.automic.docker.constants.ExceptionConstants;
import com.automic.docker.exceptions.DockerException;

/**
 * 
 * Utility class to write bytes to a Stream
 *
 */
public class ByteWriter {
    
    private static final Logger LOGGER = LogManager.getLogger(ByteWriter.class);

    private BufferedOutputStream bos = null;

    public ByteWriter(OutputStream output) throws DockerException {
        bos = new BufferedOutputStream(output, Constants.IO_BUFFER_SIZE);
    }

    /**
     * Method to write bytes to Stream
     * @param bytes
     * @throws DockerException
     */
    public void write(byte[] bytes) throws DockerException {
        write(bytes, 0, bytes.length);
    }

    /**
     * Method to write specific part of byte array to Stream
     * @param bytes
     * @param offset
     * @param length
     * @throws DockerException
     */
    public void write(byte[] bytes, int offset, int length) throws DockerException {
        try {
            bos.write(bytes, offset, length);
        } catch (IOException e) {
            LOGGER.error(ExceptionConstants.UNABLE_TO_WRITEFILE, e);
            throw new DockerException(ExceptionConstants.UNABLE_TO_WRITEFILE, e);
        }
    }

    /**
     * Method to write a String to stream
     * @param field
     * @throws DockerException
     */
    public void write(String field) throws DockerException {
        write(field.getBytes());
    }

    /**
     * Method to write a new line character to stream
     * @throws DockerException
     */
    public void writeNewLine() throws DockerException {
        write(System.lineSeparator());
    }

    /**
     * Close the underlying stream
     * @throws DockerException
     */
    public void close() throws DockerException {
        try {
            if (bos != null) {
                bos.close();
            } else {
                LOGGER.error("Stream null!! Unable to close stream");
                throw new DockerException(ExceptionConstants.UNABLE_TO_CLOSE_STREAM);
            }

        } catch (IOException e) {
            LOGGER.error(ExceptionConstants.UNABLE_TO_CLOSE_STREAM, e);
            throw new DockerException(ExceptionConstants.UNABLE_TO_CLOSE_STREAM, e);
        }
    }

    /**
     * Method to flush to stream
     * @throws DockerException
     */
    public void flush() throws DockerException {
        if (bos != null) {
            try {
                bos.flush();
            } catch (IOException e) {
                LOGGER.error(ExceptionConstants.UNABLE_TO_FLUSH_STREAM, e);
                throw new DockerException(ExceptionConstants.UNABLE_TO_FLUSH_STREAM, e);
            }
        }
    }

}
