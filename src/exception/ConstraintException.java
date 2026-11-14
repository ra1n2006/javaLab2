package exception;

/**
 * Исключение, возникающее при нарушении ограничений ключей таблицы или данных.
 */
public class ConstraintException extends RuntimeException {

    /**
     * Создаёт исключение с указанным сообщением.
     *
     * @param message описание ошибки
     */
    public ConstraintException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с сообщением и причиной возникновения ошибки.
     *
     * @param message описание ошибки
     * @param cause исходная причина ошибки
     */
    public ConstraintException(String message, Throwable cause) {
        super(message, cause);
    }
}
