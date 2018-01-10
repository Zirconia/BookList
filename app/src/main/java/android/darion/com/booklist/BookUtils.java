package android.darion.com.booklist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by BlackMagicianDTT on 1/3/2018.
 */

public final class BookUtils {
    public static final String LOG_TAG = BookUtils.class.getName();

    private BookUtils(){}

    /**
     * Returns a list of Book objects that has been built up from
     * parsing a JSON response.
     */
    private static ArrayList<Book> extractBooks(String bookJSON) {

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(bookJSON)) {
            return null;
        }

        ArrayList<Book> books = new ArrayList<>();

        //parses out the relevant information from the JSON response
        try {
            JSONObject booksRoot = new JSONObject(bookJSON);
            JSONArray items = booksRoot.optJSONArray("items");

            //return early if no books related to the user's entered words were found
            if(items == null){
                return books;
            }

            for(int i = 0; i < items.length(); ++i){
                JSONObject jsonBook = items.getJSONObject(i);
                JSONObject volumeInfo = jsonBook.getJSONObject("volumeInfo");
                JSONArray jsonAuthors = volumeInfo.optJSONArray("authors");
                String title = volumeInfo.getString("title");
                String bookImageUrl = volumeInfo.getJSONObject("imageLinks").getString("thumbnail");
                String bookIsbn = getIsbn13(volumeInfo);
                Bitmap bookImgBitmap = null;
                String previewUrl = volumeInfo.getString("previewLink");

                ArrayList<String> authors;
                if(jsonAuthors != null) {
                    authors = jarrayToArrayList(jsonAuthors);
                }
                else{
                    authors = new ArrayList<String>();
                    authors.add("N/A");
                }

                if(bookImageUrl != null) {
                    URL url = BookUtils.createUrl(bookImageUrl);
                    try {
                        bookImgBitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    }
                    catch(IOException e){
                        Log.e(LOG_TAG, "Problem parsing the book JSON results", e);
                    }
                }

                if(bookIsbn == null) {
                    bookIsbn = "N/A";
                }

                Book book = new Book(title, authors, bookImageUrl, bookImgBitmap, bookIsbn, previewUrl);
                books.add(book);
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e(LOG_TAG, "Problem parsing the book JSON results", e);
        }

        return books;
    }

    /*
     *Converts JSONArray to an ArrayList<String>
     */
    private static ArrayList<String> jarrayToArrayList(JSONArray jarray){
        ArrayList<String> result = new ArrayList<>();
        for(int i = 0; i < jarray.length(); ++i){
            try {
                result.add(jarray.getString(i));
            }
            catch(JSONException e){
                Log.e(LOG_TAG, "Error converting JSONArray to ArrayList<String>");
            }
        }

        return result;
    }

    /*
     * Makes an HTTP request to the given URL and returns a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                Log.d(LOG_TAG,"connection request successful.");
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }
            else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the book JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /*
     * Converts the InputStream into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }

        return output.toString();
    }

    /*
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    /*
     *Helper function for parsing out a Book's 13 digit ISBN to a String
     */
    private static String getIsbn13(JSONObject volumeInfo){
        String result = null;
        try {
            JSONArray identifiers = volumeInfo.getJSONArray("industryIdentifiers");
            JSONObject industryIdentifier;
            for(int i = 0; i < identifiers.length(); ++i){
                industryIdentifier = identifiers.getJSONObject(i);
                if(industryIdentifier.getString("type").equals("ISBN_13")){
                    result = industryIdentifier.getString("identifier");
                    break;
                }
            }
        }
        catch(JSONException e){
            Log.e(LOG_TAG, "Problem retrieving the book JSON results.", e);
        }

        return result;
    }

    /*
     * Queries the Google Books API and returns a List<Book> object to represent a single book.
     */
    public static List<Book> fetchBookData(String requestUrl) {
        URL url = createUrl(requestUrl);

        //Performs HTTP request to the URL and receives a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        //Extracts relevant fields from the JSON response and creates a list of books from them
        List<Book> books = extractBooks(jsonResponse);

        return books;
    }

    /*
     *Returns true iff the user is connected to the internet
     */
    public static boolean isConnectedToInternet(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return  isConnected;
    }
}
