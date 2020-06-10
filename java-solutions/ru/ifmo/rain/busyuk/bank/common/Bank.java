package ru.ifmo.rain.busyuk.bank.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {
    Account createAccount(Person person, String id) throws RemoteException;

    Account getAccount(Person person, String id) throws RemoteException;

    Person registerPerson(String name, String surname, String passport) throws RemoteException;

    Person getLocalPerson(String passport) throws RemoteException;

    Person getRemotePerson(String passport) throws RemoteException;
}
