package ru.ifmo.rain.busyuk.bank.client;

import ru.ifmo.rain.busyuk.bank.common.Person;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;

public class LocalPerson implements Person {
    private String name;
    private String surname;
    private String passport;
    private Map<String, LocalAccount> accountMap;

    public LocalPerson(String name, String surname, String passport, Map<String, LocalAccount> accountMap) {
        this.name = name;
        this.surname = surname;
        this.passport = passport;
        this.accountMap = accountMap;
    }

    @Override
    public String getName() throws RemoteException {
        return name;
    }

    @Override
    public String getSurname() throws RemoteException {
        return surname;
    }

    @Override
    public String getPassport() throws RemoteException {
        return passport;
    }

    public Set<String> getAccounts() {
        return accountMap.keySet();
    }

    public LocalAccount getAccount(final String id) {
        return accountMap.get(id);
    }
}
