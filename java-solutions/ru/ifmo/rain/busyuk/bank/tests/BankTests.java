package ru.ifmo.rain.busyuk.bank.tests;

import org.junit.jupiter.api.*;
import ru.ifmo.rain.busyuk.bank.client.Client;
import ru.ifmo.rain.busyuk.bank.client.LocalPerson;
import ru.ifmo.rain.busyuk.bank.client.RemotePerson;
import ru.ifmo.rain.busyuk.bank.common.Account;
import ru.ifmo.rain.busyuk.bank.common.Bank;
import ru.ifmo.rain.busyuk.bank.common.Person;
import ru.ifmo.rain.busyuk.bank.server.RemoteBank;
import ru.ifmo.rain.busyuk.bank.server.Server;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BankTests {
    private static Bank bank;
    private static long testStart, testsStart;
    private static final int personTestCount = 50, copyCount = 500;

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
    static void afterAll() throws MalformedURLException, RemoteException {
        Server.registry.rebind("//localhost/bank", new RemoteBank(8888));
        System.out.println("Tests done in " + (System.currentTimeMillis() - testsStart) + "ms");
    }

    @Order(8)
    @Test
    @DisplayName("11_SingleRequests")
    void firstTest() throws RemoteException {
        for (int i = 1; i < copyCount; i++) {
            String[] args = {"name" + i, "surname" + i, "444", String.valueOf(i), String.valueOf(55 * i)};
            Client.main(args);
        }
        for (int i = 1; i < copyCount; i++) {
            assert (55 * i == bank.getAccount(new RemotePerson("name" + i, "surname" + i,
                    "444"), String.valueOf(i)).getAmount());
        }
    }

    @Order(7)
    @Test
    @DisplayName("12_MultipleRequests_SingleAccount")
    void secondTest() throws RemoteException {
        int x = 0;
        for (int i = 1; i < copyCount; x += i, i++) {
            String[] args = {"name", "surname", "333", "1", String.valueOf(i)};
            Client.main(args);
        }
        assert (x == bank.getAccount(new RemotePerson("name", "surname", "333"),
                "1").getAmount());
    }

    @Order(6)
    @Test
    @DisplayName("13_MultipleRequests_MultipleAccounts")
    void thirdTest() throws RemoteException {
        List<Integer> ans = new ArrayList<>(Collections.nCopies(5, 0));
        for (int i = 1; i <= copyCount; i++) {
            String[] args = {"Name", "Surname", "222", String.valueOf(i % 5), String.valueOf(i)};
            Client.main(args);
            ans.set(i % 5, ans.get(i % 5) + i);
        }
        for (int i = 0; i < 5; i++) {
            assert (ans.get(i) == bank.getAccount(new RemotePerson("Name",
                    "Surname", "222"), String.valueOf(i % 5)).getAmount());
        }
    }

    @Order(5)
    @Test
    @DisplayName("14_MultipleRequests_MultipleAccounts_MultiplePersons")
    void fourthTest() throws RemoteException {
        for (int i = 0; i < personTestCount; i++) {
            for (int j = 0; j < personTestCount; j++) {
                String[] args = {i + "name", i + "surname", "11" + i, String.valueOf(j % 5), String.valueOf(j % 5)};
                Client.main(args);
            }
        }
        for (int i = 0; i < personTestCount; i++) {
            for (int j = 0; j < 5; j++) {
                assert (personTestCount / 5 * (j % 5) == bank.getAccount(new RemotePerson(i + "name", i + "surname",
                        "11" + i), String.valueOf(j)).getAmount());
            }
        }
    }

    @Order(4)

    @Test
    @DisplayName("21_RemotePersonAccountSingleChange")
    void fifthTest() throws RemoteException {
        for (int i = 0; i < personTestCount; i++) {
            Person person = bank.registerPerson("pname", "psurname", "1111" + i);
            for (int j = 0; j < 3; j++) {
                Account account = bank.createAccount(person, String.valueOf(j));
                account.setAmount(10 * i + j);
            }
        }
        for (int i = 0; i < personTestCount; i++) {
            for (int j = 0; j < 3; j++) {
                int x = bank.getAccount(bank.getRemotePerson("1111" + i),
                        String.valueOf(j)).getAmount();
                assert (x == 10 * i + j);
            }
        }
    }

    @Order(3)

    @Test
    @DisplayName("22_RemotePersonAccountMultipleChange")
    void sixthTest() throws RemoteException {
        for (int k = 0; k < 4; k++) {
            for (int i = 0; i < personTestCount; i++) {
                Person person = bank.registerPerson("pname", "psurname", "2111" + i);
                for (int j = 0; j < 3; j++) {
                    Account account = bank.createAccount(person, String.valueOf(j));
                    account.setAmount((10 * i + j) + k * account.getAmount());
                }
            }
        }
        for (int i = 0; i < personTestCount; i++) {
            for (int j = 0; j < 3; j++) {
                int x = bank.getAccount(bank.getRemotePerson("2111" + i),
                        String.valueOf(j)).getAmount();
                assert (x == 16 * (10 * i + j));
            }
        }
    }

    @Order(2)

    @Test
    @DisplayName("23_GetLocalPerson")
    void seventhTest() throws RemoteException {
        List<LocalPerson> localPersons = new ArrayList<>(Collections.nCopies(personTestCount, null));
        for (int i = 0; i < personTestCount; i++) {
            Person person = bank.registerPerson("pname", "psurname", "3111" + i);
            Account account = bank.createAccount(person, String.valueOf(1));
            account.setAmount(1);
            localPersons.set(i, (LocalPerson) bank.getLocalPerson("3111" + i));
        }
        for (int i = 0; i < personTestCount; i++) {
            int x = localPersons.get(i).getAccount("1").getAmount();
            int y = bank.getAccount(new RemotePerson("pname", "psurname", "3111" + i),
                    "1").getAmount();
            assert (x == y);
        }
    }

    @Order(1)
    @Test
    @DisplayName("24_LocalPersonWithoutChanges")
    void eightsTest() throws RemoteException {
        List<LocalPerson> localPersons = new ArrayList<>(Collections.nCopies(personTestCount, null));
        for (int i = 0; i < personTestCount; i++) {
            Person person = bank.registerPerson("pname", "psurname", "8111" + i);
            Account account = bank.createAccount(person, String.valueOf(1));
            account.setAmount(i);
            localPersons.set(i, (LocalPerson) bank.getLocalPerson("8111" + i));
        }
        for (int i = 0; i < personTestCount; i++) {
            bank.getAccount(new RemotePerson("pname", "psurname", "8111" + i),
                    "1").setAmount(personTestCount + i);
        }
        for (int i = 0; i < personTestCount; i++) {
            int x = localPersons.get(i).getAccount("1").getAmount();
            int y = bank.getAccount(new RemotePerson("pname", "psurname", "8111" + i),
                    "1").getAmount();
            assert (x != y);
        }
    }
}

