/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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
package org.zanata.webtrans.shared.validation.action;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class NewlineLeadTrailValidation extends ValidationAction
{
   public NewlineLeadTrailValidation(String id, String description)
   {
      super(id, description);
   }

   private final static String leadNewlineRegex = "^\n";
   private final static String endNewlineRegex = "\n$";

   private final static RegExp leadRegExp = RegExp.compile(leadNewlineRegex);
   private final static RegExp endRegExp = RegExp.compile(endNewlineRegex);

   @Override
   public void validate(String source, String target)
   {
      MatchResult sourceResult = leadRegExp.exec(source);
      if (sourceResult != null)
      {
         Log.debug("Found leading newline in source");
         MatchResult targetResult = leadRegExp.exec(target);
         if (targetResult == null)
         {
            addError(getId() + ": Leading newline not found in target");
         }
      }

      sourceResult = endRegExp.exec(source);
      if (sourceResult != null)
      {
         Log.debug("Found trailing newline in source");
         MatchResult targetResult = endRegExp.exec(target);
         if (targetResult == null)
         {
            addError(getId() + ": Trailing newline not found in target");
         }
      }
   }
}