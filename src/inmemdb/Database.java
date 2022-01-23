package inmemdb;

import java.util.Optional;

/**
 * Database is the core abstraction of the in-memory database.
 * The supported operations are described below.
 */
public interface Database {
    /**
     * Sets the variable named by variableName to the value
     * specified by value in the current transaction block.
     * If the value is already set, it is overwritten.
     *
     * Neither variableName nor value may be null - an
     * IllegalArgumentException is thrown if they are. Otherwise, there
     * are no constraints imposed on either the variableName
     * or value by this interface.  (That is, users are free to
     * constrain either as necessary.)
     *
     * @param variableName  The name of the variable to set.
     * @param value         The value to associate with the variable.
     */
    void set(String variableName, String value);

    /**
     * Gets the value most recently associated with variableName in
     * the current transaction block.  If there is no association in
     * the current transaction block, the association in the parent block is
     * considered.
     *
     * variableName nor value may be null - an IllegalArgumentException is
     * thrown if it is.
     *
     * @param variableName  The name of the variable to retrieve.
     */
    Optional<String> get(String variableName);

    /**
     * Removes the variable named by variableName in the current transaction
     * block.  If the variable has no association in the current block, unset
     * is a no-op.
     *
     * variableName nor value may be null - an IllegalArgumentException is
     * thrown if it is.
     *
     * @param variableName  The name of the variable to dissociate
     */
    void unset(String variableName);

    /**
     * Returns the number of variables that are set to the specified value,
     * relative to the current transaction block.  If there are none,
     * 0 is returned.
     *
     * @param value  The value whose associates are to be counted.
     * @return  The number of variable associated with the provided value.
     */
    int numberOfValuesEqualTo(String value);

    /**
     * Starts a new transaction block.  All associations from the containing block
     * are 'visible' in the newly created block.
     */
    void beginTransaction();

    /**
     * Undoes all changes made since the most recent beginTransaction().
     *
     * @return true if a transaction was rolled back, false if no transaction is open.
     */
    boolean rollbackTransaction();

    /**
     * Makes all changes by all open transactions permanent.  That is, these changes can
     * no longer be undone.
     */
    boolean commitAllOpenTransactions();
}
