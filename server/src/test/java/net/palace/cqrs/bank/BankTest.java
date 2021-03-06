package net.palace.cqrs.bank;

import net.palace.cqrs.bank.config.ApplicationConfig;
import net.palace.cqrs.bank.config.QueryModelConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static net.palace.cqrs.bank.MoneyUtils.toMoney;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationConfig.class, QueryModelConfig.class}, loader = AnnotationConfigContextLoader.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BankTest {
    static final String SAVINGS_ACCOUNT_1 = "MyAccounts:A:EUR";

    static final String SPENDING_ACCOUNT_1 = "MyAccounts:B:EUR";

    static final String SAVINGS_ACCOUNT_2 = "MyAccounts:C:SEK";

    static final String SPENDING_ACCOUNT_2 = "MyAccounts:D:SEK";

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransferService transferService;



    @Test
    public void transferMoney() {
        accountService.createAccount(SAVINGS_ACCOUNT_1, toMoney("1000.00 EUR"));
        accountService.createAccount(SPENDING_ACCOUNT_1, toMoney("0.00 EUR"));

        assertEquals(accountService.getBalance(SAVINGS_ACCOUNT_1), toMoney("1000.00 EUR"));
        assertEquals(accountService.getBalance(SPENDING_ACCOUNT_1), toMoney("0.00 EUR"));

        transferService.transferFunds(TransferFundsRequest.builder()
                .transactionRef("T1").transactionType("testing")
                .accountRef(SAVINGS_ACCOUNT_1).amount(toMoney("-5.00 EUR"))
                .accountRef(SPENDING_ACCOUNT_1).amount(toMoney("5.00 EUR"))
                .build());

        transferService.transferFunds(TransferFundsRequest.builder()
                .transactionRef("T2").transactionType("testing")
                .accountRef(SAVINGS_ACCOUNT_1).amount(toMoney("-10.50 EUR"))
                .accountRef(SPENDING_ACCOUNT_1).amount(toMoney("10.50 EUR"))
                .build());

        assertEquals(accountService.getBalance(SAVINGS_ACCOUNT_1), toMoney("984.50 EUR"));
        assertEquals(accountService.getBalance(SPENDING_ACCOUNT_1), toMoney("15.50 EUR"));

        List<Transaction> t1 = transferService.findTransactions(SAVINGS_ACCOUNT_1);
        assertNotNull(t1);
        assertEquals(t1.size(), 2);
        assertEquals(t1.iterator().next().getLegs().size(), 1);

        List<Transaction> t2 = transferService.findTransactions(SPENDING_ACCOUNT_1);
        assertNotNull(t2);
        assertEquals(t2.size(), 2);
        assertEquals(t2.iterator().next().getLegs().size(), 1);
    }

    @Test
    public void transferMoneyUsingMultiLeggedTransaction() {
        accountService.createAccount(SAVINGS_ACCOUNT_1, toMoney("1000.00 EUR"));
        accountService.createAccount(SPENDING_ACCOUNT_1, toMoney("0.00 EUR"));

        assertEquals(accountService.getBalance(SAVINGS_ACCOUNT_1), toMoney("1000.00 EUR"));
        assertEquals(accountService.getBalance(SPENDING_ACCOUNT_1), toMoney("0.00 EUR"));

        transferService.transferFunds(TransferFundsRequest.builder()
                .transactionRef("T3").transactionType("testing")
                .accountRef(SAVINGS_ACCOUNT_1).amount(toMoney("-5.00 EUR"))
                .accountRef(SPENDING_ACCOUNT_1).amount(toMoney("5.00 EUR"))
                .accountRef(SAVINGS_ACCOUNT_1).amount(toMoney("-10.50 EUR"))
                .accountRef(SPENDING_ACCOUNT_1).amount(toMoney("10.50 EUR"))
                .accountRef(SAVINGS_ACCOUNT_1).amount(toMoney("-2.00 EUR"))
                .accountRef(SPENDING_ACCOUNT_1).amount(toMoney("1.00 EUR"))
                .accountRef(SPENDING_ACCOUNT_1).amount(toMoney("1.00 EUR"))
                .build());

        assertEquals(accountService.getBalance(SAVINGS_ACCOUNT_1),
                toMoney("982.50 EUR"));
        assertEquals(accountService.getBalance(SPENDING_ACCOUNT_1),
                toMoney("17.50 EUR"));
    }

    @Test
    public void transferMoneyUsingMultiLeggedAndMultiCurrencyTransactions() {
        accountService.createAccount(SAVINGS_ACCOUNT_1, toMoney("1000.00 EUR"));
        accountService.createAccount(SPENDING_ACCOUNT_1, toMoney("0.00 EUR"));
        accountService.createAccount(SAVINGS_ACCOUNT_2, toMoney("1000.00 SEK"));
        accountService.createAccount(SPENDING_ACCOUNT_2, toMoney("0.00 SEK"));

        assertEquals(accountService.getBalance(SAVINGS_ACCOUNT_2), toMoney("1000.00 SEK"));
        assertEquals(accountService.getBalance(SPENDING_ACCOUNT_2), toMoney("0.00 SEK"));

        transferService.transferFunds(TransferFundsRequest.builder()
                .transactionRef("T4").transactionType("testing")
                .accountRef(SAVINGS_ACCOUNT_1).amount(toMoney("-5.00 EUR"))
                .accountRef(SPENDING_ACCOUNT_1).amount(toMoney("5.00 EUR"))
                .accountRef(SAVINGS_ACCOUNT_2).amount(toMoney("-10.50 SEK"))
                .accountRef(SPENDING_ACCOUNT_2).amount(toMoney("10.50 SEK"))
                .build());

        assertEquals(accountService.getBalance(SAVINGS_ACCOUNT_1), toMoney("995.00 EUR"));
        assertEquals(accountService.getBalance(SPENDING_ACCOUNT_1), toMoney("5.00 EUR"));
        assertEquals(accountService.getBalance(SAVINGS_ACCOUNT_2), toMoney("989.50 SEK"));
        assertEquals(accountService.getBalance(SPENDING_ACCOUNT_2), toMoney("10.50 SEK"));
    }
}
