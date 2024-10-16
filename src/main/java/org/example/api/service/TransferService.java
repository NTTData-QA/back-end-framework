package org.example.api.service;

import org.example.api.data.entity.Account;
import org.example.api.data.entity.Transfer;
import org.example.api.data.repository.AccountRepository;
import org.example.api.data.repository.TransferRepository;
import org.example.api.data.request.TransferRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransferService {

    @Autowired
    private TransferRepository transferRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountService accountService;

    public Double setCurrencyAndReturnAmount(TransferRequest transferRequest, Transfer transfer){
        // Currency
        transfer.setCurrencyType(transferRequest.getCurrencyType());

        // Amount
        Double transferAmount = transferRequest.getTransferAmount();
        transfer.setTransferAmount(transferAmount);
        return transferAmount;
    }

    public ResponseEntity<String> transferOperation(Account senderAccount, Account receiverAccount ,Double transferAmount, Transfer transfer){
        // Check - if receiving account is blocked
        //       - if requesting account is in debt
        //       - if there is not enough money in requesting account
        if (receiverAccount.getIsBlocked() || senderAccount.getIsInDebt()
                || senderAccount.getAmount() < transferAmount) {
            transfer.setTransferStatus(Transfer.TransferStatus.FAILED);
            transferRepository.save(transfer);
            return ResponseEntity.badRequest()
                    .body("Transfer can not be done. Not enough money or blocked receiver.");
        } else if(transferAmount <= 0){
            transfer.setTransferStatus(Transfer.TransferStatus.FAILED);
            transferRepository.save(transfer);
            return ResponseEntity.badRequest()
                    .body("Money to transfer must be greater than 0");

        }
        // Transfer the money
        senderAccount.setAmount(senderAccount.getAmount() - transferAmount);
        receiverAccount.setAmount(receiverAccount.getAmount() + transferAmount);

        //Check none of the accounts is in debt
        senderAccount.setIsInDebt(accountService.checkAccountInDebt(senderAccount));
        receiverAccount.setIsInDebt(accountService.checkAccountInDebt(receiverAccount));

        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);

        // Save transfer in database
        transfer.setTransferStatus(Transfer.TransferStatus.SUCCESSFUL);
        transferRepository.save(transfer);

        // Check operation has been done successfully
        return ResponseEntity.ok().body("Transfer made successfully");
    }

    public List<Transfer> findByReceivingAccount(Integer accountId) {return transferRepository.findByReceivingAccount_AccountId(accountId) ;}

    public List<Transfer> getTransferByAccountId(Integer originAccountId){
        return transferRepository.findByOriginAccount_AccountId(originAccountId);
    }

}
