package org.zanata.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;

@Name("textFlowTargetDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class TextFlowTargetDAO extends AbstractDAOImpl<HTextFlowTarget, Long>
{

   public TextFlowTargetDAO()
   {
      super(HTextFlowTarget.class);
   }

   public TextFlowTargetDAO(Session session)
   {
      super(HTextFlowTarget.class, session);
   }

   /**
    * @param textFlow
    * @param localeId
    * @return
    */
   public HTextFlowTarget getByNaturalId(HTextFlow textFlow, HLocale locale)
   {
      Criteria cr = getSession().createCriteria(HTextFlowTarget.class);
      cr.add(Restrictions.naturalId().set("textFlow", textFlow).set("locale", locale));
      cr.setCacheable(true).setComment("TextFlowTargetDAO.getByNaturalId");
      return (HTextFlowTarget) cr.uniqueResult();
   }

   public int getTotalTextFlowTargets()
   {
      Query q = getSession().createQuery("select count(*) from HTextFlowTarget");
      q.setCacheable(true);
      Long totalCount = (Long) q.uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalActiveTextFlowTargets()
   {
      Query q = getSession().createQuery("select count(*) from HTextFlowTarget t where t.textFlow.obsolete=0");
      q.setCacheable(true);
      Long totalCount = (Long) q.uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalObsoleteTextFlowTargets()
   {
      Query q = getSession().createQuery("select count(*) from HTextFlowTarget t where t.textFlow.obsolete=1");
      q.setCacheable(true);
      Long totalCount = (Long) q.uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalApprovedTextFlowTargets()
   {
      Query q = getSession().createQuery("select count(*) from HTextFlowTarget t where t.state = :state and t.textFlow.obsolete=0");
      q.setCacheable(true);
      Long totalCount = (Long) q.setParameter("state", ContentState.Approved).uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalNeedReviewTextFlowTargets()
   {
      Query q = getSession().createQuery("select count(*) from HTextFlowTarget t where t.state = :state and t.textFlow.obsolete=0");
      q.setCacheable(true);
      Long totalCount = (Long) q.setParameter("state", ContentState.NeedReview).uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalNewTextFlowTargets()
   {
      Query q = getSession().createQuery("select count(*) from HTextFlowTarget t where t.state = :state and t.textFlow.obsolete=0");
      q.setCacheable(true);
      Long totalCount = (Long) q.setParameter("state", ContentState.New).uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalApprovedWords()
   {
      Query q = getSession().createQuery("select sum(t.textFlow.wordCount) from HTextFlowTarget t where t.state = :state and t.textFlow.obsolete=0");
      q.setCacheable(true);
      Long totalCount = (Long) q.setParameter("state", ContentState.Approved).uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalNeedReviewWords()
   {
      Query q = getSession().createQuery("select sum(t.textFlow.wordCount) from HTextFlowTarget t where t.state = :state and t.textFlow.obsolete=0");
      q.setCacheable(true);
      Long totalCount = (Long) q.setParameter("state", ContentState.NeedReview).uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   @SuppressWarnings("unchecked")
   public List<HTextFlowTarget> findAllTranslations(HDocument document, LocaleId localeId)
   {
      // @formatter:off
      Query q = getSession().createQuery(
            "select t from HTextFlowTarget t where " + 
            "t.textFlow.document =:document " +
            "and t.locale.localeId =:localeId " + 
            "and t.state !=:state " + 
            "order by t.textFlow.pos");
      q.setParameter("document", document);
      q.setParameter("localeId", localeId);
      q.setParameter("state", ContentState.New);
      q.setCacheable(true);
      return q.list();
      // @formatter:on
   }

   @SuppressWarnings("unchecked")
   public List<HTextFlowTarget> findTranslations(HDocument document, HLocale locale)
   {
      // @formatter:off
      Query q = getSession().createQuery(
            "select t " +
                  "from HTextFlowTarget t where " +
                  "t.textFlow.document =:document " +
                  "and t.locale =:locale " +
                  "and t.state !=:state " +
                  "and t.textFlow.obsolete=false " +
                  "order by t.textFlow.pos");
      q.setParameter("document", document);
      q.setParameter("locale", locale);
      q.setParameter("state", ContentState.New);
      q.setCacheable(true);
      return q.list();
      // @formatter:on
   }
   
   /**
    * Fetches a set of equivalent translations for a given document on a given locale.
    * 
    * @param document The document for which to find equivalent translations.
    * @param locale The locale. Only translations for this locale are fetched.
    * @return A scrollable result set (in case there is a large result set). Position 0 of the 
    * result set is the matching translation (HTextFlowTarget) and position 1 is the HTextFlow 
    * in the document that it matches against. 
    */
   public ScrollableResults findLatestEquivalentTranslations(HDocument document, HLocale locale)
   {
      // @formatter:off
      Query q = getSession().getNamedQuery("HTextFlowTarget.findLatestEquivalentTranslations");
      q.setParameter("document", document)
      .setParameter("docId", document.getDocId())
      .setParameter("locale", locale)
      .setParameter("state", ContentState.Approved);
      q.setCacheable(false); // TODO does it make sense to cache scrollable results?
      return q.scroll(ScrollMode.FORWARD_ONLY); // Not Scrollable, only allows forward scrolling
      // @formatter:on
   }
}
