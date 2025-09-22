package org.example.apicalls.apiconfig;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.example.api.data.request.*;
import org.springframework.security.core.context.SecurityContext;
import org.example.api.data.entity.Account;
import org.example.api.data.entity.Card;
import org.example.api.data.entity.Customer;
import org.example.api.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import jakarta.ws.rs.core.Response;
import org.springframework.web.bind.annotation.RequestBody;

import javax.print.attribute.standard.Media;
import java.util.List;
import java.util.Optional;

public interface BankAPI {

    // AÑADIR TODOS LOS ENDPOINTS DE NUESTRA API

    /*
    @GET
    @Path("/v1/storage/files")
    @Produces(MediaType.APPLICATION_JSON)
    Response getV1StorageFiles(
            @HeaderParam("Authorization") String authorization, @QueryParam("q") String appId, @QueryParam("kind") String kind, @QueryParam("per_page") Integer perPage);

    @GET
    @Path("/rest/v1/info/platforms/{automation_api}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getV1InfoPlatformsAutomationApi(
            @HeaderParam("Authorization") String authorization,
            @PathParam("automation_api") String automation_api);
     */

    @GET
    @Path("/api/account/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Response accountById(
            @PathParam("id") Integer id);

    @GET
    @Path("/api/accounts/{customerId}")
    @Produces(MediaType.APPLICATION_JSON)
    Response accountsByCustomer(
            @PathParam("customerId") Integer customerId);

    @GET
    @Path("/api/accounts")
    @Produces(MediaType.APPLICATION_JSON)
    Response getUserAccounts(@Context HttpServletRequest request);

    @GET
    @Path("/api/accounts/amount")
    @Produces(MediaType.APPLICATION_JSON)
    Response getUserAmount(@Context HttpServletRequest request);

    @GET
    @Path("/api/amount/{accountId}")
    @Produces(MediaType.APPLICATION_JSON)
    Response amountOfAccount(@PathParam("accountId") Integer accountId);

    @POST
    @Path("/api/account/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response createAccount(Account newAccount, @Context HttpServletRequest request);

    @POST
    @Path("/public/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    Response addCustomer(Customer nuevoCust);

    @POST
    @Path("/public/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    Response login(LoginRequest logInRequest, @Context HttpServletRequest request);

    @POST
    @Path("/public/logout")
    @Produces(MediaType.TEXT_PLAIN)
    Response logout(@Context HttpServletRequest request);

    @GET
    @Path("/api/BDcards")
    @Produces(MediaType.APPLICATION_JSON)
    List<Card> getAllCards();

    @GET
    @Path("/api/card/{cardId}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getCardById(@PathParam("cardId") Integer cardId);

    @GET
    @Path("/api/cards/{accountId}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getCardsByAccountId(@PathParam("accountId") Integer accountId);

    @POST
    @Path("/api/card/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    Response newCard(CardRequest cardRequest);

    @GET
    @Path("/api/cards")
    @Produces(MediaType.APPLICATION_JSON)
    Response getCards();

    @GET
    @Path("/api/customer/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getCustomerById(@PathParam("id") Integer id);

    @GET
    @Path("/api/customer/email/{email}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getCustomerByEmail(@PathParam("email") String email);

    @GET
    @Path("/api/customers")
    @Produces(MediaType.APPLICATION_JSON)
    Response getAllCustomers();

    @DELETE
    @Path("/public/customer/{email}")
    @Produces(MediaType.TEXT_PLAIN)
    Response deleteCustomer(@PathParam("email") String email);

    @DELETE
    @Path("/api/transfer/{id}")
    //@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    Response deleteTransfer(@PathParam("id") int id);

    @DELETE
    @Path("/api/account/delete/{accountId}")
    @Produces(MediaType.TEXT_PLAIN)
    Response deleteAccount(@PathParam("accountId") int accountId);

    @DELETE
    @Path("/api/account/delete/customer/{customerId}")
    @Produces(MediaType.TEXT_PLAIN)
    Response deleteAccountsOfCustomer(@PathParam("customerId") int customerId);

    @DELETE
    @Path("/api/account/delete")
    @Produces(MediaType.TEXT_PLAIN)
    Response deleteLoggedUser(@Context HttpServletRequest request);

//    @PATCH
//    @Path("/api/account/withdraw/{accountId}")

    // Endpoints creados para los Withdraws
    @POST
    @Path("/api/withdraw")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response createWithdraw(WithdrawRequest request, @Context HttpServletRequest httpServletRequest);

    @GET
    @Path("/api/withdraws")
    @Produces(MediaType.APPLICATION_JSON)
    Response listMyWithdraws(@Context HttpServletRequest httpServletRequest);

    @GET
    @Path("/api/withdraws/card/{cardId}")
    @Produces(MediaType.APPLICATION_JSON)
    Response listWithdrawsByCard(@PathParam("cardId") Integer cardId, @Context HttpServletRequest httpServletRequest);

    @GET
    @Path("/api/withdraws/account/{accountId}")
    @Produces(MediaType.APPLICATION_JSON)
    Response listWithdrawsByAccount(@PathParam("accountId") Integer accountId, @Context HttpServletRequest httpServletRequest);

    @DELETE
    @Path("/api/withdraws/delete/card/{cardId}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    Response deleteWithdrawsById(@PathParam("cardId") int cardId);

    @DELETE
    @Path("/api/card/delete")
    @Produces(MediaType.TEXT_PLAIN)
    Response deleteCardsOfLoggedUser(@Context HttpServletRequest request);

    @DELETE
    @Path("/api/card/delete/customer/{customerId}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    Response deleteCardsOfCustomer(@PathParam("customerId") int customerId);

    @DELETE
    @Path("/api/card/delete/account/{accountId}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    Response deleteCardsOfAccounts(@PathParam("accountId") int accountId);

    @POST
    @Path("/api/transfer/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    Response localTransfer(TransferRequest transferRequest, @Context HttpServletRequest request);

    @GET
    @Path("/api/transfer/{transferId}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getTransferById(@PathParam("transferId") Integer transferId, @Context HttpServletRequest request);

    @PATCH
    @Path("/api/customer/update/nameandsurname")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    Response updateNameAndSurname(@RequestBody UpdateRequest updateRequest, @Context HttpServletRequest httpServletRequest);

    @PATCH
    @Path("/api/customer/update/email")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    Response updateEmail(@RequestBody UpdateRequest updateRequest, @Context HttpServletRequest httpServletRequest);

    @PATCH
    @Path("/api/customer/update/password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    Response updatePassword(@RequestBody UpdateRequest updateRequest, @Context HttpServletRequest httpServletRequest);

    @DELETE
    @Path("/public/customer/delete/{customerId}")
    @Produces(MediaType.TEXT_PLAIN)
    Response deleteCustomerById(@PathParam("customerId") int customerId);

    @PATCH
    @Path("/api/card/update/dailyLimit/{cardId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    Response updateDailyLimit(@RequestBody UpdateRequest updateRequest, @Context HttpServletRequest httpServletRequest);

    @PATCH
    @Path("/api/card/update/monthlyLimit/{cardId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    Response updateMonthlyLimit(@RequestBody UpdateRequest updateRequest, @Context HttpServletRequest httpServletRequest);

}


