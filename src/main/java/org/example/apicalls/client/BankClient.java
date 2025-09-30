package org.example.apicalls.client;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;
import org.example.apicalls.apiconfig.BankAPI;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BankClient {

    @Bean
    public BankAPI getAPI() {

        ResteasyClient client = (ResteasyClient)ClientBuilder.newClient();
        ResteasyWebTarget target = client.target("http://localhost:8080/");
        return target.proxy(BankAPI.class);
    }

    @Bean
    public BankAPI getAPI(NewCookie newCookie){
        Cookie cookie = convertNewCookieToCookie(newCookie);
        ResteasyClient clientNew = (ResteasyClient)ClientBuilder.newClient();
        ResteasyClient client = (ResteasyClient) clientNew.register(new AddCookieClientFilter(cookie));
        ResteasyWebTarget target = client.target("http://localhost:8080/");
        return target.proxy(BankAPI.class);
    }

    private Cookie convertNewCookieToCookie(NewCookie newCookie) {
        return new Cookie(
                newCookie.getName(),      // Nombre de la cookie
                newCookie.getValue(),     // Valor de la cookie
                newCookie.getPath(),      // Ruta
                newCookie.getDomain(),    // Dominio
                newCookie.getVersion()
        );
    }

    /**
     * Calls an API endpoint and returns a typed Response object.
     *
     * @param apiSupplier Supplier that executes the API call
     * @param clazz Optional class to handle entity type
     * @param genericType GenericType for complex types like collections
     * @param <T> The expected type of the response payload
     * @return A Response object wrapping the result of the API call
     */
/*
    public <T> Response call(
            Supplier<jakarta.ws.rs.core.Response> apiSupplier,
            Optional<Class> clazz,
            GenericType<T> genericType
    ) {
        int status = 0;
        try{
            jakarta.ws.rs.core.Response response = apiSupplier.get();
            status = response.getStatus();
            return wrapResponse(response, clazz, genericType);
        }
        catch (Exception e){
            return handleErrorResponse(status,e);
        }
    }
/*
    /**
     * Wraps the API response into a Response object depending on the type.
     *
     * @param response The raw API response
     * @param clazz Optional class type for the payload
     * @param genericType GenericType for handling complex payloads
     * @param <T> The expected type of the response payload
     * @return A Response object wrapping the API response
     */
/*
    private <T> Response wrapResponse(
            jakarta.ws.rs.core.Response response, Optional<Class> clazz, GenericType<T> genericType) {
        if (clazz.isPresent()) {
            // If class type is provided, use it to read the entity
            return Response.build(response, clazz);
        } else {
            // If generic type is provided (e.g., for complex types like lists), use it
            return Response.build(response, genericType);
        }
    }
*/
    /**
     * Handles the creation of a Response in case of an error during the API call.
     *
     * @param status The HTTP status code (if available)
     * @param e The exception that occurred during the API call
     * @param <T> The expected type of the response payload
     * @return A Response object representing the error
     */
/*
    private <T> ResponsehandleErrorResponse(int status, Exception e) {
        // Log the error (logging mechanism not implemented here, but can be added)
        e.printStackTrace(); // Log the stack trace for debugging

        // Return a response with the status code and error message as payload
        return new Response (status, null, (T) ("Error: " + e.getMessage()));
    } */
}
