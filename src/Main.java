import cli.CommandLineInterface;

/**
 * Точка входа в программу.
 */
public class Main {
    /**
     * Запускает консольный интерфейс СУБД.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {

        new CommandLineInterface().run();
    }
}