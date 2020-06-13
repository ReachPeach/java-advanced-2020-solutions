package ru.ifmo.rain.busyuk.bank.client;

import ru.ifmo.rain.busyuk.bank.common.Person;

import java.util.Map;

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
    public LocalAccount getAccount(final String id) {
        return accountMap.get(id);
    }
}
