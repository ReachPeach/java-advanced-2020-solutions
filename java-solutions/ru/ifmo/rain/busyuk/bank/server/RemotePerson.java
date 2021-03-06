package ru.ifmo.rain.busyuk.bank.server;

import ru.ifmo.rain.busyuk.bank.common.Account;
import ru.ifmo.rain.busyuk.bank.common.Bank;
import ru.ifmo.rain.busyuk.bank.common.Person;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RemotePerson implements Person {
    private final String name;
    private final String surname;
    private final String passport;
    private final Bank bank;
    private Map<String, RemoteAccount> accountMap;

    public RemotePerson(String name, String surname, String passport, Bank bank) {
        this.name = name;
        this.surname = surname;
        this.passport = passport;
        this.bank = bank;
        this.accountMap = new ConcurrentHashMap<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public String getPassport() {
        return passport;
    }

    @Override
    public Account getAccount(String id) throws RemoteException {
        if (!accountMap.containsKey(id)) {
            RemoteAccount account = (RemoteAccount) bank.getAccount(passport, id);
            if (account != null) {
                accountMap.put(id, account);
            }
        }
        return accountMap.get(id);
    }

    @Override
    public Account createAccount(String id) throws RemoteException {
        if (!accountMap.containsKey(id)) {
            accountMap.put(id, (RemoteAccount) bank.createAccount(passport, id));
        }
        return accountMap.get(id);
    }
}
