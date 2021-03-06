package org.zanata.webtrans.client.history;

import org.zanata.webtrans.client.presenter.MainView;

import com.allen_sauer.gwt.log.client.Log;

/**
 * Encapsulates a string token of key-value pairs for GWT history operations.
 * 
 * @author David Mason, damason@redhat.com
 * 
 */
public class HistoryToken
{
   private static final String DELIMITER_K_V = ":";
   private static final String PAIR_SEPARATOR = ";";

   public static final String KEY_DOCUMENT = "doc";

   public static final String KEY_VIEW = "view";
   public static final String VALUE_EDITOR_VIEW = "doc";

   public static final String KEY_DOC_FILTER_TEXT = "filter";

   public static final String KEY_DOC_FILTER_OPTION = "filtertype";
   public static final String VALUE_DOC_FILTER_EXACT = "exact";

   private MainView view;
   private String fullDocPath;
   private boolean docFilterExact;
   private String docFilterText;

   // defaults
   private static final MainView DEFAULT_VIEW = MainView.Documents;
   private static final String DEFAULT_DOCUMENT_PATH = "";
   private static final String DEFAULT_DOC_FILTER_TEXT = "";
   private static final boolean DEFAULT_DOC_FILTER_EXACT = false;

   public HistoryToken()
   {
      view = DEFAULT_VIEW;
      fullDocPath = DEFAULT_DOCUMENT_PATH;
      docFilterText = DEFAULT_DOC_FILTER_TEXT;
      docFilterExact = DEFAULT_DOC_FILTER_EXACT;
   }

   /**
    * Generate a history token from the given token string
    * 
    * @param token A GWT history token in the form key1:value1,key2:value2,...
    */
   public static HistoryToken fromTokenString(String token)
   {
      HistoryToken historyToken = new HistoryToken();

      if (token == null || token.length() == 0)
      {
         return historyToken;
      }

      // decode characters that may still be url-encoded
      token = token.replaceAll("%3A", ":").replaceAll("%3B", ";").replaceAll("%2F", "/");

      for (String pairString : token.split(PAIR_SEPARATOR))
      {
         String[] pair = pairString.split(DELIMITER_K_V);
         String key;
         String value;
         try
         {
            key = pair[0];
            value = pair[1];
         }
         catch (ArrayIndexOutOfBoundsException e)
         {
            continue;
         }

         if (key.equals(HistoryToken.KEY_DOCUMENT))
         {
            historyToken.setDocumentPath(value);
         }
         else if (key.equals(HistoryToken.KEY_VIEW))
         {
            if (value.equals(VALUE_EDITOR_VIEW))
            {
               historyToken.setView(MainView.Editor);
            }
            // else default will be used
         }
         else if (key.equals(KEY_DOC_FILTER_OPTION))
         {
            if (value.equals(VALUE_DOC_FILTER_EXACT))
            {
               historyToken.setDocFilterExact(true);
            }
            // else default used
         }
         else if (key.equals(HistoryToken.KEY_DOC_FILTER_TEXT))
         {
            historyToken.setDocFilterText(value);
         }

         else
            Log.info("unrecognised history key: " + key);

      }

      return historyToken;
   }

   public String getDocumentPath()
   {
      return fullDocPath;
   }

   public void setDocumentPath(String fullDocPath)
   {
      if (fullDocPath == null)
         this.fullDocPath = DEFAULT_DOCUMENT_PATH;
      else
         this.fullDocPath = fullDocPath;
   }

   public MainView getView()
   {
      return view;
   }

   public void setView(MainView view)
   {
      if (view == null)
         this.view = DEFAULT_VIEW;
      else
         this.view = view;
   }

   public boolean getDocFilterExact()
   {
      return docFilterExact;
   }

   public void setDocFilterExact(boolean exactMatch)
   {
      docFilterExact = exactMatch;
   }

   public String getDocFilterText()
   {
      return docFilterText;
   }

   public void setDocFilterText(String value)
   {
      if (value == null || value.length() == 0)
         this.docFilterText = DEFAULT_DOC_FILTER_TEXT;
      else
         this.docFilterText = value;
   }

   /**
    * @return a token string for use with
    *         {@link com.google.gwt.user.client.History}
    */
   public String toTokenString()
   {
      String token = "";
      boolean first = true;

      if (view != DEFAULT_VIEW)
      {
         if (first)
            first = false;
         else
            token += PAIR_SEPARATOR;
         // editor is the only non-default view
         token += KEY_VIEW + DELIMITER_K_V + VALUE_EDITOR_VIEW;
      }

      if (!fullDocPath.equals(DEFAULT_DOCUMENT_PATH))
      {
         if (first)
            first = false;
         else
            token += PAIR_SEPARATOR;
         token += KEY_DOCUMENT + DELIMITER_K_V + fullDocPath;
      }

      if (docFilterExact != DEFAULT_DOC_FILTER_EXACT)
      {
         if (first)
            first = false;
         else
            token += PAIR_SEPARATOR;
         // exact is the only non-default filter value
         token += KEY_DOC_FILTER_OPTION + DELIMITER_K_V + VALUE_DOC_FILTER_EXACT;
      }

      if (!docFilterText.equals(DEFAULT_DOC_FILTER_TEXT))
      {
         if (first)
            first = false;
         else
            token += PAIR_SEPARATOR;
         token += KEY_DOC_FILTER_TEXT + DELIMITER_K_V + docFilterText;
      }

      return token;
   }
}
