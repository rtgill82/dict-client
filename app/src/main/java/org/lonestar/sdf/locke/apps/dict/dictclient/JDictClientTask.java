/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dict.dictclient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.text.Html;
import android.widget.TextView;

import org.lonestar.sdf.locke.libs.dict.Definition;
import org.lonestar.sdf.locke.libs.dict.Dictionary;
import org.lonestar.sdf.locke.libs.dict.JDictClient;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JDictClientTask extends AsyncTask<Void, Void, JDictClientResult>
{
  private Activity context;
  private TextView dictView;
  private JDictClientRequest request;
  private Exception exception;

  private ProgressDialog progressDialog;

  private static final Map<JDictClientRequest.JDictClientCommand, String> messages =
    new EnumMap<JDictClientRequest.JDictClientCommand, String>(JDictClientRequest.JDictClientCommand.class);

  static
  {
    messages.put (JDictClientRequest.JDictClientCommand.DEFINE, "Looking up word...");
    messages.put (JDictClientRequest.JDictClientCommand.DICT_INFO, "Retrieving dictionary information...");
    messages.put (JDictClientRequest.JDictClientCommand.DICT_LIST, "Retrieving available dictionaries...");
  }

  public JDictClientTask (Activity context, JDictClientRequest request)
  {
    super ();
    this.context = context;
    this.request = request;
    dictView = (TextView) context.findViewById (R.id.dict_view);
  }

  @Override
  protected void onPreExecute ()
  {
    disableInput ();
    progressDialog = ProgressDialog.show (context,
                                          "Waiting",
                                          messages.get (request.getCommand ()),
                                          true);
  }

  protected JDictClientResult doInBackground (Void... voids)
  {
    try
      {
        switch (request.getCommand ())
          {
          case DEFINE:
            return new JDictClientResult (
              request,
              getDefinitions (request.getWord (), request.getDictionary ())
            );
          case DICT_INFO:
            return new JDictClientResult (
              request,
              getDictionaryInfo (request.getDictionary ())
            );
          case DICT_LIST:
            return new JDictClientResult (
              request,
              getDictionaries ()
            );
          default:
            break;
          }
      }
    catch (Exception e)
      {
        if (!(e instanceof NullPointerException))
          {
            exception = e;
          }
        else
          {
            throw (NullPointerException) e;
          }
      }

    return null;
  }

  @Override
  protected void onPostExecute (JDictClientResult result)
  {
    DefinitionHistory history = DefinitionHistory.getInstance ();

    enableInput ();
    progressDialog.dismiss ();

    if (exception != null)
      {
        if (request.getCommand () == JDictClientRequest.JDictClientCommand.DICT_LIST)
          disableInput ();

        ErrorDialog.show (context, exception.getMessage ());
        return;
      }

    switch (result.getRequest ().getCommand ())
      {
      case DEFINE:
        CharSequence text = displayDefinitions (result.getDefinitions ());
        HistoryEntry entry = new HistoryEntry (request.getWord (), text);
        history.add (entry);
        context.invalidateOptionsMenu ();
        break;

      case DICT_INFO:
        displayDictionaryInfo (result.getDictionaryInfo ());
        break;

      case DICT_LIST:
        Host host = request.getHost ();
        host.setDictionaries (result.getDictionaries ());
        ((MainActivity) context).setDictionarySpinnerData (result.getDictionaries ());
        break;

      default:
        break;
      }
  }

  private List<Dictionary> getDictionaries ()
  throws Exception
  {
    Host host = request.getHost ();
    JDictClient dictClient =
      JDictClient.connect (host.getHostName (), host.getPort ());

    List<Dictionary> dictionaries = new ArrayList<Dictionary>();
    dictionaries.add (new Dictionary (null, "All Dictionaries"));
    dictionaries.addAll (dictClient.getDictionaries ());
    dictClient.close ();
    return dictionaries;
  }

  private List<Definition> getDefinitions (String word, Dictionary dictionary)
  throws Exception
  {
    Host host = request.getHost ();
    JDictClient dictClient =
      JDictClient.connect (host.getHostName (), host.getPort ());

    List<Definition> definitions;
    if (dictionary.getDatabase () != null)
      definitions = dictClient.define (dictionary.getDatabase (), word);
    else
      definitions = dictClient.define (word);

    dictClient.close ();
    return definitions;
  }

  private String getDictionaryInfo (Dictionary dictionary)
  throws Exception
  {
    Host host = request.getHost ();
    JDictClient dictClient = JDictClient.connect (host.getHostName (),
                                                  host.getPort ());
    String dictInfo = dictClient.getDictionaryInfo (dictionary.getDatabase ());
    dictClient.close ();
    return dictInfo;
  }

  private CharSequence displayDefinitions (List<Definition> definitions)
  {
    dictView.setText ("");
    if (definitions == null)
      dictView.setText ("No definitions found.");
    else
      {
        Iterator<Definition> itr = definitions.iterator ();
        while (itr.hasNext ())
          {
            Definition definition = itr.next ();
            Dictionary dictionary = definition.getDictionary ();

            dictView.append (Html.fromHtml ("<b>" + dictionary.getDescription () + "</b><br>"));
            dictView.append (DefinitionParser.parse (definition));
            dictView.append ("\n");
          }
      }

    return dictView.getText ();
  }

  private void displayDictionaryInfo (String dictInfo)
  {
    dictView.setText ("");

    if (dictInfo == null)
      dictView.setText ("No dictionary info received.");
    else
      dictView.setText (dictInfo);
  }

  private void disableInput ()
  {
    context.findViewById (R.id.search_text).setEnabled (false);
    context.findViewById (R.id.dict_spinner).setEnabled (false);
    context.findViewById (R.id.dictinfo_button).setEnabled (false);
  }

  private void enableInput ()
  {
    context.findViewById (R.id.search_text).setEnabled (true);
    context.findViewById (R.id.dict_spinner).setEnabled (true);
    context.findViewById (R.id.dictinfo_button).setEnabled (true);
  }
}
