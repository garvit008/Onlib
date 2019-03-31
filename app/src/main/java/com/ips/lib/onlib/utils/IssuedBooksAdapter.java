package com.ips.lib.onlib.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ips.lib.onlib.Models.Book;
import com.ips.lib.onlib.R;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class IssuedBooksAdapter extends RecyclerView.Adapter<IssuedBooksAdapter.MyViewHolder> {

    private Context context;
    private List<Book> books;

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private ImageView bookCover;
        private TextView bookName, bookAuthor, bookEdtion, issueDateTv, returnDateTv;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.bookCoverIv);
            bookName = itemView.findViewById(R.id.bookName);
            bookAuthor = itemView.findViewById(R.id.bookAuthor);
            bookEdtion = itemView.findViewById(R.id.bookEdition);
            issueDateTv = itemView.findViewById(R.id.issueDate);
            returnDateTv = itemView.findViewById(R.id.returnDate);
        }
    }
    public IssuedBooksAdapter(Context context, List<Book> books) {
        this.context = context;
        this.books = books;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.issued_book_list_item, viewGroup, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
          Book book = books.get(i);
          myViewHolder.bookName.setText(book.getName());
          myViewHolder.bookAuthor.setText("By "+ book.getAuthor());
          myViewHolder.bookEdtion.setText("Edition: "+ book.getEdition());
          Date date = DateHelper.getFormattedDate(book.getIssue_date());
          String issueDate = DateHelper.getSimpleDateString(date);
          myViewHolder.issueDateTv.setText(issueDate.replace(".", ""));
          Calendar calendar = Calendar.getInstance();
          calendar.setTime(date);
          calendar.add(Calendar.DATE, 30);
          String returnDate = DateHelper.getSimpleDateString(calendar.getTime());
          myViewHolder.returnDateTv.setText(returnDate.replace(".", ""));
          UniversalImageLoader.setImage(book.getCover(), myViewHolder.bookCover, null, "");
    }

    @Override
    public int getItemCount() {
        return books.size();
    }
}
