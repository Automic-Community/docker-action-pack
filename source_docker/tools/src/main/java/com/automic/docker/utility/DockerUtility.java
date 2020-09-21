package com.automic.docker.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.automic.docker.constants.Constants;
import com.automic.docker.constants.ExceptionConstants;
import com.automic.docker.exceptions.DockerException;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Docker utility class
 * 
 */
public final class DockerUtility {

    private static final String YES = "YES";
    private static final String TRUE = "TRUE";
    private static final String ONE = "1";

    private static final Logger LOGGER = LogManager.getLogger(DockerUtility.class);
    private static final String CHARSET = "UTF-8";

    private static final String ERROR_KEY = "error";
    private static final String PROGRESS_KEY = "progress";
    private static final String STATUS_KEY = "status";

    private DockerUtility() {
    }

    /**
     * 
     * Method to read a file and return its content as a String
     * 
     * @param filePath
     *            file to read
     * @param doDelete
     *            delete file or not
     * @return file content as a String
     * @throws IOException
     */
    public static String readFileFromPath(String filePath, boolean doDelete) throws IOException {

        File file = new File(filePath);
        try {
            InputStream in = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, CHARSET));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }

            reader.close();

            return out.toString();
        } finally {
            if (doDelete && !file.delete()) {
                LOGGER.error("Error deleting file " + file.getName());
            }

        }

    }

    /**
     * Method to create file at location filePath. If some error occurs it will delete the file
     * 
     * @param filePath
     * @param content
     * @throws IOException
     */
    public static void createFile(String filePath, String content) throws DockerException {

        File file = new File(filePath);

        boolean success = true;
        try (FileWriter fr = new FileWriter(file)) {
            fr.write(content);
        } catch (IOException e) {
            success = false;
            LOGGER.error("Error while writing file ", e);
            throw new DockerException(String.format(ExceptionConstants.UNABLE_TO_WRITE_FILE, filePath), e);
        } finally {
            if (!success && !file.delete()) {
                LOGGER.error("Error deleting file " + file.getName());
            }
        }
    }

    /**
     * Method to copy contents of a File to a ByteWriter
     * 
     * @param file
     *            file to read
     * @param dest
     * @throws DockerException
     */
    public static void copyData(File file, ByteWriter dest) throws DockerException {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            copyData(is, dest);
        } catch (FileNotFoundException e) {
            String msg = String.format(ExceptionConstants.UNABLE_TO_COPY_DATA, file);
            LOGGER.error(msg, e);
            throw new DockerException(msg, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException io) {
                    // log the error
                }
            }
        }
    }

    /**
     * Method to copy contents of an {@link InputStream} to a {@link ByteWriter}
     * 
     * @param source
     *            {@link InputStream} to read from
     * @param dest
     *            {@link ByteWriter} to write to
     * @throws DockerException
     */
    public static void copyData(InputStream source, ByteWriter dest) throws DockerException {
        byte[] buffer = new byte[Constants.IO_BUFFER_SIZE];
        int length;
        try {
            while ((length = source.read(buffer)) > 0) {
                dest.write(buffer, 0, length);
            }
        } catch (IOException e) {
            String msg = String.format(ExceptionConstants.UNABLE_TO_COPY_DATA, source);
            LOGGER.error(msg, e);
            throw new DockerException(msg, e);
        }
    }

    /**
     * Method to return Stream Name based on stream id
     * 
     * @param streamId
     * @return
     */
    public static String getStreamName(int streamId) {
        String ret = "Unknown";
        switch (streamId) {
            case Constants.STDOUT_CODE:
                ret = Constants.STDOUT_MSG;
                break;
            case Constants.STDERR_CODE:
                ret = Constants.STDERR_MSG;
                break;
            default:
                break;
        }
        return ret;
    }

    /**
     * Method to convert a stream into Json object
     * 
     * @param is
     *            input stream
     * @return JSONObject
     */
    public static JSONObject jsonResponse(InputStream is) {
        return new JSONObject(new JSONTokener(is));

    }

    /**
     * Method to convert a json to xml and then write it to a File specified. It also appends a Root tag to xml.
     * 
     * @param json
     * @param filePath
     * @param rootTag
     * @throws DockerException
     */
    public static void json2xml(JSONObject json, String filePath, String rootTag) throws DockerException {
        createFile(filePath, org.json.XML.toString(JSON2XMLAdapter.adoptJsonToXml(json), rootTag));
    }

    /**
     * Method to convert a stream to xml and then write it to a File specified. It also appends a Root tag to xml.
     * 
     * @param is
     * @param filePath
     * @param rootTag
     * @throws DockerException
     */
    public static void jsonResponse2xml(InputStream is, String filePath, String rootTag) throws DockerException {
        json2xml(jsonResponse(is), filePath, rootTag);
    }

    /**
     * Method to convert a stream into Json Array
     * 
     * @param is
     *            input stream
     * @return JSONArray
     */
    public static JSONArray jsonArrayResponse(InputStream is) {
        return new JSONArray(new JSONTokener(is));
    }

    /**
     * Method to convert a Json Array to xml and then write it to a File specified. It also appends a Root tag and a
     * child tag for each entry to json array
     * 
     * @param filePath
     *            file to write to
     * @param jsonArray
     * @param rootTag
     *            root tag
     * @param childTag
     *            child tag for each entry to json array
     * @throws DockerException
     */
    public static void jsonArray2xml(JSONArray jsonArray, String filePath, String rootTag, String childTag)
            throws DockerException {

        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append("<").append(rootTag).append(">");
        for (int i = 0; i < jsonArray.length(); i++) {
            xmlStringBuilder.append(org.json.XML.toString(
                    JSON2XMLAdapter.adoptJsonToXml((JSONObject) jsonArray.get(i)), childTag));
        }
        xmlStringBuilder.append("</").append(rootTag).append(">");
        createFile(filePath, xmlStringBuilder.toString());
    }

    /**
     * 
     Method to convert a Json Array to xml and then write it to a File specified. It also appends a Root tag and a
     * child tag for each entry to json array
     * 
     * @param filePath
     *            file to write to
     * @param jsonArray
     * @param rootTag
     *            root tag
     * @param childTag
     *            child tag for each entry to json array
     * @throws DockerException
     */
    public static void jsonArrayResponse2xml(InputStream is, String filePath, String rootTag, String childTag)
            throws DockerException {
        jsonArray2xml(jsonArrayResponse(is), filePath, rootTag, childTag);
    }

    /**
     * Method to append type to message in format "type | message"
     * 
     * @param type
     * @param message
     * @return
     */
    public static String formatMessage(String type, String message) {
        StringBuffer sb = new StringBuffer();
        sb.append(type).append(" | ").append(message);
        return sb.toString();
    }

    /**
     * 
     * Method to get unsigned integer value if presented by a string literal.
     * 
     * @param value
     * @return
     */
    public static int getAndCheckUnsignedValue(String value) {
        int i = -1;
        if (Validator.checkNotEmpty(value)) {
            try {
                i = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                i = -1;
            }
        }
        return i;
    }

    /**
     * Method to convert YES/NO values to boolean true or false
     * 
     * @param value
     * @return true if YES, 1
     */
    public static boolean convert2Bool(String value) {
        boolean ret = false;
        if (Validator.checkNotEmpty(value)) {
            String upperCaseValue = value.toUpperCase();
            ret = YES.equals(upperCaseValue) || TRUE.equals(upperCaseValue) || ONE.equals(upperCaseValue);
        }
        return ret;
    }

    public static String getQueryParamValue(String queryParamExpr, String queryParam) {
        if (Validator.checkNotEmpty(queryParamExpr)) {
            int index = queryParamExpr.indexOf('=');
            if (index != -1) {
                String key = queryParamExpr.substring(0, index).trim();
                if (queryParam.equalsIgnoreCase(key)) {
                    return queryParamExpr.substring(index + 1, queryParamExpr.length()).trim();
                }
            }
        }
        return null;
    }

    public static void readJsonResponseFromStream(ClientResponse response) throws DockerException {
        long start = System.currentTimeMillis();

        long end = 0;
        String status = null;
        String errorMsg = null;
        boolean isError = false;

        try {
            JSONTokener token = new JSONTokener(response.getEntityInputStream());

            while (!token.end()) {
                char ch;
                do {
                    ch = token.next();
                    if (ch == '{') {
                        token.back();
                        JSONObject obj = new JSONObject(token);

                        @SuppressWarnings("unchecked")
                        Set<String> keySet = obj.keySet();

                        if (keySet != null && !keySet.contains(PROGRESS_KEY)) {
                            LOGGER.info(obj.toString());
                            if (keySet.contains(STATUS_KEY)) {
                                status = obj.getString(STATUS_KEY);
                            }
                            if (keySet.contains(ERROR_KEY)) {
                                isError = true;
                                errorMsg = obj.get(ERROR_KEY).toString();

                            }
                            break;
                        }
                    }
                } while (ch != 0);
            }
            System.out.println(status);
            if (isError) {
                LOGGER.error(errorMsg);
                System.err.println(errorMsg);
                throw new DockerException(ExceptionConstants.GENERIC_ERROR_MSG);
            }

        } finally {

            end = System.currentTimeMillis();
            LOGGER.info("Total time taken " + (end - start) / 1000 + " seconds ");

        }
    }

}
