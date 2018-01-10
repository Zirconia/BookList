package android.darion.com.booklist;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by BlackMagicianDTT on 1/3/2018.
 */

public class BookAdapter extends ArrayAdapter<Book> {

    public BookAdapter(Activity context, ArrayList<Book> books) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for several TextViews and an ImageView, the adapter is
        // not going to use this second argument, so it can be any value. Here, we used 0.
        super(context, 0, books);
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position The position in the list of data that should be displayed in the
     *                 list item view.
     * @param convertView The recycled view to populate.
     * @param parent The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.book_list_item, parent, false);
        }

        Book currentBook = getItem(position);

        //sets the image bitmap for the ImageView at position "position"
        //if the book has no image, a default image is displayed instead
        ImageView bookImage = (ImageView) listItemView.findViewById(R.id.bookImage);
        Bitmap b = currentBook.getBookImgBitmap();
        if(b != null) {
            bookImage.setImageBitmap(currentBook.getBookImgBitmap());
        }

        //sets the book's title to be displayed in the bookTitle TextView
        TextView bookTitle = (TextView) listItemView.findViewById(R.id.bookTitle);
        bookTitle.setText(currentBook.getTitle());

        //sets the name of one of the book's authors to be displayed in the bookAuthor TextView
        TextView authorTex = (TextView) listItemView.findViewById(R.id.bookAuthor);
        ArrayList<String> authors = currentBook.getAuthors();
        authorTex.setText(authors.get(0));

        //sets the book's ISBN to be displayed in the bookIsbn TextView
        TextView bookIsbn = (TextView) listItemView.findViewById(R.id.bookIsbn);
        bookIsbn.setText(currentBook.getIsbn());

        return listItemView;
    }
}
