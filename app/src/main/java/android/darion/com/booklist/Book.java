package android.darion.com.booklist;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by BlackMagicianDTT on 1/3/2018.
 */

public class Book implements Serializable{
    private String title;
    private ArrayList<String> authors;
    private String imageUrl;
    private transient Bitmap bookImgBitmap;
    private String isbn;
    private String previewUrl;

    public Book(String tit, ArrayList<String> auth, String img, Bitmap bit, String isb, String pUrl){
        title = tit;
        authors = auth;
        imageUrl = img;
        bookImgBitmap = bit;
        isbn = isb;
        previewUrl = pUrl;
    }

    public String getTitle(){
        return title;
    }

    public ArrayList<String> getAuthors(){return authors;}

    public String getImageUrl(){
        return imageUrl;
    }

    public String getIsbn(){return isbn;}

    public Bitmap getBookImgBitmap(){return bookImgBitmap;}

    public String getPreviewUrl(){return previewUrl;}

    public void setTitle(String tit){
        title = tit;
    }

    public void setAuthors(ArrayList<String> auth){
        authors = auth;
    }

    public void setImageUrl(String img){
        imageUrl = img;
    }

    public void setIsbn(String isb){
        isbn = isb;
    }

    public void setBookImgBitmap(Bitmap bit){
        bookImgBitmap = bit;
    }

    public void setPreviewUrl(String pUrl){previewUrl = pUrl;}
}
