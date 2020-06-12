package ru.ifmo.rain.busyuk.bank.common;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Person extends Serializable, Remote {
    /**
     * Returns person name.
     */
    String getName() throws RemoteException;

    /**
     * Returns person surname.
     */
    String getSurname() throws RemoteException;

    /**
     * Returns person passport.
     */
    String getPassport() throws RemoteException;

    /**
     * Returns person account by identifier.
     *
     * @param id account identifier
     * @return account with specified identifier  or {@code null} if such account does not exists.
     */
    Account getAccount(String id) throws RemoteException;
}
