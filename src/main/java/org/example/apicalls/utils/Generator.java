package org.example.apicalls.utils;

import static org.example.apicalls.utils.Constants.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.example.api.data.entity.Account;
import org.example.api.data.entity.Card;
import org.example.api.data.entity.Customer;
import org.example.api.data.entity.Transfer;
import org.example.api.data.request.CardRequest;
import org.jetbrains.annotations.NotNull;

public class Generator {

    public static void openHtml() throws IOException {
        try {
            File reportFile = new File("target/cucumber-reports.html");
            String absolutePath = reportFile.getAbsolutePath();
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", absolutePath);
            pb.start();
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    // Strings

    public static String generateRandomString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }

    public static String generateRandomGmail(int length) {
        return generateRandomString(length).concat(GMAIL_DOMAIN);
    }

    public static String generateRandomPassword(int length) {
        return generateRandomString(length).concat(String.valueOf(generateRandomInt(1, 3)));
    }

    @SafeVarargs
    public static <T> T randomlyChooseFrom(T... options) {
        if (options == null || options.length == 0) {
            throw new IllegalArgumentException("At least one option must be provided");
        }

        Random random = new Random();
        return options[random.nextInt(options.length)];
    }

    public static String generateRandomTransferStatus() {
        String[] strings = {"PENDING", "FAILED", "SUCCESSFUL"};
        Random random = new Random();
        return strings[random.nextInt(strings.length)];
    }

    // Numbers

    public static int generateRandomInt(int min, int max) {
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }

    public static double generateRandomDouble(int min, int max) {
        Random r = new Random();
        return min + (max - min) * r.nextDouble();
    }

    public static long generateRandomCardNumber() {
        return ThreadLocalRandom.current().nextLong(minLong, maxLong + 1);
    }

    // Dates

    public static Date generateRandomFutureDate() {
        LocalDate startDate = LocalDate.now();
        long startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endMillis =
                startDate.plusYears(5).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long randomMillis = ThreadLocalRandom.current().nextLong(startMillis, endMillis + 1);
        return new Date(randomMillis);
    }

    public static Date generateLocalDate() {
        LocalDateTime now = LocalDateTime.now();
        return Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
    }

    // Auxiliary

    public static boolean generateRandomBoolean() {
        Random random = new Random();
        return random.nextBoolean();
    }

    public static <T extends Enum<?>> T generateRandomEnum(Class<T> enumClass) {
        T[] enumValues = enumClass.getEnumConstants();
        int randomIndex = new Random().nextInt(enumValues.length);
        return enumValues[randomIndex];
    }

    // Generate Random Entities and s

    public static Account generateRandomAccount(Customer customer, int nCards) {
        Account account = new Account();
        account.setAccountType(randomlyChooseFrom(Account.AccountType.CHECKING_ACCOUNT, Account.AccountType.BUSINESS_ACCOUNT, Account.AccountType.CHILDREN_ACCOUNT, Account.AccountType.SAVINGS_ACCOUNT));
        account.setIsBlocked(generateRandomBoolean());
        account.setIsInDebt(generateRandomBoolean());
        account.setAmount(generateRandomDouble(minAmount, maxAmount));
        account.setCustomer(customer);

        List<Card> cards = new java.util.ArrayList<>(List.of());
        int n = 0;

        if (nCards < 0) {
            throw new IllegalArgumentException("The number of Cards must be higher than 0!");
        }

        while (n != nCards) {
            cards.add(generateRandomCard(account));
            n++;
        }

        account.setCards(cards);

        return account;
    }

    public static Account generateRandomAccount(Customer customer, int nCards, double amount) {
        Account account = new Account();
        account.setAccountType(randomlyChooseFrom(Account.AccountType.CHECKING_ACCOUNT, Account.AccountType.BUSINESS_ACCOUNT, Account.AccountType.CHILDREN_ACCOUNT, Account.AccountType.SAVINGS_ACCOUNT));
        account.setIsBlocked(Boolean.FALSE);
        if (amount < 0){
            account.setIsInDebt(Boolean.TRUE);
        } else {
            account.setIsInDebt(Boolean.FALSE);
        }
        account.setCreationDate(LocalDateTime.now());
        account.setExpirationDate(LocalDateTime.now().plusYears(1));
        account.setAmount(amount);
        account.setCustomer(customer);

        List<Card> cards = new java.util.ArrayList<>(List.of());
        int n = 0;

        if (nCards < 0) {
            throw new IllegalArgumentException("The number of Cards must be higher than 0!");
        }

        while (n != nCards) {
            cards.add(generateRandomCard(account));
            n++;
        }

        account.setCards(cards);

        return account;
    }


    public static Card generateRandomCard(Account account) {
        Card card = new Card();
        card.setType(randomlyChooseFrom(Card.CardType.CREDIT, Card.CardType.DEBIT));
        card.setCvc(generateRandomInt(100, 999));
        card.setNumber(generateRandomCardNumber());
        card.setExpirationDate(generateRandomFutureDate());
        card.setAccount(account);
        card.setIsBlocked(Boolean.FALSE);
        card.setDailyLimit(500.);
        card.setMonthlyLimit(2000.70);

        return card;
    }


    public static Card generateCardType(Account account, CardRequest cardRequest) {
        Card card = new Card();
        Card.CardType type;
        if (cardRequest.getType().equals("Credit") || cardRequest.getType().equals("Debit")){
            type = cardRequest.getType();
        }else {
            type = randomlyChooseFrom(Card.CardType.CREDIT, Card.CardType.DEBIT);
        }
        card.setType(type);
        card.setCvc(generateRandomInt(100, 999));
        card.setNumber(generateRandomCardNumber());
        card.setExpirationDate(generateRandomFutureDate());
        card.setAccount(account);
        card.setIsBlocked(Boolean.FALSE);
        card.setDailyLimit(500.);
        card.setMonthlyLimit(2000.70);

        return card;
    }

    public static Customer generateRandomCustomer(int nCards, int nAccounts) {
        Customer customer = new Customer();
        customer.setCustomerId(generateRandomInt(3, 999));
        customer.setName(generateRandomString(nameLength));
        customer.setSurname(generateRandomString(nameLength));
        customer.setEmail(generateRandomGmail(nameLength));
        customer.setPassword(generateRandomPassword(passwordLength));
        customer.setRole(Customer.UserType.USER);

        List<Account> accounts = new java.util.ArrayList<>(List.of());
        int n = 0;

        if (nAccounts < 0) {
            throw new IllegalArgumentException("The number of Accounts must be higher than 0!");
        }

        while (n != nAccounts) {
            accounts.add(generateRandomAccount(customer, nCards));
            n++;
        }

        customer.setAccounts(accounts);

        return customer;
    }

    public static Customer generateRandomCustomer(int nCards, int nAccounts, double amount) {
        Customer customer = new Customer();
        customer.setName(generateRandomString(nameLength));
        customer.setSurname(generateRandomString(nameLength));
        customer.setEmail(generateRandomGmail(nameLength));
        customer.setPassword(generateRandomPassword(passwordLength));
        customer.setRole(Customer.UserType.USER);

        List<Account> accounts = new java.util.ArrayList<>(List.of());
        int n = 0;

        if (nAccounts < 0) {
            throw new IllegalArgumentException("The number of Accounts must be higher than 0!");
        }

        while (n != nAccounts) {
            accounts.add(generateRandomAccount(customer, nCards, amount));
            n++;
        }

        customer.setAccounts(accounts);

        return customer;
    }


    public static Transfer generateRandomTransfer(Account origin, Account receiver) {
        Transfer transfer = new Transfer();
        Transfer.TransferStatus transferStatus = generateRandomEnum(Transfer.TransferStatus.class);
        Transfer.CurrencyType currencyType = generateRandomEnum(Transfer.CurrencyType.class);

        transfer.setTransferAmount(generateRandomDouble(minAmount, maxAmount));
        transfer.setCurrencyType(currencyType);
        transfer.setTransferStatus(transferStatus);
        transfer.setOriginAccount(origin);
        transfer.setReceivingAccount(receiver);

        return transfer;
    }

}
