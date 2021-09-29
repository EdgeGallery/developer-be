package org.edgegallery.developer.exception;

import java.util.ArrayList;
import java.util.List;

public class RestfulRequestException extends RuntimeException{
    private static final long serialVersionUID = 1646444285623052457L;

    private ErrorMessage errMsg;


    /**
     * Constructor to create RestfulRequestException with message.
     *
     * @param message exception message
     */
    public RestfulRequestException(String message) {
        super(message);
    }


    /**
     * Constructor to create RestfulRequestException with message.
     *
     * @param message exception message
     * @param ret retCode
     */
    public RestfulRequestException(String message, int ret) {
        super(message);
        ErrorMessage errorMessage = new ErrorMessage(ret, null);
        errMsg = errorMessage;
    }

    /**
     * Constructor to create RestfulRequestException with retCode and params.
     *
     * @param ret retCode
     * @param args params of error message
     */
    public RestfulRequestException(String message, int ret, Object... args) {
        super(message);
        int length = args == null ? 0 : args.length;
        List<String> params = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            params.add(args[i].toString());
        }
        ErrorMessage errorMessage = new ErrorMessage(ret,params);
        errMsg = errorMessage;
    }

    /**
     * get error message.
     *
     */
    public ErrorMessage getErrMsg() {
        return errMsg;
    }


}
