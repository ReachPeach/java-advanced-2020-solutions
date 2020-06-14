package ru.ifmo.rain.busyuk.bank.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {
    /**
     * Creates a new person's account with specified identifier if it doesn't already exist.
     *
     * @param person owner of new account
     * @param id     account id
     * @return created or existing account.
     */
    Account createAccount(Person person, String id) throws RemoteException;

    /**
     * Returns account by identifier and person owner.
     *
     * @param person owner of account
     * @param id     account id
     * @return account with specified identifier and person owner or {@code null} if such account does not exists.
     */
    Account getAccount(Person person, String id) throws RemoteException;

    /**
     * Registers a new person with specified name, surname and passport if another person with the same passport doesn't
     * already exist.
     *
     * @param name     person name
     * @param surname  person surname
     * @param passport person passport
     * @return registered or existing person or {@code null} if person with same passport already exists.
     */
    Person registerPerson(String name, String surname, String passport) throws RemoteException;

    /**
     * Returns local person by passport.
     *
     * @param passport person passport
     * @return local-usage person with specified passport or {@code null} if such person does not exist
     */
    Person getLocalPerson(String passport) throws RemoteException;

    /**
     * Returns remote person by passport.
     *
     * @param passport person passport
     * @return remote person with specified passport or {@code null} if such person does not exist
     */
    Person getRemotePerson(String passport) throws RemoteException;
}
