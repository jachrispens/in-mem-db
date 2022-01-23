package inmemdb;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

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
     * Captures the state of a variable in a prior transaction.
     */
    enum State {
        SET,
        UNSET
    }

    /**
     * Captures the prior state of a variable, either SET with its value,
     * or unset (value is null).
     */
    class PriorState {

        private State state;

        private String value;

        PriorState(State state, String value) {
            this.state = state;
            this.value = value;
        }
    }

    /**
     * PriorStates captures the states of modified variables prior to
     * the current transaction.  Each open transaction block has an
     * associated prior states object.  The first time a variable is
     * modified in the transaction block, the prior states object is
     * updated to capture the prior state of that variable.  Should
     * a rollback occur, the prior states are applied to the database.
     */
    class PriorStates {

        /**
         * Captures the prior states of modified variables.
         */
        private HashMap<String, PriorState> priorStateOfVariable;

        PriorStates() {
            priorStateOfVariable = new HashMap<>();
        }

        /**
         * Captures the prior state of the indicated variable, but only if the prior state has not already
         * been captured.
         *
         * @param variableName
         */
        public void capturePriorStateIfNecessary(String variableName) {
            if (!priorStateOfVariable.containsKey(variableName)) {
                String value = variableToValue.get(variableName);
                if (value != null) {
                    priorStateOfVariable.put(variableName, new PriorState(State.SET, value));
                } else { // value == null
                    priorStateOfVariable.put(variableName, new PriorState(State.UNSET, ""));
                }
            }
        }

        public void restorePriorStates() {
            for (Map.Entry<String, PriorState> priorStateOfVariable : priorStateOfVariable.entrySet()) {
                String variableName = priorStateOfVariable.getKey();
                PriorState priorState = priorStateOfVariable.getValue();
                switch (priorState.state) {
                    case SET:
                        set(variableName, priorState.value);
                        break;
                    case UNSET:
                        unset(variableName);
                        break;
                }
            }
        }
    }

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

    /**
     * Maintains the prior states for nested transaction blocks.  A new
     * prior state is pushed for each transaction begin, and a prior
     * state is popped and restored for each rollback.
     */
    private Stack<PriorStates> nestedPriorStates;

    public HashMapDatabase() {
        variableToValue = new HashMap<>();
        valueToCount = new HashMap<>();
        nestedPriorStates = new Stack<>();
    }

    @Override
    public void set(String variableName, String value) {
        if (variableName == null || value == null) {
            throw new IllegalArgumentException("Neither argument may be null");
        }
        capturePriorStateIfNecessary(variableName);
        // unset() is called to update the count for the variable, if
        // necessary.
        unset(variableName);
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
        if (variableName == null) {
            throw new IllegalArgumentException("variableName may be null");
        }
        String value = variableToValue.get(variableName);
        Optional<String> answer = Optional.ofNullable(value);
        return answer;
    }

    @Override
    public void unset(String variableName) {
        if (variableName == null) {
            throw new IllegalArgumentException("variableName may not be null");
        }
        capturePriorStateIfNecessary(variableName);
        String value = variableToValue.get(variableName);
        if (value != null) {
            valueToCount.compute(value, (unused, count) -> count - 1);
        }
        variableToValue.remove(variableName);
    }

    @Override
    public int numberOfValuesEqualTo(String value) {
        if (value == null) {
            throw new IllegalArgumentException("value may be null");
        }
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
        nestedPriorStates.push(new PriorStates());
    }

    @Override
    public boolean rollbackTransaction() {
        boolean transactionOpen = !nestedPriorStates.empty();
        if (transactionOpen) {
            PriorStates lastPriorStates = nestedPriorStates.pop();
            lastPriorStates.restorePriorStates();
        }
        return transactionOpen;
    }

    @Override
    public boolean commitAllOpenTransactions() {
        boolean transactionOpen = !nestedPriorStates.empty();
        nestedPriorStates.clear();
        return transactionOpen;
    }

    /**
     * This variable is about to be modified.  Capture the prior state of the value.
     * It is assumed that the null check on variableName as already occurred.
     *
     * @param variableName  The variable to capture.
     */
    private void capturePriorStateIfNecessary(String variableName) {
        if (!nestedPriorStates.empty()) {
            PriorStates currentPriorStates = nestedPriorStates.peek();
            currentPriorStates.capturePriorStateIfNecessary(variableName);
        }
    }
}

