package com.darioguida.calendarapp;

import android.util.Log;
import android.util.Xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class SuggestTask implements Runnable {
    private static final String TAG = "SuggestTask";
    private final SuggestionActivity suggest;
    private final String original;

    SuggestTask(SuggestionActivity context, String original) {
        this.suggest = context;
        this.original = original;
    }

    public void run() {
        // Get suggestions for the original text
        List<String> suggestions = doSuggest(original);
        suggest.setSuggestions(suggestions);
    }

    /**
     * Call the Google Suggest API to create a list of suggestions
     * from a partial string.
     * <p/>
     * Note: This isn't really a supported API so if it breaks, try
     * the Yahoo one instead:
     * <p/>
     * http://ff.search.yahoo.com/gossip?output=xml&command=WORD or
     * http://ff.search.yahoo.com/gossip?output=fxjson&command=WORD
     */
    private List<String> doSuggest(String original) {
        List<String> messages = new LinkedList<String>();
        String error = null;
        HttpURLConnection con = null;
        Log.d(TAG, "doSuggest(" + original + ")");

        try {
            // Check if task has been interrupted
            if (Thread.interrupted())
                throw new InterruptedException();

            // Build RESTful query for Google API
            String q = URLEncoder.encode(original, "UTF-8");
            URL url = new URL("http://thesaurus.altervista.org/thesaurus/v1?word=" + q + "&language=en_US&key=gB5tplzjJee641pjQDmL&output=xml");
            con = (HttpURLConnection) url.openConnection();
            con.setReadTimeout(10000 /* milliseconds */);
            con.setConnectTimeout(15000 /* milliseconds */);
            con.setRequestMethod("GET");

            con.setDoInput(true);

            // Start the query
            con.connect();

            // Check if task has been interrupted
            if (Thread.interrupted())
                throw new InterruptedException();

            // Read results from the query
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(con.getInputStream(), null);
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = docBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            Document doc = null;
            try {
                doc = docBuilder.parse(con.getInputStream(), null);
            } catch (SAXException e) {
                e.printStackTrace();
            }
            NodeList list = doc.getElementsByTagName("list");
            int totalList = list.getLength();


            for (int i = 0; i < list.getLength(); i++) {

                Node firstBookNode = list.item(i);
                if (firstBookNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element firstElement = (Element) firstBookNode;


                    //-------
                    NodeList firstNameList = firstElement.getElementsByTagName("synonyms");
                    Element firstNameElement = (Element) firstNameList.item(0);

                    NodeList textFNList = firstNameElement.getChildNodes();
                    String[] s = textFNList.item(0).getNodeValue().trim().split("[\\s+|,\\s*|\\.\\s*\\s(\\s)]");
                    for (String l : s) {
                        messages.add(l);
                    }

                }
            }
            // Check if task has been interrupted
            if (Thread.interrupted())
                throw new InterruptedException();

        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
            error = suggest.getResources().getString(R.string.error)
                    + " " + e.toString();
        } catch (XmlPullParserException e) {
            Log.e(TAG, "XmlPullParserException", e);
            error = suggest.getResources().getString(R.string.error)
                    + " " + e.toString();
        } catch (InterruptedException e) {
            Log.d(TAG, "InterruptedException", e);
            error = suggest.getResources().getString(
                    R.string.interrupted);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }

        // If there was an error, return the error by itself
        if (error != null) {
            messages.clear();
            messages.add(error);
        }

        // Print something if we got nothing
        if (messages.size() == 0) {
            messages.add(suggest.getResources().getString(
                    R.string.no_results));
        }

        // All done
        //Log.d(TAG, "   -> returned " + messages);

        Log.d(TAG, "   -> returned " + messages);
        return messages;
    }
}

