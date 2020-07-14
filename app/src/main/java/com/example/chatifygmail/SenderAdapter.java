package com.example.chatifygmail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatifygmail.database.Sender;

import java.util.List;

public class SenderAdapter extends RecyclerView.Adapter<SenderAdapter.SenderViewHolder> {

    final private ItemClickListener mItemClickListener;
    private List<Sender> senders;
    private Context mContext;

    public SenderAdapter(Context context, ItemClickListener listener){
        mContext = context;
        mItemClickListener = listener;
    }

    @NonNull
    @Override
    public SenderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.sender_layout, parent, false);

        return new SenderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SenderViewHolder holder, int position) {
        Sender sender = senders.get(position);
        String emailAddress = sender.getEmailAddress();
        int unread = sender.getUnread();

        holder.emailTextView.setText(emailAddress);
        String unreadString = "" + unread; // converts int to String
        holder.unreadView.setText(unreadString);
        Log.i("Parameters Width Bind: ",holder.unreadView.getMeasuredWidth()+"");
        Log.i("Parameters HeightBind: ",holder.unreadView.getMeasuredHeight()+"");
        GradientDrawable priorityCircle = (GradientDrawable) holder.unreadView.getBackground();
        // Get the appropriate background color based on the priority
        int unreadColor = R.color.colorPrimaryDark;
        priorityCircle.setColor(mContext.getResources().getColor(unreadColor) );
    }

    @Override
    public int getItemCount() {
        if (senders == null) {
            return 0;
        }
        return senders.size();
    }

    public List<Sender> getSenders() {
        return senders;
    }

    public void setSenders(List<Sender> senders) {
        this.senders = senders;
    }

    class SenderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView emailTextView;
        TextView unreadView;


        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.email_address_text_view);

            unreadView = itemView.findViewById(R.id.unread_text_view);
            int width  = Resources.getSystem().getDisplayMetrics().widthPixels;
            /*Display display = ((Activity)mContext).getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int screen_width = size.x;
            int screen_height = size.y;*/
            //int height = Resources.getSystem().getDisplayMetrics().heightPixels;
            int targetHeight = (int) ((width-(2*dpToPx(16)))/10);

            Log.i("Parameters Width: ",width+"");

            Log.i("Parameters Height: ",targetHeight+"");

            unreadView.getLayoutParams().height = targetHeight;
            unreadView.setLayoutParams(unreadView.getLayoutParams());
            //unreadView.setLayoutParams(new LinearLayout.LayoutParams(width, height));
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Intent intent = new Intent(mContext, AddSenderActivity.class);
                    intent.putExtra(AddSenderActivity.EXTRA_EMAIL_ID, emailTextView.getText());
                    mContext.startActivity(intent);
                    return true;
                }
            });

        }

        @Override
        public void onClick(View view) {
            Sender sender = senders.get(getAdapterPosition());
            mItemClickListener.onItemClickListener(sender);
        }
    }

    public interface ItemClickListener {
        void onItemClickListener(Sender sender);
    }
    public int dpToPx(int dp) {
        float density = mContext.getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }
}
