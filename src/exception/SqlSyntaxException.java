package exception;

public class SqlSyntaxException extends RuntimeException {
    public SqlSyntaxException(String message) {
        super(message);
    }

    public SqlSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
}
