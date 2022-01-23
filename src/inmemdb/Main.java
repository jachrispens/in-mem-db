package inmemdb;

import java.io.*;
import java.util.Locale;
import java.util.Optional;

/**
 * This class is the primary driver of the in-memory database.
 * If --test is passed as the first argument, tests are run.  Exceptions
 * are thrown if the tests fail.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("--test")) {
            executeTests();
        } else {
            Reader systemInAsReader = new InputStreamReader(System.in);
            Writer systemOutAsWriter = new OutputStreamWriter(System.out);
            HashMapDatabase database = new HashMapDatabase();
            commandLoop(database, systemInAsReader, systemOutAsWriter);
        }
    }

    /**
     * The command loop for the in-memory database.  Commands are read a line at a time - commands are not
     * case-sensitive.  Arguments are assumed to be separated by single space characters.  The 'END' command exits the
     * loop.  Unrecognized or ill-formed commands result in an error message on System.err, but doesn't exit the loop.
     * Empty commands are ignored.
     *
     * IOExceptions result in a runtime exception being thrown from this method.
     *
     * @param database  The database instance to operate against.
     * @param input     Where input is read from
     * @param output    Where output is written to
     */
    private static void commandLoop(Database database, Reader input, Writer output) {
        BufferedReader bufferedInput = new BufferedReader(input);
        BufferedWriter bufferedOutput = new BufferedWriter(output);

        try {
            boolean endCommandExecuted = false;
            while (!endCommandExecuted) {
                String currentLine = bufferedInput.readLine();
                if (currentLine == null) {
                    System.err.println("End-of-file reached before 'END' command.");
                    break;
                }
                String[] arguments = currentLine.split(" ");
                if (arguments.length > 0) {
                    String command = arguments[0].toLowerCase();
                    switch (command) {
                        case "set":
                            if (arguments.length == 3) {
                                String variableName = arguments[1];
                                String value = arguments[2];
                                database.set(variableName, value);
                            } else {
                                System.err.println("SET requires three arguments: SET variable value");
                            }
                            break;
                        case "get":
                            if (arguments.length == 2) {
                                String variableName = arguments[1];
                                Optional<String> value = database.get(variableName);
                                String outputToWrite = value.orElse("NULL");
                                bufferedOutput.write(outputToWrite, 0, outputToWrite.length());
                                bufferedOutput.write('\n');
                                bufferedOutput.flush();
                            } else {
                                System.err.println("GET requires an argument: GET variable");
                            }
                            break;
                        case "unset":
                            if (arguments.length == 2) {
                                String variableName = arguments[1];
                                database.unset(variableName);
                            } else {
                                System.err.println("UNSET requires an argument: UNSET variable");
                            }
                            break;
                        case "numequalto":
                            if (arguments.length == 2) {
                                String value = arguments[1];
                                int count = database.numberOfValuesEqualTo(value);
                                String outputToWrite = Integer.toString(count);
                                bufferedOutput.write(outputToWrite, 0, outputToWrite.length());
                                bufferedOutput.write('\n');
                                bufferedOutput.flush();
                            } else {
                                System.err.println("UNSET requires an argument: UNSET variable");
                            }
                        case "begin":
                            database.beginTransaction();
                            break;
                        case "rollback":
                            database.rollbackTransaction();
                            break;
                        case "commit":
                            database.commitAllOpenTransactions();
                            break;
                        case "end":
                            endCommandExecuted = true;
                            break;
                        // Empty lines result in an empty command - ignore these.
                        case "":
                            break;
                        default:
                            System.err.println("Unrecognized command: " + command);
                            break;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("IO error - stopping database.", e);
        }
    }

    /**
     * Executes the tests.  The first failing tests results in an exception, exiting the program.
     */
    private static void executeTests() {
        StringReader input = new StringReader("");
        StringWriter output = new StringWriter();
        Database database = new HashMapDatabase();
        commandLoop(database, input, output);
    }
}
