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

import java.util.Date;
import java.util.List;

public class IssueHistoryAdapter extends RecyclerView.Adapter<IssueHistoryAdapter.MyViewHolder> {

    private List<Book> bookList;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, author, editon, branch, issueDate;
        public ImageView cover;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.bookName);
            author = (TextView) view.findViewById(R.id.bookAuthor);
            editon = (TextView) view.findViewById(R.id.bookEdition);
            cover = (ImageView) view.findViewById(R.id.bookCoverIv);
            branch = view.findViewById(R.id.bookBranch);
            issueDate = view.findViewById(R.id.issueDate);
        }
    }

    public IssueHistoryAdapter(List<Book> bookList, Context context) {
        this.bookList = bookList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.issue_history_item, viewGroup, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        Book book = bookList.get(i);
        myViewHolder.name.setText(book.getName());
        myViewHolder.author.setText("by " + book.getAuthor());
        myViewHolder.editon.setText("Editon: "+ book.getEdition());
        myViewHolder.branch.setText("Branch: " + book.getBranch());
        Date date = DateHelper.getFormattedDate(book.getIssue_date());
        myViewHolder.issueDate.setText("Last issued on: " + DateHelper.getSimpleDateString(date).replace(".", ""));
        UniversalImageLoader.setImage(book.getCover(), myViewHolder.cover, null, "");

    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }
}
