package inmemdb;

import java.util.HashMap;
import java.util.Optional;

/**
 * HashMapDatabase is an implementation of Database using a HashMap as
 * the primary data structures.  Furthermore, this implementation
 * is prioritizes the performance of data commands - each should run in
 * roughly constant time.  Transactional commands run in time proportional
 * to the number of values modified.
 *
 * This implementation of Database is not thread-safe.
 */
public class HashMapDatabase implements Database {

    /**
     * variableToValue represents the core of the database, mapping
     * variableNames to values.  Note that null is not permitted
     * as a value.
     */
    private HashMap<String, String> variableToValue;

    /**
     * valueToCount maps a value to the number of occurrences of that
     * value in the database.
     */
    private HashMap<String, Integer> valueToCount;

    public HashMapDatabase() {
        variableToValue = new HashMap<>();
        valueToCount = new HashMap<>();
    }

    @Override
    public void set(String variableName, String value) {
        variableToValue.put(variableName, value);
        valueToCount.compute(value, (unused, count) -> {
            if (count == null) {
                return 1;
            } else {
                return count + 1;
            }
        });
    }

    @Override
    public Optional<String> get(String variableName) {
        String value = variableToValue.get(variableName);
        Optional<String> answer = Optional.ofNullable(value);
        return answer;
    }

    @Override
    public void unset(String variableName) {
        String value = variableToValue.get(variableName);
        if (value != null) {
            valueToCount.compute(value, (unused, count) -> count - 1);
        }
        variableToValue.remove(variableName);
    }

    @Override
    public int numberOfValuesEqualTo(String value) {
        int answer;
        Integer count = valueToCount.get(value);

        if (count != null) {
            answer = count;
        } else {
            answer = 0;
        }
        return answer;
    }

    @Override
    public void beginTransaction() {

    }

    @Override
    public void rollbackTransaction() {

    }

    @Override
    public void commitAllOpenTransactions() {

    }
}
