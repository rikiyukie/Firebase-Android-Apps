package com.fans.edu;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.print.PrintAttributes;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fans.edu.Model.ChatMessage;
import com.fans.edu.Model.Video;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
import java.util.Random;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 */
public class tab3 extends Fragment {

    private RecyclerView list_chat;
    private String useremail;
    private EditText inputMessage;
    private ImageView sendBtn;
    private DatabaseReference mDatabase, userRef;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private Query mQueryCurrent;
    private FirebaseRecyclerAdapter<ChatMessage, tab3.EntryViewHolder> firebaseRecyclerAdapter;

    public tab3() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab3, container, false);
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);
        userRef = mDatabase.child("Users");
        mQueryCurrent = mDatabase.child("CHAT_ROOM").orderByChild("sendDate");

        inputMessage = (EditText)rootView.findViewById(R.id.input_message);
        sendBtn = (ImageView)rootView.findViewById(R.id.btn_send);

        list_chat = (RecyclerView)rootView.findViewById(R.id.chat_recyclerView);
        list_chat.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        list_chat.setLayoutManager(layoutManager);

        sendBtn.setOnClickListener(sendMessage);

        String currentUser = mUser.getUid();
        userRef.child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    useremail = String.valueOf(dataSnapshot.child("Email").getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return rootView;
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

                    LinearLayout.LayoutParams paramsTimeChat = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    paramsTimeChat.gravity = Gravity.END;
                    paramsTimeChat.topMargin = 10;
                    TextView sendtime = (TextView)entryViewHolder.mView.findViewById(R.id.time_chat);
                    sendtime.setLayoutParams(paramsTimeChat);

                }else {

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
            Picasso.with(getActivity()).load(photoUrl).into(chat_photo);
        }

    }

}
