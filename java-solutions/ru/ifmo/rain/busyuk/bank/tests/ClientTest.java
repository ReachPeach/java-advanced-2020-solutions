package ru.ifmo.rain.busyuk.bank.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import ru.ifmo.rain.busyuk.bank.client.Client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientTest extends Base {
    @Order(0)
    @Test
    @DisplayName("1.1_Client test: Single request per person")
    void SingleRequests() throws RemoteException {
        for (int i = 1; i <= requestsTestCount; i++) {
            String[] args = {"pname", "psurname", "11", String.valueOf(i), String.valueOf(55 * i)};
            Client.main(args);
        }
        for (int i = 1; i <= requestsTestCount; i++) {
            assertEquals(55 * i, bank.getRemotePerson("11")
                    .getAccount(String.valueOf(i)).getAmount());
        }
    }

    @Order(10)
    @Test
    @DisplayName("1.2_Client test: Multiple requests per person")
    void MultipleRequests() throws RemoteException {
        int x = 0;
        for (int i = 1; i <= requestsTestCount; x += i, i++) {
            String[] args = {"name", "surname", "12", "1", String.valueOf(i)};
            Client.main(args);
        }
        assertEquals(x, bank.getRemotePerson("12").getAccount("1").getAmount());
    }

    @Order(50)
    @Test
    @DisplayName("1.3_Client test: Multiple requests per person for different accounts")
    void MultipleRequestsForSingleAccount() throws RemoteException {
        List<Integer> ans = new ArrayList<>(Collections.nCopies(5, 0));
        for (int i = 1; i <= personTestCount; i++) {
            String[] args = {"Name", "Surname", "13", String.valueOf(i % 5), String.valueOf(i)};
            Client.main(args);
            ans.set(i % 5, ans.get(i % 5) + i);
        }
        for (int i = 0; i < 5; i++) {
            assertEquals(ans.get(i), bank.getRemotePerson("13").getAccount
                    (String.valueOf(i % 5)).getAmount());
        }
    }

    @Order(100)
    @Test
    @DisplayName("1.4_Client test: Multiple requests for multiple accounts per person")
    void MultipleAccountsPerformance() throws RemoteException {
        List<List<Integer>> ans = new ArrayList<>(Collections.nCopies(personTestCount, null));
        for (int i = 0; i < personTestCount; i++) {
            ans.set(i, new ArrayList<>(Collections.nCopies(personTestCount / 20, 0)));
            for (int j = 0; j < personTestCount / 20; j++) {
                String[] args = {"name", "surname", "14" + i, String.valueOf(j % 5), String.valueOf(j % 7)};
                Client.main(args);
                ans.get(i).set(j % 5, ans.get(i).get(j % 5) + j % 7);
            }
        }
        for (int i = 0; i < personTestCount; i++) {
            for (int j = 0; j < 5; j++) {
                assertEquals(ans.get(i).get(j), bank.getAccount(bank.getRemotePerson("14" + i).getPassport(),
                        String.valueOf(j)).getAmount());
            }
        }
    }

    @Order(500)
    @Test
    @DisplayName("1.5_Client test: multiple passport registration")
    void RegistrationPerformance() throws RemoteException {
        String[] args = {"name", "surname", "15" + 0, "1", "100"};
        Client.main(args);
        for (int i = 1; i <= personTestCount; i++) {
            args = new String[]{"name" + i, "surname", "15" + 0, "1", "100"};
            Client.main(args);
            args = new String[]{"name", "surname" + i, "15" + 0, "1", "100"};
            Client.main(args);
            args = new String[]{"name" + i, "surname" + i, "15" + 0, "1", "100"};
            Client.main(args);
        }
        assertEquals(100, bank.getRemotePerson("150").getAccount("1").getAmount());
    }

}
