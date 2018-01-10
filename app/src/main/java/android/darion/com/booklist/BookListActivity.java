package android.darion.com.booklist;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class BookListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Book>>{

    private static final String LOG_TAG = BookListActivity.class.getName();
    private static final String BASE_URL = "https://www.googleapis.com/books/v1/volumes?q=";        //the base url that is always used when querying the google books API.
    private static final String MAX_RESULTS = "&maxResults=10";                                     //argument appended to base url that determines how many new book results to get.
    private static StringBuilder request;                                                           //used to build the desired query url.
    private static String requestUrl;                                                               //the complete url we actually use to query the google books API.
    private BookAdapter bookAdapter;                                                                //custom adapter for listView of books.
    private TextView mEmptyStateTextView;                                                           //displays a "No books found." message when no books matching the user's seaarch are found.
    private View ftView;                                                                            //displays a spinning progress bar at bottom of listView when user tries to load more books.

    private ListView bookListView;
    private String searchArgs;                                                                      //the words the user entered for the search.
    private static int currListSize = 10;                                                           //stores the size of the book list.
    private ArrayList<Book> currBooks = new ArrayList<Book>();                                      //list of books used by the BookAdapter and ListView for display purposes.
    private static final int LOADER_ID = 0;
    private boolean noMoreBooks = false;                                                            //indicates whether or not it is possible to fetch more books.
    private boolean alreadyInformed = false;                                                        //used to prevent app from informing the user multiple times that there are no more books to fetch.
    private boolean isLoading = false;                                                              //determines whether or not the app is currently trying to load book info into the listView.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        //initializes the spinning progress bar for the footer of the book list view
        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ftView = li.inflate(R.layout.footer_view,null);

        if(savedInstanceState != null){
            requestUrl = savedInstanceState.getString("REQUEST_URL");
            isLoading = savedInstanceState.getBoolean("IS_LOADING");
            searchArgs = savedInstanceState.getString("SEARCH_ARGS");
            currListSize = savedInstanceState.getInt("LIST_SIZE");
            currBooks = (ArrayList<Book>)savedInstanceState.getSerializable("BOOKS_ARRAY");
            noMoreBooks = savedInstanceState.getBoolean("NO_MORE_BOOKS,");
            alreadyInformed = savedInstanceState.getBoolean("ALREADY_INFORMED");
        }
        else {
            // Get the Intent that started this activity and extract the string
            Intent intent = getIntent();
            String searchStr = intent.getStringExtra("TOPIC");
            searchArgs = searchStr;

            //construct the url we will use for the HTTP request
            //by combining the user's search criteria with the
            //base url, max result and startIndex arguments
            request = new StringBuilder();
            request.append(BASE_URL);
            request.append(searchStr);
            request.append(MAX_RESULTS);
            String startIndex = "&startIndex=0";
            request.append(startIndex);
            requestUrl = request.toString();
            requestUrl = requestUrl.replaceAll("\\s+", "+");
        }

        // Get a reference to the ListView, and attach the adapter to the listView.
        ListView listView = (ListView) findViewById(R.id.list);
        // Create a new adapter that takes an empty list of books as input
        bookAdapter = new BookAdapter(this, currBooks);
        listView.setAdapter(bookAdapter);
        //initialize global reference to listView
        bookListView = listView;

        //init a TextView to show a message to handle the case where there are no books to show
        mEmptyStateTextView = (TextView)findViewById(R.id.emptyState);
        listView.setEmptyView(mEmptyStateTextView);

        //make query to Google Books API in background thread
        getLoaderManager().initLoader(LOADER_ID, null, this);

        //sets an onclick listener to open the appropriate details page for the book
        //that was clicked
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Book book = bookAdapter.getItem(position);
                String bookUrl = book.getPreviewUrl();
                if(!TextUtils.isEmpty(bookUrl)) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(bookUrl)));
                }
            }
        });

        //sets an onscroll listener to load in 10 more books when the user scrolls to the bottom of
        //the listView
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //check when scroll to last item in listView
                if(view.getLastVisiblePosition() == totalItemCount - 1 && isLoading == false && !noMoreBooks){
                        isLoading = true;
                        getMoreBooks();
                }
                else if(noMoreBooks){
                    if(bookListView.getLastVisiblePosition() == bookListView.getAdapter().getCount() -1 &&
                            bookListView.getChildAt(bookListView.getChildCount() - 1).getBottom() <= bookListView.getHeight())
                    {
                        getMoreBooks();
                    }
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putString("REQUEST_URL",requestUrl);
        outState.putBoolean("IS_LOADING", isLoading);
        outState.putString("SEARCH_ARGS", searchArgs);
        outState.putInt("LIST_SIZE", currListSize);
        outState.putSerializable("BOOKS_ARRAY", currBooks);
        outState.putBoolean("NO_MORE_BOOKS", noMoreBooks);
        outState.putBoolean("ALREADY_INFORMED",alreadyInformed);
    }

    @Override
    public Loader<List<Book>> onCreateLoader(int id, Bundle args){
        bookListView.addFooterView(ftView);
        return new BookLoader(BookListActivity.this, requestUrl);
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> data){
        isLoading = false;
        bookListView.removeFooterView(ftView);

        //hide progress bar after loading is finished
        ProgressBar progress = (ProgressBar)findViewById(R.id.progressBar);
        progress.setVisibility(View.INVISIBLE);

        //checks if there are no more books to fetch
        if((data == null || data.size() == 0) && currBooks.size() > 0){
            noMoreBooks = true;
        }

        updateUi(data);

        //checks if we need to display "No internet connection." or "No books found." messages
        if((data == null || data.size() == 0) && currBooks.size() == 0 &&
                BookUtils.isConnectedToInternet(getApplicationContext()))
        {
            mEmptyStateTextView.setText(getApplicationContext().getString(R.string.no_books));
        }
        else if(!BookUtils.isConnectedToInternet(getApplicationContext())){
            mEmptyStateTextView.setText(getApplicationContext().getString(R.string.no_internet));
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader){}

    /**
     * Update the UI with the new books.
     */
    private void updateUi(List<Book> books) {
        if (books != null && !books.isEmpty() && bookAdapter != null) {
            currBooks.addAll(books);
            bookAdapter.notifyDataSetChanged();
        }
    }

    /*
     *Fetches 10 more books from Google Books API to display to the user.
     */
    public void getMoreBooks(){
        if(noMoreBooks && BookUtils.isConnectedToInternet(getApplicationContext())){
            if(!alreadyInformed) {
                Toast.makeText(this, getString(R.string.no_more_books),
                        Toast.LENGTH_SHORT).show();
            }
            alreadyInformed = true;
            isLoading = false;
            return;
        }
        else if(!BookUtils.isConnectedToInternet(getApplicationContext())){
                Toast.makeText(this, getString(R.string.no_internet),
                        Toast.LENGTH_SHORT).show();
                isLoading = false;
                return;
        }

        //constructs the url we will use to query for 10 more books
        request.setLength(0);
        request.append(BASE_URL);
        request.append(searchArgs);
        request.append(MAX_RESULTS);
        String newStartIndex = "&startIndex=";
        String newStart = Integer.toString(currListSize);
        newStartIndex += newStart;
        request.append(newStartIndex);
        requestUrl = request.toString();
        currListSize += 10;

        //query Google Books API for 10 more books in a background thread
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(!BookUtils.isConnectedToInternet(getApplicationContext())){
            hideBookListUI();
        }
        else{
            displayBookListUI();
        }
    }

    /*
     *Onclick handler for the "Retry" button. Displays book list UI iff "Retry" button is pressed and
     *the user now has an internet connection.
     */
    public void retrySearch(View v){
        if(BookUtils.isConnectedToInternet(getApplicationContext())){
            displayBookListUI();
        }
    }

    /*
     *Hides the book list UI and displays a "Retry" button and a "No internet connection." message.
     *Called when user is not connected to the internet and is using BookListActivity
     */
    public void hideBookListUI(){
        //hide listView
        ListView lv = (ListView)findViewById(R.id.list);
        lv.setVisibility(View.INVISIBLE);

        //displays "No internet connection." message if not connected to the internet
        TextView noInternetTextView = (TextView)findViewById(R.id.noInternet);
        noInternetTextView.setText(getApplicationContext().getString(R.string.no_internet));
        noInternetTextView.setVisibility(View.VISIBLE);

        //shows button to retry starting app after user has fixed their connection
        Button retryBtn = (Button)findViewById(R.id.retry_button);
        retryBtn.setVisibility(View.VISIBLE);
    }

    /*
     *Displays the book list UI and hides the "Retry" button and "No internet connection." message.
     *Called to redisplay UI when user has restored their internet connection.
     */
    public void displayBookListUI(){
        TextView noInternetTextView = (TextView)findViewById(R.id.noInternet);
        noInternetTextView.setText(getApplicationContext().getString(R.string.no_internet));
        noInternetTextView.setVisibility(View.INVISIBLE);

        Button retryBtn = (Button)findViewById(R.id.retry_button);
        retryBtn.setVisibility(View.INVISIBLE);

        //show listView
        ListView lv = (ListView)findViewById(R.id.list);
        lv.setVisibility(View.VISIBLE);
    }
}
