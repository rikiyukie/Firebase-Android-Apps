package com.fans.edu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fans.edu.Model.ChatMessage;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ChatRoom_Admin extends AppCompatActivity {

    private RecyclerView list_chat;
    private ScrollView scrollView;
    private String useremail;
    private EditText inputMessage;
    private ImageView sendBtn;
    private DatabaseReference mDatabase, adminRef, userRef;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private LinearLayoutManager layoutManager;
    private Query mQueryCurrent;
    private FirebaseRecyclerAdapter<ChatMessage, ChatRoom_Admin.EntryViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_admin);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);
        adminRef = mDatabase.child("ADMIN");
        userRef = mDatabase.child("Users");
        mQueryCurrent = mDatabase.child("CHAT_ROOM").orderByChild("sendDate");

        inputMessage = (EditText)findViewById(R.id.input_message_admin);
        sendBtn = (ImageView)findViewById(R.id.btn_send_admin);

        scrollView = (ScrollView)findViewById(R.id.scroll_chat_admin);

        list_chat = (RecyclerView)findViewById(R.id.chat_recyclerView_admin);
        list_chat.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        //layoutManager.setReverseLayout(true);
        //layoutManager.setStackFromEnd(true);
        list_chat.setLayoutManager(layoutManager);

        String currentUser = mUser.getUid();
        adminRef.child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    useremail = String.valueOf(dataSnapshot.child("email").getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sendBtn.setOnClickListener(sendMessage);
    }

    private View.OnClickListener sendMessage = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String message = inputMessage.getText().toString();
            if (!inputNull(message)){
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());
            String uuid = UUID.randomUUID().toString();

            ChatMessage chatMessage = new ChatMessage(useremail, message, currentDateandTime);
            mDatabase.child("CHAT_ROOM").child(uuid).setValue(chatMessage).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    inputMessage.setText(null);
                }
            });

        }
    };

    private boolean inputNull(String message) {
        boolean result = true;
        if (TextUtils.isEmpty(message)){
            inputMessage.setError("Pesan Kosong");
            result = false;
        }else {
            inputMessage.setError(null);
        }

        return result;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<ChatMessage> options = new FirebaseRecyclerOptions.Builder<ChatMessage>()
                .setQuery(mQueryCurrent, ChatMessage.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ChatMessage, EntryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final EntryViewHolder entryViewHolder, final int i, @NonNull ChatMessage chatMessage) {

                String senddate = chatMessage.getSendDate();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                Date date = null;
                try {
                    date = dateFormat.parse(senddate);
                }catch (ParseException e){
                    e.printStackTrace();
                }
                SimpleDateFormat newdateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
                String finalformat = newdateFormat.format(date);
                String emailchat = chatMessage.getUserEmailChat();

                userRef.orderByChild("Email").equalTo(emailchat).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot data : dataSnapshot.getChildren()){
                            String nickname = data.child("Username").getValue().toString();
                            String photourl = data.child("Userphoto").getValue().toString();
                            if (photourl.equalsIgnoreCase("unknown")){
                                ImageView ppuser = (ImageView)entryViewHolder.mView.findViewById(R.id.userProfile_chat);
                                ppuser.setImageResource(R.drawable.ic_person);
                            }else {
                                entryViewHolder.setPhoto(photourl);
                            }
                            entryViewHolder.setUserName(nickname);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                entryViewHolder.setMessage(chatMessage.getUserMessageChat());
                //entryViewHolder.setUserName(chatMessage.getUserNameChat());
                entryViewHolder.setTime(finalformat);

                if (chatMessage.getUserEmailChat().equalsIgnoreCase(useremail)){
                    entryViewHolder.getAdapterPosition();

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.ALIGN_PARENT_END);
                    LinearLayout kotakpesan = (LinearLayout)entryViewHolder.mView.findViewById(R.id.kotak_pesan);
                    kotakpesan.setLayoutParams(params);
                    kotakpesan.setBackground(getResources().getDrawable(R.drawable.ic_message_bg_2));

                    ImageView ppuser = (ImageView)entryViewHolder.mView.findViewById(R.id.userProfile_chat);
                    ppuser.setVisibility(View.GONE);

                    LinearLayout.LayoutParams paramsUserName = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    paramsUserName.gravity = Gravity.END;
                    LinearLayout nicklayout = (LinearLayout)entryViewHolder.mView.findViewById(R.id.layout_nick);
                    nicklayout.setLayoutParams(paramsUserName);
                    /*TextView namauser = (TextView)entryViewHolder.mView.findViewById(R.id.userName_Chat);
                    namauser.setLayoutParams(paramsUserName);*/

                    LinearLayout.LayoutParams paramsTimeChat = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    paramsTimeChat.gravity = Gravity.END;
                    paramsTimeChat.topMargin = 10;
                    TextView sendtime = (TextView)entryViewHolder.mView.findViewById(R.id.time_chat);
                    sendtime.setLayoutParams(paramsTimeChat);

                }else {
                    //null
                }

                mDatabase.child("verified").orderByChild("email").equalTo(emailchat).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            ImageView centang = (ImageView)entryViewHolder.mView.findViewById(R.id.icon_verified);
                            centang.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_card, parent, false);
                return new EntryViewHolder(view);
            }
        };

        list_chat.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public class EntryViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView chat_username, chat_message, chat_time;
        ImageView chat_photo;

        public EntryViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void setUserName (String userName){
            chat_username = (TextView)mView.findViewById(R.id.userName_Chat);
            chat_username.setText(userName);
        }

        public void setMessage (String message){
            chat_message = (TextView)mView.findViewById(R.id.message_user);
            chat_message.setText(message);
        }

        public void setTime (String time){
            chat_time = (TextView)mView.findViewById(R.id.time_chat);
            chat_time.setText(time);
        }

        public void setPhoto (String photoUrl){
            chat_photo = (ImageView)mView.findViewById(R.id.userProfile_chat);
            Picasso.with(getApplicationContext()).load(photoUrl).into(chat_photo);
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    public void onBackPressed(){
        //kembali ke home page
        Intent back = new Intent(ChatRoom_Admin.this, Halaman_Admin.class);
        startActivity(back);
        finish();
    }
}
