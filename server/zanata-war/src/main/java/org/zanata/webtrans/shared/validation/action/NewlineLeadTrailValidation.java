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

import org.zanata.webtrans.client.resources.ValidationMessages;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.regexp.shared.RegExp;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class NewlineLeadTrailValidation extends ValidationAction
{
   public NewlineLeadTrailValidation(final ValidationMessages messages)
   {
      super(messages.newlineValidatorName(), messages.newlineValidatorDescription(), true, messages);
   }

   private final static String leadNewlineRegex = "^\n";
   private final static String endNewlineRegex = "\n$";

   private final static RegExp leadRegExp = RegExp.compile(leadNewlineRegex);
   private final static RegExp endRegExp = RegExp.compile(endNewlineRegex);

   @Override
   public void doValidate(String source, String target)
   {
      if (!shareLeading(source, target))
         addError(getMessages().leadingNewlineMissing());

      if (!shareLeading(target, source))
         addError(getMessages().leadingNewlineAdded());

      if (!shareTrailing(source, target))
         addError(getMessages().trailingNewlineMissing());

      if (!shareTrailing(target, source))
         addError(getMessages().trailingNewlineAdded());
   }

   /**
    * @return false if base has a leading newline and test does not, true
    *         otherwise
    */
   private boolean shareLeading(String base, String test)
   {
      if (leadRegExp.test(base))
      {
         Log.debug("Found leading newline");
         return leadRegExp.test(test);
      }
      // no newline so can't fail
      return true;
   }

   /**
    * @return false if base has a trailing newline and test does not, true
    *         otherwise
    */
   private boolean shareTrailing(String base, String test)
   {
      if (endRegExp.test(base))
      {
         Log.debug("Found trailing newline");
         return endRegExp.test(test);
      }
      // no newline so can't fail
      return true;
   }
}
