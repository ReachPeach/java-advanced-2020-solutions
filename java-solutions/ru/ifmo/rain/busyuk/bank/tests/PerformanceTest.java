package ru.ifmo.rain.busyuk.bank.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import ru.ifmo.rain.busyuk.bank.common.Person;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class PerformanceTest extends Base {
    @Order(-100)
    @Test
    @DisplayName("0.1_Base test: getting accounts")
    void AccountsGettingPerformance() throws RemoteException {
        for (int i = 0; i < personTestCount; i++) {
            bank.createAccount(bank.registerPerson("name", "surname", "01" + i).getPassport(),
                    "1").setAmount(100);
        }
        for (int i = 0; i < personTestCount; i++) {
            assertEquals(bank.getAccount("01" + i, "1").getAmount(), bank.getRemotePerson("01" + i).
                    getAccount("1").getAmount());
        }
    }

    @Order(-100)
    @Test
    @DisplayName("0.2_Base test: creating accounts")
    void AccountsCreatingPerformance() throws RemoteException {
        for (int i = 0; i < personTestCount; i++) {
            bank.registerPerson("name", "surname", "01" + i).createAccount("1").setAmount(100);
        }
        for (int i = 0; i < personTestCount; i++) {
            assertEquals(bank.getAccount("01" + i, "1").getAmount(), bank.getRemotePerson("01" + i).
                    getAccount("1").getAmount());
        }
    }

    @Order(10000)
    @Test
    @DisplayName("2.1_Bank test: Changing account from remote person")
    void RemotePersonChangeTest() throws RemoteException {
        List<Person> remotePersonList = new ArrayList<>(Collections.nCopies(personTestCount, null));
        for (int i = 0; i < personTestCount; i++) {
            remotePersonList.set(i, bank.registerPerson("pname", "psurname",
                    "21" + i));
            for (int j = 0; j < 3; j++) {
                bank.createAccount("21" + i, String.valueOf(j));
                remotePersonList.get(i).getAccount(String.valueOf(j)).setAmount(11 * j + i);
            }
        }
        for (int i = 0; i < personTestCount; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(bank.getAccount("21" + i, String.valueOf(j)).
                        getAmount(), remotePersonList.get(i).getAccount(String.valueOf(j)).getAmount());
            }
        }
    }

    @Order(50000)
    @Test
    @DisplayName("2.2_Bank test: Changing account from bank")
    void RemoteBankChangeTest() throws RemoteException {
        List<Person> remotePersonList = new ArrayList<>(Collections.nCopies(personTestCount, null));
        for (int i = 0; i < personTestCount; i++) {
            remotePersonList.set(i, bank.registerPerson("pname",
                    "psurname", "22" + i));
            for (int j = 0; j < 3; j++) {
                bank.createAccount("22" + i, String.valueOf(j)).setAmount(10 * i + j);
                bank.getAccount("22" + i, String.valueOf(j)).setAmount(10 * (10 * i + j));
            }
        }
        for (int i = 0; i < personTestCount; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(bank.getAccount("22" + i, String.valueOf(j)).
                        getAmount(), remotePersonList.get(i).getAccount(String.valueOf(j)).getAmount());
            }
        }
    }

    @Order(1000000)
    @Test
    @DisplayName("2.3_Bank test: Getting local person")
    void LocalPersonSave() throws RemoteException {
        List<Person> localPersons = new ArrayList<>(Collections.nCopies(personTestCount, null));
        for (int i = 0; i < personTestCount; i++) {
            for (int j = 0; j < personTestCount / 20; j++) {
                bank.createAccount(bank.registerPerson("pname", "psurname", "23" + i).getPassport(),
                        String.valueOf(j)).setAmount(78 * j);
                localPersons.set(i, bank.getLocalPerson("23" + i));
            }
        }
        for (int i = 0; i < personTestCount; i++) {
            for (int j = 0; j < personTestCount / 20; j++) {
                assertEquals(localPersons.get(i).getAccount(String.valueOf(j)).getAmount(),
                        bank.getRemotePerson("23" + i).getAccount(String.valueOf(j)).getAmount());
            }
        }
    }

    @Order(2000000)
    @Test
    @DisplayName("2.4_Bank test: Local person changes")
    void LocalPersonChange() throws RemoteException {
        List<Person> localPersons = new ArrayList<>(Collections.nCopies(personTestCount, null));
        for (int i = 0; i < personTestCount; i++) {
            for (int j = 0; j < personTestCount / 20; j++) {
                bank.createAccount(bank.registerPerson("pname", "psurname", "24" + i).getPassport(),
                        String.valueOf(j)).setAmount(78 * j);
                localPersons.set(i, bank.getLocalPerson("24" + i));
            }
        }
        for (Person localPerson : localPersons) {
            for (int j = 0; j < personTestCount / 20; j++) {
                localPerson.getAccount(String.valueOf(j)).setAmount(78 * j + 11);
            }
        }
        for (int i = 0; i < personTestCount; i++) {
            for (int j = 0; j < personTestCount / 20; j++) {
                assertNotEquals(localPersons.get(i).getAccount(String.valueOf(j)).getAmount(),
                        bank.getAccount("24" + i, String.valueOf(j)).getAmount());
            }
        }
    }

    @Order(3000000)
    @Test
    @DisplayName("2.5_Bank test: chenging in bank not in local person")
    void LocalPersonDontChange() throws RemoteException {
        List<Person> localPersons = new ArrayList<>(Collections.nCopies(personTestCount, null));
        for (int i = 0; i < personTestCount; i++) {
            for (int j = 0; j < personTestCount / 20; j++) {
                bank.createAccount(bank.registerPerson("pname", "psurname", "25" + i).getPassport(),
                        String.valueOf(j)).setAmount(78 * j);
                localPersons.set(i, bank.getLocalPerson("25" + i));
            }
        }
        for (int i = 0; i < personTestCount; i++) {
            for (int j = 0; j < personTestCount / 20; j++) {
                bank.getAccount("25" + i, String.valueOf(j)).setAmount(89 * j + 5);
            }
        }
        for (int i = 0; i < personTestCount; i++) {
            for (int j = 0; j < personTestCount / 20; j++) {
                assertNotEquals(localPersons.get(i).getAccount(String.valueOf(j)).getAmount(),
                        bank.getAccount("25" + i, String.valueOf(j)).getAmount());
            }
        }
    }

    @Order(6000000)
    @Test
    @DisplayName("2.6_Bank test: multiple changing from different copies of remote person")
    void RemotePersonPerformance() throws RemoteException {
        List<Person> remotePersonList = new ArrayList<>(Collections.nCopies(personTestCount, null));
        bank.registerPerson("pname", "psurname", "260");
        for (int i = 0; i < accountsTestCount; i++) {
            bank.createAccount("260", String.valueOf(i)).setAmount(11 * i);
        }
        for (int i = 0; i < personTestCount; i++) {
            remotePersonList.set(i, bank.getRemotePerson("260"));
        }
        for (int i = 0; i < personTestCount; i++) {
            for (int j = 0; j < accountsTestCount / 10; j++) {
                remotePersonList.get(i).getAccount(String.valueOf((i * j + i + j) % accountsTestCount)).
                        setAmount(i * i + j * j);
            }
        }

        for (int i = 0; i < personTestCount; i++) {
            for (int j = 0; j < accountsTestCount; j++) {
                assertEquals(bank.getAccount("260", String.valueOf(j)).getAmount(),
                        remotePersonList.get(i).getAccount(String.valueOf(j)).getAmount());
            }
        }
    }
}
