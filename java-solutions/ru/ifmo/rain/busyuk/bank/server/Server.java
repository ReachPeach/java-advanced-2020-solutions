package ru.ifmo.rain.busyuk.bank.server;

import ru.ifmo.rain.busyuk.bank.common.Bank;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    private final static int PORT = 8888;
    public static Registry registry;

    static {
        try {
            registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public static void main(final String... args) {
        final Bank bank = new RemoteBank(PORT);
        try {
            UnicastRemoteObject.exportObject(bank, PORT);
            registry.bind("//localhost/bank", bank);
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            //ignored
        }
        System.out.println("Server started");
    }

}
