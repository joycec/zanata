package org.zanata.action;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.EntityStatus;
import org.zanata.model.HProject;

@Name("projectSearch")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ProjectSearch
{

   private int pageSize = 30;

   private String searchQuery;

   private List<HProject> searchResults;

   private int currentPage = 1;

   private int resultSize;
   
   private boolean includeObsolete;

   @In
   EntityManager entityManager;

   public String getSearchQuery()
   {
      return searchQuery;
   }

   public void setSearchQuery(String searchQuery)
   {
      this.searchQuery = searchQuery;
   }

   public List<HProject> getSearchResults()
   {
      return searchResults;
   }

   public void setSearchResults(List<HProject> projects)
   {
      this.searchResults = projects;
   }

   public int getResultSize()
   {
      return resultSize;
   }

   public int getCurrentPage()
   {
      return currentPage;
   }

   public void setCurrentPage(int page)
   {
      if (page < 1)
         this.currentPage = 1;
      else
         this.currentPage = page;
   }

   @SuppressWarnings("unchecked")
   @Begin(join=true)
   public void search()
   {
      FullTextQuery query;
      try
      {
         query = searchQuery(searchQuery);
      }
      catch (ParseException pe)
      {
         return;
      }
      
      searchResults = query.setMaxResults(pageSize + 1).setFirstResult(pageSize * (currentPage - 1)).getResultList();
      
      // Manually filtering collection as status field is not indexed by hibernate search
      if( !this.includeObsolete )
      {
         CollectionUtils.filter(searchResults, new Predicate()
         {
            @Override
            public boolean evaluate(Object arg0)
            {
               return ((HProject)arg0).getStatus() != EntityStatus.OBSOLETE;
            }
         });
      }
      resultSize = searchResults.size();
   }

   @SuppressWarnings("deprecation")
   private FullTextQuery searchQuery(String searchQuery) throws ParseException
   {
      String[] projectFields = { "slug", "name", "description" };
      QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_29, projectFields, new StandardAnalyzer(Version.LUCENE_24));
      parser.setAllowLeadingWildcard(true);
      Query luceneQuery = parser.parse(QueryParser.escape(searchQuery));
      return ((FullTextEntityManager) entityManager).createFullTextQuery(luceneQuery, HProject.class);
   }

   public int getPageSize()
   {
      return pageSize;
   }

   public void setPageSize(int pageSize)
   {
      this.pageSize = pageSize;
   }

   public boolean isIncludeObsolete()
   {
      return includeObsolete;
   }

   public void setIncludeObsolete(boolean includeObsolete)
   {
      this.includeObsolete = includeObsolete;
   }

}
