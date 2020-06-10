package ru.ifmo.rain.busyuk.bank.common;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Person extends Serializable, Remote {
    String getName() throws RemoteException;

    String getSurname() throws RemoteException;

    String getPassport() throws RemoteException;
}
