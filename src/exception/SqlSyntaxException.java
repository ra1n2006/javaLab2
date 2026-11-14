package exception;

/**
 * Исключение, возникающее при ошибках синтаксиса SQL-запроса.
 */
public class SqlSyntaxException extends RuntimeException {

    /**
     * Создаёт исключение с указанным сообщением.
     *
     * @param message описание ошибки
     */
    public SqlSyntaxException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с сообщением и причиной возникновения ошибки.
     *
     * @param message описание ошибки
     * @param cause исходная причина ошибки
     */
    public SqlSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
}