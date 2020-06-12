package ru.ifmo.rain.busyuk.bank.tests;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import ru.ifmo.rain.busyuk.bank.common.Bank;
import ru.ifmo.rain.busyuk.bank.server.Server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Base {
    protected static Bank bank;
    protected static long testStart, testsStart;
    protected static final int personTestCount = 200, requestsTestCount = 1000, accountsTestCount = 150;

    @BeforeAll
    static void beforeAll() throws RemoteException, NotBoundException {
        Server.main();
        bank = (Bank) Server.registry.lookup("//localhost/bank");
        testsStart = System.currentTimeMillis();
    }

    @BeforeEach
    void beforeEach() {
        testStart = System.currentTimeMillis();
    }

    @AfterEach
    void afterEach() {
        System.out.println("Test done in " + (System.currentTimeMillis() - testStart) + "ms");
    }

    @AfterAll
    static void afterAll() throws RemoteException, NotBoundException {
        Server.registry.rebind("//localhost/bank", bank);
        System.out.println("Base tests done in " + (System.currentTimeMillis() - testsStart) + "ms");
    }

}
