package com.ips.lib.onlib.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ips.lib.onlib.EditProfileActivity;
import com.ips.lib.onlib.Models.BookRefined;
import com.ips.lib.onlib.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.MyViewHolder>{

    private List<BookRefined> bookList;
    private Context context;
    private String activity;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, author, editon, availability, branch;
        public ImageView cover;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.bookName);
            author = (TextView) view.findViewById(R.id.bookAuthor);
            editon = (TextView) view.findViewById(R.id.bookEdition);
            availability = (TextView) view.findViewById(R.id.bookAvailability);
            cover = (ImageView) view.findViewById(R.id.bookCoverIv);
            branch = view.findViewById(R.id.branch);
        }
    }

    public BooksAdapter(Context context, List<BookRefined> bookList, String activity) {
        this.context = context;
        this.bookList = bookList;
        this.activity = activity;
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
        else{
            itemView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.search_list_item, viewGroup, false);
        }


        return  new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
          BookRefined book = bookList.get(i);
          myViewHolder.name.setText(book.getName());
          myViewHolder.author.setText("by " + book.getAuthor());
          myViewHolder.editon.setText("Editon: "+ book.getEdition());
          int available = Integer.parseInt(book.getAvailable());
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
          myViewHolder.availability.setText("Copies available: "+ book.getAvailable());
          UniversalImageLoader.setImage(book.getCover(), myViewHolder.cover, null, "");
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

}

