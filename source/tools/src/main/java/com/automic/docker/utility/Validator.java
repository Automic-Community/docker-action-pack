package com.automic.docker.utility;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;


/**
 * 
 * Utility Class that has many utility methods to put validations on {@link Object}, {@link String}, {@link File}.
 *
 */
public final class Validator {
    
    private Validator() {
    }

    /**
     * Method to check if an Object is null
     * @param field
     * @return true or false
     */
    public static boolean checkNotNull(Object field) {
        return (field != null);
    }

    /**
     * Method to check if a String is not empty
     * @param field
     * @return true if String is not empty else false
     */
    public static boolean checkNotEmpty(String field) {
        return (field != null && !field.isEmpty());
    }

    /**
     * Method to check if file represented by a string literal exists or not
     * @param filePath
     * @return true or false
     */
    public static boolean checkFileExists(String filePath) {
        return (checkNotEmpty(filePath) && checkFileExists(new File(filePath)));
    }

    /**
     * Method to check if file represented by a string literal exists or not
     * @param filePath
     * @return true or false
     */
    public static boolean checkFileExists(File file) {
        return (checkNotNull(file) && file.exists());
    }

    /**
     * Method to check if file represented by a string literal exists or not
     * @param filePath
     * @return true or false
     */
    public static boolean checkFileExistsAndIsFile(String filePath) {
        return (checkFileExists(filePath) && new File(filePath).isFile());
    }

    /**
     * Method to check if Parent Folder exists or not
     * @param filePath
     * @return true if exists else false
     */
    public static boolean checkFileFolderExists(String filePath) {
        boolean ret = false;
        if (checkNotEmpty(filePath)) {
            File tmpFile = new File(filePath);
            File folderPath = tmpFile.getParentFile();
            ret = checkNotNull(folderPath) && folderPath.exists() && !folderPath.isFile();
        }
        return ret;
    }

    /**
     *  Method to check if path specified by string literal is a Directory and it exists or not
     * @param dir
     * @return true or false
     */
    public static boolean checkDirectoryExist(String dir) {
        boolean ret = false;
        if (checkNotEmpty(dir)) {
            File tmpFile = new File(dir);
            ret = tmpFile.exists() && !tmpFile.isFile();
        }
        return ret;
    }

    /**
     * Method to check if path specified by string literal is a Directory or not.
     * @param dirPath
     * @return true or false
     */
    public static boolean checkIfValidDirectory(String dirPath) {
        return (!dirPath.isEmpty() && Files.isDirectory(Paths.get(dirPath)));
    }

    /**
     * Method to check if a text matches the given pattern
     * 
     * @param pattern pattern to match
     * @param text String to match the pattern
     * @return true or false
     */
    public static boolean isValidText(String pattern, String text) {
        return (Pattern.matches(pattern, text));
    }

}
