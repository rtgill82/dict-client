/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dict.dictclient;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import org.lonestar.sdf.locke.libs.dict.Definition;
import org.lonestar.sdf.locke.libs.dict.JDictClient;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

class JDictClientTask extends AsyncTask<Void, Void, JDictClientResult>
{
  private MainActivity context;
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

  public JDictClientTask (MainActivity context, JDictClientRequest request)
  {
    super ();
    this.context = context;
    this.request = request;
  }

  @Override
  protected void onPreExecute ()
  {
    progressDialog = ProgressDialog.show (
        context,
        "Waiting",
        messages.get (request.getCommand ()),
        true
    );
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
    progressDialog.dismiss ();
    context.onTaskFinished (result, exception);
  }

  private List<Dictionary> getDictionaries ()
  throws Exception
  {
    Host host = request.getHost ();
    JDictClient dictClient =
      JDictClient.connect (host.getHostName (), host.getPort ());

    List<Dictionary> dictionaries = new ArrayList<Dictionary>();
    dictionaries.add (Dictionary.ALL_DICTIONARIES);
    dictionaries.addAll (ClassConvert.convert (dictClient.getDictionaries (),
                                               host));
    dictClient.close ();
    return dictionaries;
  }

  @Override
  protected void onCancelled (JDictClientResult result)
  {
    progressDialog.dismiss ();
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
}
