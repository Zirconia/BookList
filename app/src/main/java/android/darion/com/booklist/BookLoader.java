package android.darion.com.booklist;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List; 

/**
 * Created by BlackMagicianDTT on 1/3/2018.
 */

public class BookLoader extends AsyncTaskLoader<List<Book>> {
    private static final String LOG_TAG = BookLoader.class.getName();

    //Query URL
    private String mUrl;

    public BookLoader(Context context, String url){
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Book> loadInBackground(){
        if (mUrl.length() < 1 || mUrl == null) {
            return null;
        }

        // Perform the HTTP request for earthquake data and process the response.
        List<Book> result = BookUtils.fetchBookData(mUrl);
        return result;
    }
}
