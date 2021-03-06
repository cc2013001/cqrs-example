package net.palace.cqrs.bank.account.command;

import net.palace.cqrs.bank.TransactionLeg;
import net.palace.cqrs.bank.TransferFundsRequest;
import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.domain.StringAggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountCommandHandler {

    private EventSourcingRepository<Account> repository;

    @CommandHandler
    public void createAccount(CreateAccountCommand command) {
        repository.add(new Account(command.getAccountRef(), command.getAmount()));
    }

    @CommandHandler
    public void updateAccountBalance(UpdateAccountBalanceCommand command) {
        TransferFundsRequest transferFundsRequest = command.getTransferRequest();
        for (TransactionLeg transactionLeg : transferFundsRequest.getLegs()) {
            Account account = repository.load(new StringAggregateIdentifier(transactionLeg.getAccountRef()));
            account.updateBalance(transactionLeg.getAmount());
        }
    }

    @Autowired
    public void setAccountRepository(EventSourcingRepository<Account> accountRepository) {
        this.repository = accountRepository;
    }
}
