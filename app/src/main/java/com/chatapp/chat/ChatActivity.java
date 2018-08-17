package com.chatapp.chat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chatapp.MyApplication;
import com.chatapp.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Member;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pub.devrel.easypermissions.EasyPermissions;

import static com.chatapp.MyApplication.getAppContext;
import static com.chatapp.MyApplication.sender;


public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    DatabaseReference myRef;
    ChatMessageAdapter mAdapter;
    Integer receiver_id, sender_id = -1;
    String chatPath;
    String senderAvatar = "";
    List<String> childKeys = new ArrayList<>();
    StorageReference storageRef;
    ChildEventListener childEventListener;
    FirebaseDatabase database;
    @BindView(R.id.chat_lv)
    ListView mChatView;
    @BindView(R.id.message)
    EditText etMessage;
    @BindView(R.id.attachment)
    ImageView attachment;
    @BindView(R.id.send_messsage)
    ImageView send;


    //Requesting run-time permissions
    private long enqueue;
    private DownloadManager dm;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    Activity thisActivity;
    MediaRecorder mRecorder = null;

    List<Chat> chatList = new ArrayList<>();

    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
    String uploaded_image_url ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);


        thisActivity = this;
        chatPath = "1U_chats_1P";


        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(
                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enqueue);
                    Cursor c = dm.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c
                                .getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c
                                .getInt(columnIndex)) {

                          /*  ImageView view = (ImageView) findViewById(R.id.imageView1);
                            String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            view.setImageURI(Uri.parse(uriString));*/
                            String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            //   view.setImageURI(Uri.parse(uriString));
                            Log.e("uriString", "uriString" + Uri.parse(uriString));
                            Toast.makeText(context, "Download Completed", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        };

        this.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        storageRef = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();


        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }


        etMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                      String myText = etMessage.getText().toString().trim();
                        if (myText.length() > 0) {
                            sendMessage(myText);
                        }

                    handled = true;
                }
                return handled;
            }
        });

        etMessage.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etMessage.getText().toString().length() != 0) {
                    send.setVisibility(View.VISIBLE);
                    attachment.setVisibility(View.GONE);
                } else {
                    send.setVisibility(View.GONE);
                    attachment.setVisibility(View.VISIBLE);
                }
            }
        });

        sender_id = MyApplication.sender;
        receiver_id  = MyApplication.receiver;


        mAdapter = new ChatMessageAdapter(this, chatList, senderAvatar);
        mChatView.setAdapter(mAdapter);



        myRef = database.getReference().child(chatPath);
        childEventListener = myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                childKeys.add(dataSnapshot.getKey());
                Chat chat = dataSnapshot.getValue(Chat.class);
                assert chat != null;
                if (chat.getReceiver() != null && chat.getRead() != null) {
                    if (chat.getReceiver().equals(sender_id) && chat.getRead() == 0) {
                        chat.setRead(1);
                        dataSnapshot.getRef().setValue(chat);
                    }
                }

                mAdapter.add(chat);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {


                Chat chat = dataSnapshot.getValue(Chat.class);
                if (chat.getReceiver().equals(receiver_id) && chat.getRead() == 1) {
                    mAdapter.getItem(chatList.size() - 1).setRead(1);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });




    }

    @Override
    protected void onResume() {
        super.onResume();

    }



    @OnClick({R.id.attachment, R.id.send_messsage})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.attachment:

                    openAttachmentDialog();

                break;
            case R.id.send_messsage:


                    String myText = etMessage.getText().toString().trim();
                    if (myText.length() > 0) {
                        sendMessage(myText);
                    }

                break;


        }
    }


    private void sendMessage(String message) {

        Chat chat = new Chat();
        chat.setSender(sender_id);
        chat.setTimestamp(System.currentTimeMillis());
        chat.setType("text");
        chat.setText(message);
        chat.setReceiver(receiver_id);
        chat.setUrl("");
        chat.setRead(0);

        myRef.push().setValue(chat);

        etMessage.setText("");


    }

    private void sendFile(File file, final String type) {

        Toast.makeText(this, getString(R.string.uploading), Toast.LENGTH_SHORT).show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        final StorageReference ref = storageReference.child(chatPath + "/" + file.getName());
        UploadTask uploadTask1 = ref.putFile(Uri.fromFile(file));

        progressBar.setIndeterminate(true);


        Task<Uri> urlTask = uploadTask1.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    //this is the uri you needed...
                    uploaded_image_url = downloadUri.toString();

                    Chat chat = new Chat();
                    chat.setSender(sender_id);
                    chat.setTimestamp(System.currentTimeMillis());
                    chat.setType(type);
                    chat.setUrl(String.valueOf(uploaded_image_url));
                    chat.setReceiver(receiver_id);
                    chat.setRead(0);
                    myRef.push().setValue(chat);

                    Toast.makeText(ChatActivity.this, "Image uploaded!", Toast.LENGTH_SHORT).show();

                    progressBar.setIndeterminate(false);
                } else {
                    // Handle failures
                    // ...
                    Toast.makeText(ChatActivity.this, "Image uploading failed ", Toast.LENGTH_SHORT).show();

                    progressBar.setIndeterminate(false);
                }
            }});

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                e.printStackTrace();
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                sendFile(imageFile, "image");
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {

            }
        });


    }



    public String getMimeType(Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = getAppContext().getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);


        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                EasyImage.openCamera(ChatActivity.this, 0);
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myRef != null) {
            myRef.removeEventListener(childEventListener);
            childEventListener = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    private void openAttachmentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] animals = {"Camera", "Gallery"};
        builder.setItems(animals, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            EasyImage.openCamera(ChatActivity.this, 0);
                        } else {
                            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                        }
                        break;
                    case 1:
                        if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            EasyImage.openGallery(ChatActivity.this, 0);
                        } else {
                            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                        }
                        break;

                    default:
                        break;
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


}

