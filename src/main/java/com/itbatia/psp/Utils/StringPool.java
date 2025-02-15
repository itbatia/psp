package com.itbatia.psp.Utils;

/**
 * @author Batsian_SV
 */
public interface StringPool {

    String COLON = ":";
    String MERCHANT_ID = "merchant_id";
    String START_DATE = "start_date";
    String END_DATE = "end_date";
    String TRANSACTION_ID = "transaction_id";
    String USER_ID = "user_id";

    String INVALID_CREDENTIALS = "Invalid credentials";
    String INVALID_IP = "Invalid remote ip address";
    String INVALID_API_KEY = "Invalid api key";
    String INVALID_AUTH = "Header 'Authorization' is missing or doesn't starts with the 'Basic ' prefix";

    String ERROR_MSG = "{\"error_message\": \"%s\"}";
    String MSG_INVALID_AUTH = "{\"error_message\": \"" + INVALID_AUTH + "\"}";
    String MSG_INVALID_CREDENTIALS = "{\"error_message\": \"" + INVALID_CREDENTIALS + "\"}";
    String MSG_INVALID_IP = "{\"error_message\": \"" + INVALID_IP + "\"}";
    String MSG_INVALID_API_KEY = "{\"error_message\": \"" + INVALID_API_KEY + "\"}";
}
