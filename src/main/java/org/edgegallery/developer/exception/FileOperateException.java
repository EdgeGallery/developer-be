package org.edgegallery.developer.exception;

public class FileOperateException extends DomainException {

    private static final long serialVersionUID = 1311109258952411151L;

    private ErrorMessage errMsg;

    public FileOperateException(String message) {
        super(message);
    }

    /**
     * Constructor to create FileOperateException with retCode and params.
     *
     * @param ret retCode
     */
    public FileOperateException(String msg, int ret) {
        super(msg);
        ErrorMessage errorMessage = new ErrorMessage(ret, null);
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
