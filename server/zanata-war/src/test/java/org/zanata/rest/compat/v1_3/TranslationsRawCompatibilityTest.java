/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.rest.compat.v1_3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.testng.annotations.Test;
import org.zanata.ZanataCompatibilityTest;
import org.zanata.v1_3.common.ContentState;
import org.zanata.v1_3.common.ContentType;
import org.zanata.v1_3.common.LocaleId;
import org.zanata.v1_3.common.ResourceType;
import org.zanata.v1_3.rest.StringSet;
import org.zanata.v1_3.rest.client.ITranslationResources;
import org.zanata.v1_3.rest.dto.extensions.comment.SimpleComment;
import org.zanata.v1_3.rest.dto.extensions.gettext.PoHeader;
import org.zanata.v1_3.rest.dto.resource.Resource;
import org.zanata.v1_3.rest.dto.resource.ResourceMeta;
import org.zanata.v1_3.rest.dto.resource.TextFlow;
import org.zanata.v1_3.rest.dto.resource.TextFlowTarget;
import org.zanata.v1_3.rest.dto.resource.TranslationsResource;

public class TranslationsRawCompatibilityTest extends ZanataCompatibilityTest
{

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/AccountData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/DocumentsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/TextFlowTestData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      
      afterTestOperations.add(new DataSetOperation("org/zanata/test/model/HistoryTestData.dbunit.xml", DatabaseOperation.DELETE_ALL));
   }
   
   @Test
   public void getJsonResource() throws Exception
   {      
      new ResourceRequest(sharedEnvironment, Method.GET, "/restv1/projects/p/sample-project/iterations/i/1.0/r/my,path,document-2.txt")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
         }
         
         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertJsonUnmarshal(response, Resource.class);
            
            Resource resource = jsonUnmarshal(response, Resource.class);
            assertThat(resource.getName(), is("my/path/document-2.txt"));
            assertThat(resource.getType(), is(ResourceType.FILE));
            assertThat(resource.getLang(), is(LocaleId.EN_US));
            
            // Make sure all Text Flows are present
            assertThat(resource.getTextFlows().size(), greaterThanOrEqualTo(3));
            
            // Evaluate individual text flows
            TextFlow txtFlow = resource.getTextFlows().get(0);
            assertThat(txtFlow.getId(), is("tf2"));
            assertThat(txtFlow.getRevision(), is(1));
            assertThat(txtFlow.getContent(), is("mssgId1"));
            
            txtFlow = resource.getTextFlows().get(1);
            assertThat(txtFlow.getId(), is("tf3"));
            assertThat(txtFlow.getRevision(), is(1));
            assertThat(txtFlow.getContent(), is("mssgId2"));
            
            txtFlow = resource.getTextFlows().get(2);
            assertThat(txtFlow.getId(), is("tf4"));
            assertThat(txtFlow.getRevision(), is(1));
            assertThat(txtFlow.getContent(), is("mssgId3"));
         } 
      }.run();
   }
   
   @Test
   public void postJsonResource() throws Exception
   {
      // Create a new Resource
      final Resource res = new Resource("new-resource");
      res.setType(ResourceType.FILE);
      res.setContentType(ContentType.TextPlain);
      res.setLang(LocaleId.EN_US);
      res.setRevision(1);
      res.getExtensions(true).add( new PoHeader("This is a PO Header") );
      
      TextFlow tf1 = new TextFlow("tf1", LocaleId.EN_US, "First Text Flow");
      tf1.getExtensions(true).add( new SimpleComment("This is one comment") );
      res.getTextFlows().add(tf1);
      
      new ResourceRequest(sharedEnvironment, Method.POST, "/restv1/projects/p/sample-project/iterations/i/1.0/r")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.setContentType( MediaType.APPLICATION_JSON );
            // Note: The setQueryParameter method doesnt allow multiple values. This is why manually setting the query string is required.
            request.setQueryString("ext=" + PoHeader.ID + "&ext=" + SimpleComment.ID);  
            request.setContent( jsonMarshal(res).getBytes() );
         }
         
         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat( response.getStatus(), is(Status.CREATED.getStatusCode()) ); // 201
         }         
      }.run();
      
      // Verify that it was created successfully
      ITranslationResources translationsClient = super.createProxy(ITranslationResources.class, "/projects/p/sample-project/iterations/i/1.0/r");
      ClientResponse<Resource> resourceResponse = translationsClient.getResource(res.getName(), new StringSet(PoHeader.ID + ";" + SimpleComment.ID));
      
      assertThat(resourceResponse.getStatus(), is(Status.OK.getStatusCode())); // 200
      
      Resource createdResource = resourceResponse.getEntity();
      
      assertThat(createdResource.getName(), is(res.getName()));
      assertThat(createdResource.getType(), is(res.getType()));
      assertThat(createdResource.getContentType(), is(res.getContentType()));
      assertThat(createdResource.getLang(), is(res.getLang()));
      assertThat(createdResource.getRevision(), is(1)); // Created, so revision 1
      
      // Extensions
      assertThat(createdResource.getExtensions(true).size(), greaterThanOrEqualTo(1));
      assertThat(createdResource.getExtensions(true).findByType(PoHeader.class).getComment(), is("This is a PO Header") );
      
      // Text Flow
      assertThat(createdResource.getTextFlows().size(), is(1));
      
      TextFlow createdTf = createdResource.getTextFlows().get(0);
      assertThat(createdTf.getContent(), is(tf1.getContent()));
      assertThat(createdTf.getId(), is(tf1.getId()));
      assertThat(createdTf.getLang(), is(tf1.getLang()));
      assertThat(createdTf.getRevision(), is(1)); // Create, so revision 1
      
      // Text Flow extensions
      assertThat(createdTf.getExtensions(true).size(), is(1));
      assertThat(createdTf.getExtensions(true).findOrAddByType(SimpleComment.class).getValue(), is("This is one comment"));
   }
   
   @Test
   public void putJsonResource() throws Exception
   {
      // Create a new Resource
      final Resource res = new Resource("new-put-resource");
      res.setType(ResourceType.FILE);
      res.setContentType(ContentType.TextPlain);
      res.setLang(LocaleId.EN_US);
      res.setRevision(1);
      res.getExtensions(true).add( new PoHeader("This is a PO Header") );
      
      TextFlow tf1 = new TextFlow("tf1", LocaleId.EN_US, "First Text Flow");
      tf1.getExtensions(true).add( new SimpleComment("This is one comment") );
      res.getTextFlows().add(tf1);
      
      new ResourceRequest(sharedEnvironment, Method.PUT, "/restv1/projects/p/sample-project/iterations/i/1.0/r/" + res.getName())
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.setContentType( MediaType.APPLICATION_JSON );
            // Note: The setQueryParameter method doesnt allow multiple values. This is why manually setting the query string is required.
            request.setQueryString("ext=" + SimpleComment.ID + "&ext=" + PoHeader.ID);  
            request.setContent( jsonMarshal(res).getBytes() );
         }
         
         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat( response.getStatus(), is(Status.CREATED.getStatusCode()) ); // 201
         }         
      }.run();
      
      ITranslationResources translationsClient = super.createProxy(ITranslationResources.class, "/projects/p/sample-project/iterations/i/1.0/r/");      
      // Verify that it was created successfully
      ClientResponse<Resource> resourceResponse = translationsClient.getResource(res.getName(), new StringSet(PoHeader.ID + ";" + SimpleComment.ID));
      Resource createdResource = resourceResponse.getEntity();
      
      assertThat(createdResource.getName(), is(res.getName()));
      assertThat(createdResource.getType(), is(res.getType()));
      assertThat(createdResource.getContentType(), is(res.getContentType()));
      assertThat(createdResource.getLang(), is(res.getLang()));
      assertThat(createdResource.getRevision(), is(1)); // Created, so revision 1
      
      // Extensions
      assertThat(createdResource.getExtensions(true).size(), greaterThanOrEqualTo(1));
      assertThat(createdResource.getExtensions(true).findByType(PoHeader.class).getComment(), is("This is a PO Header") );
      
      // Text Flow
      assertThat(createdResource.getTextFlows().size(), is(1));
      
      TextFlow createdTf = createdResource.getTextFlows().get(0);
      assertThat(createdTf.getContent(), is(tf1.getContent()));
      assertThat(createdTf.getId(), is(tf1.getId()));
      assertThat(createdTf.getLang(), is(tf1.getLang()));
      assertThat(createdTf.getRevision(), is(1)); // Create, so revision 1
      
      // Text Flow extensions
      assertThat(createdTf.getExtensions(true).size(), is(1));
      assertThat(createdTf.getExtensions(true).findOrAddByType(SimpleComment.class).getValue(), is("This is one comment"));
   }
   
   @Test
   public void getJsonResourceMeta() throws Exception
   {
      new ResourceRequest(unauthorizedEnvironment, Method.GET, "/restv1/projects/p/sample-project/iterations/i/1.0/r/my,path,document-2.txt")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            // Note: The setQueryParameter method doesnt allow multiple values. This is why manually setting the query string is required.
            request.setQueryString("ext=" + SimpleComment.ID);
         }
         
         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat( response.getStatus(), is(Status.OK.getStatusCode()) ); // 200
            assertJsonUnmarshal(response, ResourceMeta.class);
            
            ResourceMeta resMeta = jsonUnmarshal(response, ResourceMeta.class);
            assertThat(resMeta.getName(), is("my/path/document-2.txt"));
            assertThat(resMeta.getType(), is(ResourceType.FILE));
            assertThat(resMeta.getLang(), is(LocaleId.EN_US));
            assertThat(resMeta.getContentType(), is(ContentType.TextPlain));
         }         
      }.run();
   }
   
   @Test
   public void putJsonResourceMeta() throws Exception
   {
      ITranslationResources translationsClient = super.createProxy(ITranslationResources.class, "/projects/p/sample-project/iterations/i/1.0/r/");
      final ResourceMeta resMeta = new ResourceMeta();
      resMeta.setName("my/path/document-2.txt");
      resMeta.setType(ResourceType.FILE);
      resMeta.setContentType(ContentType.TextPlain);
      resMeta.setLang(LocaleId.EN_US);
      resMeta.setRevision(1);
      
      new ResourceRequest(unauthorizedEnvironment, Method.PUT, "/restv1/projects/p/sample-project/iterations/i/1.0/r/my,path,document-2.txt")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.setContentType(MediaType.APPLICATION_JSON);
            // Note: The setQueryParameter method doesnt allow multiple values. This is why manually setting the query string is required.
            request.setQueryString("ext=" + SimpleComment.ID);
            request.setContent( jsonMarshal(resMeta).getBytes() );
         }
         
         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat( response.getStatus(), is(Status.OK.getStatusCode()) ); // 200
         }         
      }.run();
      
      // Fetch again
      ClientResponse<ResourceMeta> getResponse = translationsClient.getResourceMeta("my,path,document-2.txt", null);
      ResourceMeta newResMeta = getResponse.getEntity();
      
      assertThat( getResponse.getStatus(), is(Status.OK.getStatusCode()) ); // 200
      assertThat( newResMeta.getName(), is(resMeta.getName()) );
      assertThat( newResMeta.getContentType(), is(resMeta.getContentType()) );
      assertThat( newResMeta.getLang(), is(resMeta.getLang()) );
      assertThat( newResMeta.getType(), is(resMeta.getType()) );
      assertThat( newResMeta.getRevision(), greaterThan(1) ); // Updated so higher revision
   }
   
   @Test
   public void getJsonTranslations() throws Exception
   {
      new ResourceRequest(unauthorizedEnvironment, Method.GET, "/restv1/projects/p/sample-project/iterations/i/1.0/r/my,path,document-2.txt/translations/" + LocaleId.EN_US)
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            // Note: The setQueryParameter method doesnt allow multiple values. This is why manually setting the query string is required.
            request.setQueryString("ext=" + SimpleComment.ID);
         }
         
         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat( response.getStatus(), is(Status.OK.getStatusCode()) ); // 200
            assertJsonUnmarshal(response, TranslationsResource.class);
            
            TranslationsResource transRes = jsonUnmarshal(response, TranslationsResource.class);
            assertThat(transRes.getTextFlowTargets().size(), greaterThanOrEqualTo(3));
            
            // First Text Flow Target
            TextFlowTarget tft1 = transRes.getTextFlowTargets().get(0);
            assertThat(tft1.getResId(), is("tf2"));
            assertThat(tft1.getState(), is(ContentState.NeedReview));
            assertThat(tft1.getContent(), is("mssgTrans1"));
            assertThat(tft1.getExtensions(true).findByType(SimpleComment.class).getValue(), is("Text Flow Target Comment 1"));
            assertThat(tft1.getTranslator().getName(), is("Sample User"));
            assertThat(tft1.getTranslator().getEmail(), is("user1@localhost"));
            
            // Second Text Flow Target
            TextFlowTarget tft2 = transRes.getTextFlowTargets().get(1);
            assertThat(tft2.getResId(), is("tf3"));
            assertThat(tft2.getState(), is(ContentState.NeedReview));
            assertThat(tft2.getContent(), is("mssgTrans2"));
            assertThat(tft2.getExtensions(true).findByType(SimpleComment.class).getValue(), is("Text Flow Target Comment 2"));
            assertThat(tft2.getTranslator().getName(), is("Sample User"));
            assertThat(tft2.getTranslator().getEmail(), is("user1@localhost"));
            
            // First Text Flow Target
            TextFlowTarget tft3 = transRes.getTextFlowTargets().get(2);
            assertThat(tft3.getResId(), is("tf4"));
            assertThat(tft3.getState(), is(ContentState.NeedReview));
            assertThat(tft3.getContent(), is("mssgTrans3"));
            assertThat(tft3.getExtensions(true).findByType(SimpleComment.class).getValue(), is("Text Flow Target Comment 3"));
            assertThat(tft3.getTranslator().getName(), is("Sample User"));
            assertThat(tft3.getTranslator().getEmail(), is("user1@localhost"));
         }         
      }.run();
   }
   
   @Test
   public void putJsonTranslations() throws Exception
   {
      // Get the original translations
      ITranslationResources translationsClient = super.createProxy(ITranslationResources.class, "/projects/p/sample-project/iterations/i/1.0/r/");
      ClientResponse<TranslationsResource> response = translationsClient.getTranslations("my,path,document-2.txt", LocaleId.EN_US, new StringSet(SimpleComment.ID));
      final TranslationsResource transRes = response.getEntity();
      
      assertThat(response.getStatus(), is(Status.OK.getStatusCode())); // 200
      assertThat(transRes.getTextFlowTargets().size(), greaterThanOrEqualTo(3));
      
      // Alter the translations
      transRes.getTextFlowTargets().get(0).setContent("Translated 1");
      transRes.getTextFlowTargets().get(1).setContent("Translated 2");
      transRes.getTextFlowTargets().get(2).setContent("Translated 3");
      
      transRes.getTextFlowTargets().get(0).setState(ContentState.Approved);
      transRes.getTextFlowTargets().get(1).setState(ContentState.Approved);
      transRes.getTextFlowTargets().get(2).setState(ContentState.Approved);
      
      transRes.getTextFlowTargets().get(0).getExtensions(true).add( new SimpleComment("Translated Comment 1") );
      transRes.getTextFlowTargets().get(1).getExtensions(true).add( new SimpleComment("Translated Comment 2") );
      transRes.getTextFlowTargets().get(2).getExtensions(true).add( new SimpleComment("Translated Comment 3") );
      
      // Put the translations
      new ResourceRequest(unauthorizedEnvironment, Method.PUT, "/restv1/projects/p/sample-project/iterations/i/1.0/r/my,path,document-2.txt/translations/" + LocaleId.EN_US)
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.setContentType(MediaType.APPLICATION_JSON);
            // Note: The setQueryParameter method doesnt allow multiple values. This is why manually setting the query string is required.
            request.setQueryString("ext=" + SimpleComment.ID);
            request.setContent( jsonMarshal(transRes).getBytes() );
         }
         
         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat( response.getStatus(), is(Status.OK.getStatusCode()) ); // 200
         }         
      }.run();
      
      // Retrieve the translations once more to make sure they were changed accordingly
      response = translationsClient.getTranslations("my,path,document-2.txt", LocaleId.EN_US, new StringSet(SimpleComment.ID));
      TranslationsResource updatedTransRes = response.getEntity();
      
      assertThat(response.getStatus(), is(Status.OK.getStatusCode())); // 200
      assertThat(updatedTransRes.getTextFlowTargets().size(), greaterThanOrEqualTo(3));
      
      // First Text Flow Target
      TextFlowTarget tft1 = updatedTransRes.getTextFlowTargets().get(0);
      assertThat(tft1.getResId(), is("tf2"));
      assertThat(tft1.getState(), is(ContentState.Approved));
      assertThat(tft1.getContent(), is("Translated 1"));
      assertThat(tft1.getExtensions(true).findByType(SimpleComment.class).getValue(), is("Translated Comment 1"));
      assertThat(tft1.getTranslator().getName(), is("Sample User"));
      assertThat(tft1.getTranslator().getEmail(), is("user1@localhost"));
      
      // Second Text Flow Target
      TextFlowTarget tft2 = updatedTransRes.getTextFlowTargets().get(1);
      assertThat(tft2.getResId(), is("tf3"));
      assertThat(tft2.getState(), is(ContentState.Approved));
      assertThat(tft2.getContent(), is("Translated 2"));
      assertThat(tft2.getExtensions(true).findByType(SimpleComment.class).getValue(), is("Translated Comment 2"));
      assertThat(tft2.getTranslator().getName(), is("Sample User"));
      assertThat(tft2.getTranslator().getEmail(), is("user1@localhost"));
      
      // First Text Flow Target
      TextFlowTarget tft3 = updatedTransRes.getTextFlowTargets().get(2);
      assertThat(tft3.getResId(), is("tf4"));
      assertThat(tft3.getState(), is(ContentState.Approved));
      assertThat(tft3.getContent(), is("Translated 3"));
      assertThat(tft3.getExtensions(true).findByType(SimpleComment.class).getValue(), is("Translated Comment 3"));
      assertThat(tft3.getTranslator().getName(), is("Sample User"));
      assertThat(tft3.getTranslator().getEmail(), is("user1@localhost"));
   }

}
