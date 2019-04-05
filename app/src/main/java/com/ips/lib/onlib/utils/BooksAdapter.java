package com.ips.lib.onlib.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.ips.lib.onlib.EditProfileActivity;
import com.ips.lib.onlib.Models.BookRefined;
import com.ips.lib.onlib.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.MyViewHolder>{

    private static final String TAG = "BooksAdapter";
    private List<BookRefined> bookList;
    private Context context;
    private String activity;
    private OnClickListerner onClickListerner;
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView name, author, editon, availability, branch;
        public ImageView cover, optionsMenu;
        OnClickListerner onClickListerner;
        public MyViewHolder(View view, OnClickListerner onClickListerner) {
            super(view);
            name = (TextView) view.findViewById(R.id.bookName);
            author = (TextView) view.findViewById(R.id.bookAuthor);
            editon = (TextView) view.findViewById(R.id.bookEdition);
            availability = (TextView) view.findViewById(R.id.bookAvailability);
            cover = (ImageView) view.findViewById(R.id.bookCoverIv);
            branch = view.findViewById(R.id.branch);
            optionsMenu = view.findViewById(R.id.optionsIv);
            this.onClickListerner = onClickListerner;
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
              onClickListerner.onClick(getAdapterPosition(), cover);
        }
    }

    public interface OnClickListerner{
        void onClick(int position, ImageView view);
    }

    public BooksAdapter(Context context, List<BookRefined> bookList, String activity, OnClickListerner onClickListerner) {
        this.context = context;
        this.bookList = bookList;
        this.activity = activity;
        this.onClickListerner = onClickListerner;
        initImageLoader();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = null;
        if(activity.equals("CatalogueActivity")){
            itemView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.book_list_item, viewGroup, false);
        }
        else if(activity.equals("SearchActivity")){
            itemView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.search_list_item, viewGroup, false);
        }
        else {
            itemView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.wishlist_item, viewGroup, false);
        }


        return  new MyViewHolder(itemView, onClickListerner);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, int i) {
          BookRefined book = bookList.get(i);
          myViewHolder.name.setText(book.getName());
          myViewHolder.author.setText("by " + book.getAuthor());
          myViewHolder.editon.setText("Editon: "+ book.getEdition());
          int available = Integer.parseInt(book.getAvailable());
          final int position = i;
          if(available == 0){
              myViewHolder.availability.setTextColor(context.getResources().getColor(R.color.red));
          }
          else
          {
              myViewHolder.availability.setTextColor(context.getResources().getColor(R.color.text_green));
          }
          if(activity.equals("SearchActivity")){
              myViewHolder.branch.setText("Branch: "+book.getBranch());
          }
          if(activity.equals("MyBooksActivity")){
              myViewHolder.optionsMenu.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View view) {
                      PopupMenu popup = new PopupMenu(context, myViewHolder.optionsMenu);
                      //inflating menu from xml resource
                      popup.inflate(R.menu.wishlist_item_menu);
                      //adding click listener
                      popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                          @Override
                          public boolean onMenuItemClick(MenuItem item) {
                              switch (item.getItemId()) {
                                  case R.id.remove:
                                      Log.d(TAG, "onMenuItemClick: options menu clicked");
                                      //handle menu1 click
                                      removeFromDatabase(position);
                                      break;

                              }
                              return false;
                          }
                      });
                      //displaying the popup
                      popup.show();
                  }
              });
          }
          myViewHolder.availability.setText("Copies available: "+ book.getAvailable());
          UniversalImageLoader.setImage(book.getCover(), myViewHolder.cover, null, "");
          ViewCompat.setTransitionName(myViewHolder.cover, book.getName());
    }

    @Override
    public int getItemCount() {
        if(bookList  == null){
            return 0;
        }
        return bookList.size();
    }

    private void initImageLoader(){
        UniversalImageLoader imageLoader = new UniversalImageLoader(context);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    private void removeFromDatabase(int position){
        FirebaseDatabase.getInstance().getReference().child(context.getString(R.string.dbname_wishlist))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(BookHelper.getRefinedKey(bookList.get(position)))
                .removeValue();
        bookList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, bookList.size());
        Toast.makeText(context, "Book removed", Toast.LENGTH_SHORT).show();
    }

}

