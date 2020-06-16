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

    private boolean containsNull(Object[] args) {
        for (Object arg : args) {
            if (arg == null) return true;
        }
        return false;
    }

    private boolean isPersonValid(Person person, String name, String surname) throws RemoteException {
        return person.getName().equals(name) && person.getSurname().equals(surname);
    }

    public RemoteBank(final int port) {
        this.port = port;
    }

    public Account createAccount(final String passport, final String id) throws RemoteException {
        Object[] args = {passport, id};
        if (containsNull(args)) {
            return null;
        }
        String accountId = passport + ':' + id;
        System.out.println("Creating account " + accountId);
        Account newAccount = new RemoteAccount(accountId);
        if (!accounts.containsKey(accountId)) {
            UnicastRemoteObject.exportObject(newAccount, port);
            accounts.put(accountId, newAccount);
            passportAccounts.get(passport).add(id);
            return newAccount;
        } else {
            return getAccount(passport, id);
        }
    }

    public Account getAccount(final String passport, final String id) {
        Object[] args = {passport, id};
        if (containsNull(args)) {
            return null;
        }
        return accounts.get(passport + ":" + id);
    }

    public Person registerPerson(final String name, final String surname, final String passport) throws RemoteException {
        Object[] args = {name, surname, passport};
        if (containsNull(args)) {
            return null;
        }
        Person person = persons.get(passport);
        if (person != null) {
            return isPersonValid(person, name, surname) ? person : null;
        }
        Person newPerson = new RemotePerson(name, surname, passport, this);
        System.out.println("Creating person " + name + " " + surname + " " + passport);
        UnicastRemoteObject.exportObject(newPerson, port);
        persons.putIfAbsent(newPerson.getPassport(), newPerson);
        passportAccounts.put(newPerson.getPassport(), new ConcurrentSkipListSet<>());
        return newPerson;
    }

    public Person getRemotePerson(final String passport) {
        Object[] args = {passport};
        if (containsNull(args)) {
            return null;
        }
        return persons.get(passport);
    }

    public Person getLocalPerson(final String passport) throws RemoteException {
        Object[] args = {passport};
        if (containsNull(args)) {
            return null;
        }
        Person person = getRemotePerson(passport);
        List<RemoteException> exceptionList = new ArrayList<>();
        Map<String, RemoteAccount> personAccounts = new HashMap<>();
        for (String id : passportAccounts.get(person.getPassport())) {
            try {
                personAccounts.put(id,
                        (new RemoteAccount(accounts.get(passport + ":" + id))));
            } catch (RemoteException e) {
                exceptionList.add(e);
            }
        }
        if (!exceptionList.isEmpty()) {
            RemoteException remoteException = exceptionList.get(0);
            exceptionList.stream().skip(1).forEach(remoteException::addSuppressed);
            throw remoteException;
        }
        return new LocalPerson(person.getName(), person.getSurname(), person.getPassport(), personAccounts);
    }

}
