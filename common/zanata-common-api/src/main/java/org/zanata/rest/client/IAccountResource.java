package org.zanata.rest.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;

import org.jboss.resteasy.client.ClientResponse;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Account;
import org.zanata.rest.service.AccountResource;

//@Path("/accounts/u/{username}")
public interface IAccountResource extends AccountResource
{

   @GET
   @Produces(
   {MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML, MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON})
   public ClientResponse<Account> get();

   @PUT
   @Consumes(
   {MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML, MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON})
   public ClientResponse put(Account account);

}
