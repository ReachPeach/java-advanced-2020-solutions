package ru.ifmo.rain.busyuk.bank.server;

import ru.ifmo.rain.busyuk.bank.common.Account;
import ru.ifmo.rain.busyuk.bank.common.Bank;
import ru.ifmo.rain.busyuk.bank.common.Person;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Person> persons = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<String>> passportAccounts = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    public Account createAccount(final Person person, final String id) throws RemoteException {
        String accountId = person.getPassport() + ':' + id;
        System.out.println("Creating account " + accountId);
        Account newAccount = new RemoteAccount(accountId);
        if (accounts.putIfAbsent(accountId, newAccount) == null) {
            passportAccounts.get(person.getPassport()).add(id);
            UnicastRemoteObject.exportObject(newAccount, port);
            return newAccount;
        } else {
            return getAccount(person, id);
        }
    }

    public Account getAccount(final Person person, final String id) throws RemoteException {
        return accounts.get(person.getPassport() + ":" + id);
    }

    public Person registerPerson(final String name, final String surname, final String passport) throws RemoteException {
        Person newPerson = new RemotePerson(name, surname, passport, this);
        System.out.println("Creating person " + name + " " + surname + " " + passport);
        if (persons.putIfAbsent(newPerson.getPassport(), newPerson) == null) {
            passportAccounts.put(newPerson.getPassport(), new ConcurrentSkipListSet<>());
            UnicastRemoteObject.exportObject(newPerson, port);
            return newPerson;
        } else {
            return getRemotePerson(passport);
        }
    }

    public Person getRemotePerson(final String passport) {
        return persons.get(passport);
    }

    public Person getLocalPerson(final String passport) throws RemoteException {
        Person person = persons.get(passport);
        Map<String, RemoteAccount> personAccounts = new HashMap<>();
        passportAccounts.get(person.getPassport()).forEach(id -> {
            personAccounts.put(id, (RemoteAccount) (accounts.get(passport + ":" + id)));
        });
        return new LocalPerson(person.getName(), person.getSurname(), person.getPassport(), personAccounts);
    }

}
