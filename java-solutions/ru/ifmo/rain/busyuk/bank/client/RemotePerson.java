package ru.ifmo.rain.busyuk.bank.client;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RemotePerson implements Person {
    private String name;
    private String surname;
    private String passport;
    private Bank bank;
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
            RemoteAccount account = (RemoteAccount) bank.getAccount(this, id);
            if (account != null) {
                accountMap.put(id, account);
            }
        }
        return accountMap.get(id);
    }
}
