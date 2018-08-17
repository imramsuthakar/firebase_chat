package com.chatapp.chat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chatapp.MyApplication;
import com.chatapp.R;
import com.chatapp.util.PhotoViewActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;

import java.util.List;
import java.util.Objects;



public class ChatMessageAdapter extends ArrayAdapter<Chat> {
    private static final int MY_MESSAGE = 0, OTHER_MESSAGE = 1, MY_IMAGE = 2, OTHER_IMAGE = 3;
    private Integer senderId = -1;
    private Context context;
    String senderAvatar;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    ClipboardManager clipboard;

    public ChatMessageAdapter(Context context, List<Chat> data, String senderAvatar) {
        super(context, R.layout.item_mine_message, data);
        this.context = context;
        this.senderId =  MyApplication.sender;
        this.senderAvatar = senderAvatar;
        clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    public int getViewTypeCount() {
        // my message, other message, my image, other image
        return 2;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int getItemViewType(int position) {
        Chat item = getItem(position);

        if (item.getSender().equals(senderId)) {
            return MY_MESSAGE;
        } else
            return OTHER_MESSAGE;

        /*if (item.getIsMine()) return MY_MESSAGE;
        else return OTHER_MESSAGE;
*/
        /*if (item.isMine() && !item.isImage()) return MY_MESSAGE;
        else if (!item.isMine() && !item.isImage()) return OTHER_MESSAGE;
        else if (item.isMine() && item.isImage()) return MY_IMAGE;
        else return OTHER_IMAGE;*/
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        int viewType = getItemViewType(position);
        final Chat chat = getItem(position);
        if (viewType == MY_MESSAGE) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_mine_message, parent, false);
            if (chat.getType().equals("text")) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_mine_message, parent, false);
                TextView textView = convertView.findViewById(R.id.text);
                textView.setText(getItem(position).getText());
                TextView timeStamp = convertView.findViewById(R.id.time_stamp);

                if(chat.getTimestamp()!=null)
                    timeStamp.setText(MyApplication.getDisplayableTime(chat.getTimestamp()));

//                Utilities.printV("chat.getRead()===>",chat.getRead()+"");

                if (chat.getRead() != null)
                    if (chat.getRead() == 1) {
                        timeStamp.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_double_tick, 0);
                    }
            } else if (chat.getType().equals("image")) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_mine_image, parent, false);
                ImageView mine_image = convertView.findViewById(R.id.mine_image);
                mine_image.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent i = new Intent(getContext(), PhotoViewActivity.class);
                        i.putExtra("url", chat.getUrl());
                        context.startActivity(i);
                    }
                });
                Glide.with(context).load(chat.getUrl()).apply(new RequestOptions().placeholder(R.drawable.ic_gallery).error(R.drawable.ic_gallery).centerCrop().dontAnimate()).into(mine_image);
                TextView timeStamp = convertView.findViewById(R.id.time_stamp);
                if(chat.getTimestamp()!=null)
                   timeStamp.setText(MyApplication.getDisplayableTime(chat.getTimestamp()));
                if (chat.getRead() == 1) {
                    timeStamp.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_double_tick, 0);
                }
            }
        } else

        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_other_message, parent, false);
            if (chat.getType().equals("text")) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_other_message, parent, false);
                TextView textView = convertView.findViewById(R.id.text);
                textView.setText(getItem(position).getText());
                TextView timeStamp = convertView.findViewById(R.id.time_stamp);
                if(chat.getTimestamp()!=null)
                    timeStamp.setText(MyApplication.getDisplayableTime(chat.getTimestamp()));

                TextView user_name = convertView.findViewById(R.id.user_name);


            } else if (chat.getType().equals("image")) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_other_image, parent, false);
                ImageView other_image = convertView.findViewById(R.id.other_image);

                other_image.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent i = new Intent(getContext(), PhotoViewActivity.class);
                        i.putExtra("url", chat.getUrl());
                        context.startActivity(i);
                    }
                });

                Glide.with(context).load(chat.getUrl()).apply(new RequestOptions().placeholder(R.drawable.ic_gallery).error(R.drawable.ic_gallery).centerCrop().dontAnimate()).into(other_image);
                TextView timeStamp = convertView.findViewById(R.id.time_stamp);
                if(chat.getTimestamp()!=null)
                    timeStamp.setText(MyApplication.getDisplayableTime(chat.getTimestamp()));



            }
        }

        convertView.findViewById(R.id.chatMessageView).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {


                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                String[] items = {"Copy"};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                if (chat.getType().equalsIgnoreCase("text")) {
                                    ClipData clip = ClipData.newPlainText(null, chat.getText());
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                                }
                                break;

                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();



                return true;
            }
        });


        return convertView;
    }
}
