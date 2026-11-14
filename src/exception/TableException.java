package exception;

/**
 * Исключение, возникающее при ошибках работы с таблицами
 * или при выполнении операций над ними.
 */
public class TableException extends RuntimeException {

    /**
     * Создаёт исключение с указанным сообщением.
     *
     * @param message описание ошибки
     */
    public TableException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с сообщением и причиной возникновения ошибки.
     *
     * @param message описание ошибки
     * @param cause исходная причина ошибки
     */
    public TableException(String message, Throwable cause) {
        super(message, cause);
    }
}
