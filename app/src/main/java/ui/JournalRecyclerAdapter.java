package ui;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.self.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import Model.Journal;

public class JournalRecyclerAdapter extends RecyclerView.Adapter<JournalRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<Journal> journalList;

    public JournalRecyclerAdapter(Context context, List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;


    }

    @NonNull
    @Override
    public JournalRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.journal_row, viewGroup, false);


        return new ViewHolder(view, context);
    }

    @Override       //binds widgets with the data
    public void onBindViewHolder(@NonNull JournalRecyclerAdapter.ViewHolder viewHolder, int position) {

        Journal journal = journalList.get(position);
        String imageUrl;

        viewHolder.title.setText(journal.getTitle());
        viewHolder.thoughts.setText(journal.getThought());
//        viewHolder.name.setText(journal.getUserName());
        imageUrl = journal.getImageUrl();
        //method used to get time ago like "1 hour ago"
        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(journal.getTimeAdded().getSeconds() * 1000);
        viewHolder.dateAdded.setText(timeAgo);
        /*
        Use Picasso Library to download and show image

        Use placeholder just in case Image cant be loaded
        use fit() to fit image into viewholder
         */
        Picasso.get().load(imageUrl).placeholder(R.drawable.image_three).fit().into(viewHolder.image);



    }

    @Override
    public int getItemCount() {
        //get the size of the Journal List Array
        return journalList.size();
    }

    //this is what contains all the items on the Layout xml journal_row.xml
    public class ViewHolder extends RecyclerView.ViewHolder {
        //declared every TextView
        public TextView
                title,
                thoughts,
                dateAdded,
                name;

        public ImageView image;

        String userId;
        String username;

        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);

            context = ctx;

            title = itemView.findViewById(R.id.jouranl_title_list);
            thoughts = itemView.findViewById(R.id.jouranl_thought_list);
            dateAdded = itemView.findViewById(R.id.journal_timestamp_list);
            image = itemView.findViewById(R.id.journal_image_list);
           // username = itemView.findViewById(R.id.username_account);

        }
    }
}
