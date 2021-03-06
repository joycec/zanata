/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.hibernate.search;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

public class IdFilter extends Filter
{
   private static final long serialVersionUID = 1L;
   private static final Log log = Logging.getLog(IdFilter.class);
   private List<Long> ids;

   public IdFilter(List<Long> ids)
   {
      this.ids = ids;
   }
   
   @Override
   public DocIdSet getDocIdSet(IndexReader reader) throws IOException
   {
      OpenBitSet bitSet = new OpenBitSet(reader.maxDoc());
      if (log.isDebugEnabled())
         log.debug("getDocIdSet for {0} ids", ids.size());
      for (Long id : ids)
      {
         Term term = new Term("id", id.toString());
         TermDocs termDocs = reader.termDocs(term);
         while (termDocs.next())
            bitSet.set(termDocs.doc());
      }
      return bitSet;
   }
}
