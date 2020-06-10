package ru.ifmo.rain.busyuk.bank.client;

import ru.ifmo.rain.busyuk.bank.common.Person;

import java.rmi.RemoteException;

public class RemotePerson implements Person {
    private String name;
    private String surname;
    private String passport;

    public RemotePerson(String name, String surname, String passport) {
        this.name = name;
        this.surname = surname;
        this.passport = passport;
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
    public String getPassport()  {
        return passport;
    }
}
