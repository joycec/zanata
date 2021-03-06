package org.zanata.rest.service;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.codehaus.enunciate.jaxrs.TypeHint;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.security.Identity;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.model.HTermComment;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Glossary;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryTerm;
import org.zanata.security.SecurityChecker;
import org.zanata.service.LocaleService;

@Name("glossaryService")
@Path(GlossaryService.SERVICE_PATH)
@Transactional
public class GlossaryService implements GlossaryResource, SecurityChecker
{
   @Context
   private UriInfo uri;

   @HeaderParam("Content-Type")
   @Context
   private MediaType requestContentType;

   @Context
   private HttpHeaders headers;

   @Context
   private Request request;

   @In
   private GlossaryDAO glossaryDAO;

   @In
   private Identity identity;

   @In
   private LocaleService localeServiceImpl;

   private final static String GLOSSARY_ACTION_INSERT = "glossary-insert";
   private final static String GLOSSARY_ACTION_DELETE = "glossary-delete";
   private final static String GLOSSARY_ACTION_UPDATE = "glossary-update";

   public GlossaryService()
   {
   }

   public GlossaryService(GlossaryDAO glossaryDAO, Identity identity, LocaleService localeService)
   {
      this.glossaryDAO = glossaryDAO;
      this.identity = identity;
      this.localeServiceImpl = localeService;
   }

   /**
    * Returns all Glossary entries.
    * 
    * @return The following response status codes will be returned from this operation:<br>
    * OK(200) - Response containing all Glossary entries in the system.
    * INTERNAL SERVER ERROR(500) - If there is an unexpected error in the server while performing this operation.
    */
   @Override
   @GET
   @Produces( { MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML, MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   @TypeHint(Glossary.class)
   public Response getEntries()
   {
      ResponseBuilder response = request.evaluatePreconditions();
      if (response != null)
      {
         return response.build();
      }

      List<HGlossaryEntry> hGlosssaryEntries = glossaryDAO.getEntries();

      Glossary glossary = new Glossary();
      transferEntriesResource(hGlosssaryEntries, glossary);

      return Response.ok(glossary).build();
   }

   /**
    * Returns Glossary entries for a single locale.
    * 
    * @param locale Locale for which to retrieve entries.
    * @return The following response status codes will be returned from this operation:<br>
    * OK(200) - Response containing all Glossary entries for the given locale.
    * INTERNAL SERVER ERROR(500) - If there is an unexpected error in the server while performing this operation.
    */
   @Override
   @GET
   @Path("/{locale}")
   @Produces( { MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML, MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   @TypeHint(Glossary.class)
   public Response get(@PathParam("locale") LocaleId locale)
   {
      ResponseBuilder response = request.evaluatePreconditions();
      if (response != null)
      {
         return response.build();
      }

      List<HGlossaryEntry> hGlosssaryEntries = glossaryDAO.getEntriesByLocaleId(locale);
      Glossary glossary = new Glossary();

      transferEntriesLocaleResource(hGlosssaryEntries, glossary, locale);

      return Response.ok(glossary).build();
   }

   /**
    * Adds glossary entries.
    * 
    * @param glossary The Glossary entries to add.
    * @return The following response status codes will be returned from this operation:<br>
    * CREATED(201) - If the glossary entries were successfully created.
    * UNAUTHORIZED(401) - If the user does not have the proper permissions to perform this operation.<br>
    * INTERNAL SERVER ERROR(500) - If there is an unexpected error in the server while performing this operation.
    */
   @Override
   @PUT
   @Consumes( { MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML, MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   @Restrict("#{glossaryService.checkPermission('glossary-insert')}")
   public Response put(Glossary glossary)
   {
      ResponseBuilder response;

      // must be a create operation
      response = request.evaluatePreconditions();
      if (response != null)
      {
         return response.build();
      }
      response = Response.created(uri.getAbsolutePath());

      for (GlossaryEntry glossaryEntry : glossary.getGlossaryEntries())
      {
         transferGlossaryEntry(glossaryEntry);
      }
      glossaryDAO.flush();

      return response.build();
   }

   /**
    * Delete all glossary terms with the specified locale.
    * 
    * @param targetLocale The target locale to delete glossary entries from.
    * @return The following response status codes will be returned from this operation:<br>
    * OK(200) - If the glossary entries were successfully deleted.
    * UNAUTHORIZED(401) - If the user does not have the proper permissions to perform this operation.<br>
    * INTERNAL SERVER ERROR(500) - If there is an unexpected error in the server while performing this operation.
    */
   @Override
   @DELETE
   @Path("/{locale}")
   @Restrict("#{glossaryService.checkPermission('glossary-delete')}")
   public Response deleteGlossary(@PathParam("locale") LocaleId targetLocale)
   {
      ResponseBuilder response = request.evaluatePreconditions();
      if (response != null)
      {
         return response.build();
      }

      List<HGlossaryEntry> hGlossaryEntries = glossaryDAO.getEntries();

      for (HGlossaryEntry hGlossaryEntry : hGlossaryEntries)
      {
         for (HLocale key : hGlossaryEntry.getGlossaryTerms().keySet())
         {
            HGlossaryTerm hGlossaryTerm = hGlossaryEntry.getGlossaryTerms().get(key);
            if (hGlossaryTerm.getLocale().getLocaleId().equals(targetLocale))
            {
               // Locale is unique for each entry
               hGlossaryEntry.getGlossaryTerms().remove(hGlossaryTerm.getLocale());
               break;
            }
         }

         if (hGlossaryEntry.getGlossaryTerms().isEmpty())
         {
            glossaryDAO.makeTransient(hGlossaryEntry);
         }
         else
         {
            glossaryDAO.makePersistent(hGlossaryEntry);
         }

         glossaryDAO.flush();

      }

      return Response.ok().build();
   }

   /**
    * Delete all glossary terms.
    * 
    * @return The following response status codes will be returned from this operation:<br>
    * OK(200) - If the glossary entries were successfully deleted.
    * UNAUTHORIZED(401) - If the user does not have the proper permissions to perform this operation.<br>
    * INTERNAL SERVER ERROR(500) - If there is an unexpected error in the server while performing this operation.
    */
   @Override
   @DELETE
   @Restrict("#{glossaryService.checkPermission('glossary-delete')}")
   public Response deleteGlossaries()
   {
      ResponseBuilder response = request.evaluatePreconditions();
      if (response != null)
      {
         return response.build();
      }
      List<HGlossaryEntry> hGlossaryEntries = glossaryDAO.getEntries();

      for (HGlossaryEntry hGlossaryEntry : hGlossaryEntries)
      {
         glossaryDAO.makeTransient(hGlossaryEntry);
      }
      glossaryDAO.flush();

      return Response.ok().build();
   }

   private HGlossaryTerm getOrCreateGlossaryTerm(HGlossaryEntry hGlossaryEntry, HLocale termHLocale, String content)
   {
      HGlossaryTerm hGlossaryTerm = hGlossaryEntry.getGlossaryTerms().get(termHLocale);

      if (hGlossaryTerm == null)
      {
         hGlossaryTerm = new HGlossaryTerm(content);
         hGlossaryTerm.setLocale(termHLocale);
         hGlossaryTerm.setGlossaryEntry(hGlossaryEntry);
         hGlossaryEntry.getGlossaryTerms().put(termHLocale, hGlossaryTerm);
      }

      return hGlossaryTerm;
   }

   private HGlossaryEntry getOrCreateGlossaryEntry(LocaleId srcLocale, String srcContent)
   {
      HGlossaryEntry hGlossaryEntry = glossaryDAO.getEntryBySrcLocaleAndContent(srcLocale, srcContent);

      if (hGlossaryEntry == null)
      {
         hGlossaryEntry = new HGlossaryEntry();
         HLocale srcHLocale = localeServiceImpl.getByLocaleId(srcLocale);
         hGlossaryEntry.setSrcLocale(srcHLocale);
      }
      return hGlossaryEntry;
   }

   public String getSrcGlossaryTerm(GlossaryEntry entry)
   {
      for (GlossaryTerm term : entry.getGlossaryTerms())
      {
         if (term.getLocale().equals(entry.getSrcLang()))
         {
            return term.getContent();
         }
      }
      return null;
   }

   public void transferGlossaryEntry(GlossaryEntry from)
   {
      HGlossaryEntry to = getOrCreateGlossaryEntry(from.getSrcLang(), getSrcGlossaryTerm(from));
            
      to.setSourceRef(from.getSourcereference());

      for (GlossaryTerm glossaryTerm : from.getGlossaryTerms())
      {
         HLocale termHLocale = localeServiceImpl.validateSourceLocale(glossaryTerm.getLocale());

         // check if there's existing term with same content, overrides comments
         HGlossaryTerm hGlossaryTerm = getOrCreateGlossaryTerm(to, termHLocale, glossaryTerm.getContent());

         hGlossaryTerm.getComments().clear();

         for (String comment : glossaryTerm.getComments())
         {
            hGlossaryTerm.getComments().add(new HTermComment(comment));
         }
      }
      glossaryDAO.makePersistent(to);
   }

   public void transferEntriesResource(List<HGlossaryEntry> hGlosssaryEntries, Glossary glossary)
   {
      for (HGlossaryEntry hGlossaryEntry : hGlosssaryEntries)
      {
         GlossaryEntry glossaryEntry = new GlossaryEntry();
         glossaryEntry.setSourcereference(hGlossaryEntry.getSourceRef());
         glossaryEntry.setSrcLang(hGlossaryEntry.getSrcLocale().getLocaleId());

         for (HGlossaryTerm hGlossaryTerm : hGlossaryEntry.getGlossaryTerms().values())
         {
            GlossaryTerm glossaryTerm = new GlossaryTerm();
            glossaryTerm.setContent(hGlossaryTerm.getContent());
            glossaryTerm.setLocale(hGlossaryTerm.getLocale().getLocaleId());

            for (HTermComment hTermComment : hGlossaryTerm.getComments())
            {
               glossaryTerm.getComments().add(hTermComment.getComment());
            }
            glossaryEntry.getGlossaryTerms().add(glossaryTerm);
         }
         glossary.getGlossaryEntries().add(glossaryEntry);
      }
   }

   public static void transferEntriesLocaleResource(List<HGlossaryEntry> hGlosssaryEntries, Glossary glossary, LocaleId locale)
   {
      for (HGlossaryEntry hGlossaryEntry : hGlosssaryEntries)
      {
         GlossaryEntry glossaryEntry = new GlossaryEntry();
         glossaryEntry.setSrcLang(hGlossaryEntry.getSrcLocale().getLocaleId());
         glossaryEntry.setSourcereference(hGlossaryEntry.getSourceRef());
         for (HGlossaryTerm hGlossaryTerm : hGlossaryEntry.getGlossaryTerms().values())
         {
            if (hGlossaryTerm.getLocale().getLocaleId().equals(locale))
            {
               GlossaryTerm glossaryTerm = new GlossaryTerm();
               glossaryTerm.setContent(hGlossaryTerm.getContent());
               glossaryTerm.setLocale(hGlossaryTerm.getLocale().getLocaleId());

               for (HTermComment hTermComment : hGlossaryTerm.getComments())
               {
                  glossaryTerm.getComments().add(hTermComment.getComment());
               }
               glossaryEntry.getGlossaryTerms().add(glossaryTerm);
            }
         }
         glossary.getGlossaryEntries().add(glossaryEntry);
      }
   }
   
   @Override
   public boolean checkPermission(String operation)
   {
      return identity != null && identity.hasPermission("", operation);
   }
}
