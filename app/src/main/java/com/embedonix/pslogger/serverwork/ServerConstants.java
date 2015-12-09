package com.embedonix.pslogger.serverwork;

/**
 * This class contains URL and constants required to communicate with logger server
 * <p/>
 * <p/>
 * <p/>
 * Pervasive Systems Research Group
 * University Twente
 * Created by Saeid on 13/07/2014.
 */
public class ServerConstants {

    /**
     * Change this if necessary as this is the base web address to the server
     */

    public static final String PF_USER_FULLNAME = "name";
    public static final String PF_USERNAME = "username";
    public static final String PF_PASSWORD = "password";
    public static final String PF_EMAIL = "email";
    public static final String PF_PHOTO = "photo";
    public static final String PF_FILENAME = "filename";
    public static final String PF_FILE_CONTENT = "filecontent";
    public static final String PF_FILE_TYPE = "filetype";
    public static final String TYPE_APP_LOG_FILE = "app_log_file";

    public static final String URL_CHECK_FOR_EXISTING_VALUES =
            "android/check_for_existing_values.php";
    public static final String ACTION = "action";
    public static final String CHECK_USERNAME_EXISTS = "CHECK_USERNAME_EXISTS";

    public static final String CHECK_EMAIL_EXIST = "CHECK_EMAIL_EXIST";


    public static final String RESULT_VALUE_EXIST = "VALUE_EXIST";
    public static final String RESULT_VALUE_NOT_EXIST = "VALUE_NOT_EXIST";
    public static final String URL_REGISTRATION_PROCESS =
            "android/register_process.php";

    public static final String URL_RECEIVE_LOGS =
            "android/receive_log_file.php";
    public static final String URL_TEST_SERVER =
            "android/test_server_access.php";

    public static final String URL_LOGIN =
            "android/login.php";
    public static final String REGISTRATION_OK = "REGISTRATION_OK";
    public static final String INVALID_FORM_DATA = "INVALID_FORM_DATA";
    public static final String USERNAME_EXIST = "USERNAME_EXIST";
    public static final String UPLOAD_OK = "UPLOAD_OK";
    public static final String SERVER_OK = "SERVER_OK";
    public static final String FILE_EXIST = "FILE_EXIST";
    public static final String LOGIN_OK = "LOGIN_OK";
}
