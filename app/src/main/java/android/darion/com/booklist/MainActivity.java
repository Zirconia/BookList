package android.darion.com.booklist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check for internet connectivity
        boolean isConnected = BookUtils.isConnectedToInternet(getApplicationContext());

        //informs the user if they are not connected to the internet
        if(!isConnected){
            //displays "No internet connection." message if not connected to the internet
            TextView noInternetTextView = (TextView)findViewById(R.id.noInternet);
            noInternetTextView.setText(getApplicationContext().getString(R.string.no_internet));
            noInternetTextView.setVisibility(View.VISIBLE);

            //hides the search box and search button if not connected to the internet
            EditText searchTex = (EditText) findViewById(R.id.search_box);
            searchTex.setVisibility(View.INVISIBLE);
            Button searchButton = (Button) findViewById(R.id.search_button);
            searchButton.setVisibility(View.INVISIBLE);
        }
    }

    /*
     *Invoked when search button is clicked. Launches BookListActivity to display list of books
     *related to the entered search words.
     */
    public void searchBooks(View v){
        if(!BookUtils.isConnectedToInternet(getApplicationContext())){
            //displays "No internet connection." message if not connected to the internet
            TextView noInternetTextView = (TextView)findViewById(R.id.noInternet);
            noInternetTextView.setText(getApplicationContext().getString(R.string.no_internet));
            noInternetTextView.setVisibility(View.VISIBLE);

            //hides the search box and search button if not connected to the internet
            EditText searchTex = (EditText) findViewById(R.id.search_box);
            searchTex.setVisibility(View.INVISIBLE);
            Button searchButton = (Button) findViewById(R.id.search_button);
            searchButton.setVisibility(View.INVISIBLE);

            //shows button to retry starting app after user has fixed their connection
            Button retryBtn = (Button)findViewById(R.id.retry_button);
            retryBtn.setVisibility(View.VISIBLE);
        }

        Intent intent = new Intent(this, BookListActivity.class);
        EditText searchTopicTex = (EditText) findViewById(R.id.search_box);
        String topic = searchTopicTex.getText().toString().trim();

        //only launches BookListActivity if the user has entered text into search box
        if(!TextUtils.isEmpty(topic)){
            intent.putExtra("TOPIC",topic);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(!BookUtils.isConnectedToInternet(getApplicationContext())){
            //displays "No internet connection." message if not connected to the internet
            TextView noInternetTextView = (TextView)findViewById(R.id.noInternet);
            noInternetTextView.setText(getApplicationContext().getString(R.string.no_internet));
            noInternetTextView.setVisibility(View.VISIBLE);

            //hides the search box and search button if not connected to the internet
            EditText searchTex = (EditText) findViewById(R.id.search_box);
            searchTex.setVisibility(View.INVISIBLE);
            Button searchButton = (Button) findViewById(R.id.search_button);
            searchButton.setVisibility(View.INVISIBLE);

            //shows button to retry starting app after user has fixed their connection
            Button retryBtn = (Button)findViewById(R.id.retry_button);
            retryBtn.setVisibility(View.VISIBLE);
        }
    }

    /*
     *Hides the "no internet" UI and re-displays the search box
     *and search button for the user if they are now connected to the internet.
     */
    public void retrySearch(View v){
        if(BookUtils.isConnectedToInternet(getApplicationContext())){
            TextView noInternetTextView = (TextView)findViewById(R.id.noInternet);
            noInternetTextView.setText(getApplicationContext().getString(R.string.no_internet));
            noInternetTextView.setVisibility(View.INVISIBLE);

            Button retryBtn = (Button)findViewById(R.id.retry_button);
            retryBtn.setVisibility(View.INVISIBLE);


            EditText searchTex = (EditText) findViewById(R.id.search_box);
            searchTex.setVisibility(View.VISIBLE);
            Button searchButton = (Button) findViewById(R.id.search_button);
            searchButton.setVisibility(View.VISIBLE);
        }
    }
}
