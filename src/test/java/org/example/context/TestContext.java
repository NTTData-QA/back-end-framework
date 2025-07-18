package org.example.context;

import jakarta.ws.rs.core.Response;
import org.example.api.data.entity.Card;
import org.example.api.data.entity.Customer;
import org.example.apicalls.service.BankService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.ThreadLocal.withInitial;


public enum TestContext {

    CONTEXT;

    private static final String RESPONSE = "RESPONSE";
    private static final String ORIGINID = "ORIGINID";
    private static final String BANKSERVICE = "BANKSERVICE";
    private static final String CUSTOMER = "CUSTOMER";
    private static final String ACCOUNT = "ACCOUNT";
    private static final String CARDS = "CARDS";
    private static final String REGISTERED_EMAIL ="REGISTERED_EMAIL";
    private final ThreadLocal<Map<String, Object>> testContexts = withInitial(HashMap::new);

    public <T> T get(String name) {
        return (T) testContexts.get()
                .get(name);
    }

    public <T> T set(String name, T object) {
        testContexts.get()
                .put(name, object);
        return object;
    }


    public Response getResponse() {
        return get(RESPONSE);
    }

    public Response setResponse(Response response) {
        return set(RESPONSE, response);
    }


    public Integer getOriginID() {
        return get(ORIGINID);
    }

    public Integer setOriginID(Integer originId) {
        return set(ORIGINID, originId);
    }

    public BankService getBankService() {
        return get(BANKSERVICE);
    }

    public BankService setBankService(BankService bankService) {
        return  set(BANKSERVICE, bankService);
    }

    public List<Card> getCards() {
        return get(CARDS);
    }

    public List<Card> setCards(List<Card> cards) {
        return  set(CARDS, cards);
    }

    public Customer getCustomer() {
        return get(CUSTOMER);
    }

    public Customer setCustomer(Customer customer) {
        return  set(CUSTOMER, customer);
    }

    public String getRegisteredEmail(){
        return get(REGISTERED_EMAIL);
    }

    public String setRegisteredEmail(String email){
        return set(REGISTERED_EMAIL,email);
    }

    public void reset() {
        testContexts.get()
                .clear();
    }
}

