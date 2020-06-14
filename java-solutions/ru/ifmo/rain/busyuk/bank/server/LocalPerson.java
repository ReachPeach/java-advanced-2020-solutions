package ru.ifmo.rain.busyuk.bank.server;

import ru.ifmo.rain.busyuk.bank.common.Account;
import ru.ifmo.rain.busyuk.bank.common.Person;

import java.util.Map;

public class LocalPerson implements Person {
    private final String name;
    private final String surname;
    private final String passport;
    private final Map<String, RemoteAccount> accountMap;

    public LocalPerson(String name, String surname, String passport, Map<String, RemoteAccount> accountMap) {
        this.name = name;
        this.surname = surname;
        this.passport = passport;
        this.accountMap = accountMap;
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
    public Account getAccount(final String id) {
        return accountMap.get(id);
    }

    @Override
    public Account createAccount(String id) {
        if (accountMap.containsKey(id)) {
            return accountMap.get(id);
        } else {
            return accountMap.put(id, new RemoteAccount(id));
        }
    }
}
