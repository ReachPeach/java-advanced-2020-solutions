package ru.ifmo.rain.busyuk.bank.client;

import ru.ifmo.rain.busyuk.bank.common.Account;
import ru.ifmo.rain.busyuk.bank.common.Bank;
import ru.ifmo.rain.busyuk.bank.common.Person;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    private static void run(String name, String surname, String passport, String accountId, int delta) throws ClientException {
        System.out.println("Request: name <" + name + ">, surname <" + surname + ">, passport <" + passport + ">, " +
                "account id <" + accountId + ">, adding <" + delta + "> money");
        Bank bank;
        try {
            Registry registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
            bank = (Bank) registry.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            throw new ClientException("Bank is not bound", e);
        } catch (RemoteException e) {
            throw new ClientException("Bunk is unreachable", e);
        }

        Person person;
        try {
            person = bank.registerPerson(name, surname, passport);
        } catch (RemoteException e) {
            throw new ClientException("Can't save a new person with passport " + passport, e);
        }
        if (person == null) {
            throw new ClientException("Person with passport <" + passport + "> already registered.");
        }

        Account account;
        try {
            account = person.createAccount(accountId);
        } catch (RemoteException e) {
            throw new ClientException("Can't save a new person's account with passport <" + passport +
                    ">, id <" + accountId + ">", e);
        }

        try {
            System.out.println("Account value: " + account.getAmount());
            System.out.println("Changing...");
            account.setAmount(account.getAmount() + delta);
            System.out.println("Operation done. Checking account...");
            System.out.println("Value in bank: " + bank.getAccount(passport, accountId).getAmount());
            System.out.println("Value in person: " + account.getAmount());
        } catch (RemoteException e) {
            throw new ClientException("Can't reach the bank", e);
        }
    }


    public static void main(String[] args) {
        if (args.length != 5) {
            System.err.println("Incorrect count of arguments!");
            return;
        }
        for (String arg : args) {
            if (arg == null) {
                System.err.println("Arguments mustn't a null!");
                return;
            }
        }
        final String name = args[0];
        final String surname = args[1];
        final String passport = args[2];
        final String accountId = args[3];
        final int delta;
        try {
            delta = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.err.println("Incorrect format of money changing!");
            return;
        }

        try {
            run(name, surname, passport, accountId, delta);
        } catch (ClientException e) {
            System.err.println(e.getMessage());
        }
    }
}