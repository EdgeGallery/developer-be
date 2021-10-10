package org.edgegallery.developer.exception;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class IllegalRequestException extends IllegalArgumentException implements Serializable {

    private static final long serialVersionUID = 1311109258952411150L;

    private ErrorMessage errMsg;

    /**
     * Constructor to create IllegalRequestException with message.
     *
     * @param msg exception message
     */
    public IllegalRequestException(String msg) {
        super(msg);
    }

    /**
     * Constructor to create IllegalRequestException with retCode and params.
     *
     * @param ret retCode
     */
    public IllegalRequestException(String msg, int ret) {
        super(msg);
        ErrorMessage errorMessage = new ErrorMessage(ret, null);
        errMsg = errorMessage;
    }

    /**
     * Constructor to create IllegalRequestException with retCode and params.
     *
     * @param ret retCode
     * @param args params of error message
     */
    public IllegalRequestException(String msg, int ret, Object... args) {
        super(msg);
        List<String> params = new ArrayList<>();
        int length = args == null ? 0 : args.length;
        for (int i = 0; i < length; i++) {
            params.add(args[i].toString());
        }
        ErrorMessage errorMessage = new ErrorMessage(ret, params);
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
