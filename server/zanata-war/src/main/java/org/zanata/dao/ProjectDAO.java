package org.zanata.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.EntityStatus;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;

@Name("projectDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class ProjectDAO extends AbstractDAOImpl<HProject, Long>
{
   public ProjectDAO()
   {
      super(HProject.class);
   }

   public ProjectDAO(Session session)
   {
      super(HProject.class, session);
   }

   public HProject getBySlug(String slug)
   {
      Criteria crit = getSession().createCriteria(HProject.class);
      crit.add(Restrictions.naturalId().set("slug", slug));
      crit.setCacheable(true).setComment("ProjectDAO.getBySlug");
      return (HProject) crit.uniqueResult();
   }

   @SuppressWarnings("unchecked")
   public List<HPerson> getProjectMaintainerBySlug(String slug)
   {
      Query q = getSession().createQuery("select p.maintainers from HProject as p where p.slug = :slug");
      q.setParameter("slug", slug);
      q.setCacheable(true).setComment("ProjectDAO.getProjectMaintainerBySlug");
      return q.list();
   }

   @SuppressWarnings("unchecked")
   public List<HProject> getOffsetListByCreateDate(int offset, int count, boolean filterActive, boolean filterReadOnly, boolean filterObsolete)
   {
      String condition = constructFilterCondition(filterActive, filterReadOnly, filterObsolete);
      Query q = getSession().createQuery("from HProject p " + condition + "order by p.creationDate desc");
      q.setMaxResults(count).setFirstResult(offset);
      q.setCacheable(true).setComment("ProjectDAO.getOffsetListByCreateDate");
      return q.list();
   }

   public int getFilterProjectSize(boolean filterActive, boolean filterReadOnly, boolean filterObsolete)
   {
      String query = "select count(*) from HProject p " + constructFilterCondition(filterActive, filterReadOnly, filterObsolete);
      Query q = getSession().createQuery(query.toString());
      q.setCacheable(true).setComment("ProjectDAO.getFilterProjectSize");
      Long totalCount = (Long) q.uniqueResult();

      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   private String constructFilterCondition(boolean filterActive, boolean filterReadOnly, boolean filterObsolete)
   {
      StringBuilder condition = new StringBuilder();
      if (filterActive || filterReadOnly || filterObsolete)
      {
         condition.append("where ");
      }

      if (filterActive)
      {
         // TODO bind this as a parameter
         condition.append("p.status <> '" + EntityStatus.ACTIVE.getInitial() + "' ");
      }

      if (filterReadOnly)
      {
         if (filterActive)
         {
            condition.append("and ");
         }

         // TODO bind this as a parameter
         condition.append("p.status <> '" + EntityStatus.READONLY.getInitial() + "' ");
      }

      if (filterObsolete)
      {
         if (filterActive || filterReadOnly)
         {
            condition.append("and ");
         }

         // TODO bind this as a parameter
         condition.append("p.status <> '" + EntityStatus.OBSOLETE.getInitial() + "' ");
      }
      return condition.toString();
   }

   @SuppressWarnings("unchecked")
   public List<HProjectIteration> getActiveIterations(String slug)
   {
      Query q = getSession().createQuery("from HProjectIteration t where t.project.slug = :projectSlug and t.status = :status");
      q.setParameter("projectSlug", slug).setParameter("status", EntityStatus.ACTIVE);
      q.setCacheable(true).setComment("ProjectDAO.getActiveIterations");
      return q.list();
   }

   @SuppressWarnings("unchecked")
   public List<HProjectIteration> getReadOnlyIterations(String slug)
   {
      Query q = getSession().createQuery("from HProjectIteration t where t.project.slug = :projectSlug and t.status = :status");
      q.setParameter("projectSlug", slug).setParameter("status", EntityStatus.READONLY);
      q.setCacheable(true).setComment("ProjectDAO.getReadOnlyIterations");
      return q.list();
   }

   @SuppressWarnings("unchecked")
   public List<HProjectIteration> getObsoleteIterations(String slug)
   {
      Query q = getSession().createQuery("from HProjectIteration t where t.project.slug = :projectSlug and t.status = :status");
      q.setParameter("projectSlug", slug).setParameter("status", EntityStatus.OBSOLETE);
      q.setCacheable(true).setComment("ProjectDAO.getObsoleteIterations");
      return q.list();
   }

   public int getTotalProjectCount()
   {
      String query = "select count(*) from HProject";
      Query q = getSession().createQuery(query.toString());
      q.setCacheable(true).setComment("ProjectDAO.getTotalProjectCount");
      Long totalCount = (Long) q.uniqueResult();

      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalActiveProjectCount()
   {
      Query q = getSession().createQuery("select count(*) from HProject p where p.status = :status");
      q.setParameter("status", EntityStatus.ACTIVE);
      q.setCacheable(true).setComment("ProjectDAO.getTotalActiveProjectCount");
      Long totalCount = (Long) q.uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalReadOnlyProjectCount()
   {
      Query q = getSession().createQuery("select count(*) from HProject p where p.status = :status");
      q.setParameter("status", EntityStatus.READONLY);
      q.setCacheable(true).setComment("ProjectDAO.getTotalReadOnlyProjectCount");
      Long totalCount = (Long) q.uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalObsoleteProjectCount()
   {
      Query q = getSession().createQuery("select count(*) from HProject p where p.status = :status");
      q.setParameter("status", EntityStatus.OBSOLETE);
      q.setCacheable(true).setComment("ProjectDAO.getTotalObsoleteProjectCount");
      Long totalCount = (Long) q.uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }
}
