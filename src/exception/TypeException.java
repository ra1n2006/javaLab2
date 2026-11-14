package exception;

/**
 * Исключение, возникающее при несовместимости типов данных
 * или ошибках преобразования значений.
 */
public class TypeException extends RuntimeException {

    /**
     * Создаёт исключение с указанным сообщением.
     *
     * @param message описание ошибки
     */
    public TypeException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с сообщением и причиной возникновения ошибки.
     *
     * @param message описание ошибки
     * @param cause исходная причина ошибки
     */
    public TypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
