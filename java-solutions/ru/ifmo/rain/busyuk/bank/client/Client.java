package ru.ifmo.rain.busyuk.bank.client;


import ru.ifmo.rain.busyuk.bank.common.Account;
import ru.ifmo.rain.busyuk.bank.common.Bank;
import ru.ifmo.rain.busyuk.bank.common.Person;
import ru.ifmo.rain.busyuk.bank.server.Server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {
    public static void main(String[] args) {
        if (args.length < 5 || args.length > 6) {
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
        System.out.println("request: name <" + name + ">, surname <" + surname + ">, passport <" + passport + ">, " +
                "account id <" + accountId + ">, adding <" + delta + "> money");
        Bank bank;
        try {
            bank = (Bank) Server.registry.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (RemoteException e) {
            System.out.println("Bunk is unreachable");
            return;
        }
        Person person;
        try {
            person = bank.getRemotePerson(passport);
        } catch (RemoteException e) {
            System.err.println("Can't remote person!");
            return;
        }
        if (person == null) {
            try {
                person = bank.registerPerson(name, surname, passport);
            } catch (RemoteException e) {
                System.err.println("Can't save a new person with passport " + passport);
                return;
            }
        } else
            try {
                if (!person.getName().equals(name) || !person.getSurname().equals(surname)) {
                    System.err.println("Person with this passport already registered");
                    return;
                }
            } catch (RemoteException e) {
                System.err.println("Can't remote person!");
                return;
            }

        Account account;
        try {
            account = bank.getAccount(person, accountId);
        } catch (RemoteException e) {
            System.err.println("Can't remote person's account!");
            return;
        }
        if (account == null) {
            try {
                account = bank.createAccount(person, accountId);
            } catch (RemoteException e) {
                System.err.println("Can't save a new person's account with passport <" + passport + ">, id <" + accountId + ">");
                return;
            }
        }
        try {
            System.out.println("Account value: " + account.getAmount());
            System.out.println("Changing...");
            account.setAmount(account.getAmount() + delta);
            System.out.println("Operation done. Checking account...");
            System.out.println("Value in bank: " + bank.getAccount(person, accountId).getAmount());
            System.out.println("Value in person: " + account.getAmount());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}