package com.fans.edu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.UUID;

public class AkunSetting extends AppCompatActivity {

    private TextView usrNama, usrEmail, usrNickname, uploadPhotoProgress;
    private ImageView usrPhoto, edtNickname, edtPhoto, selectPhoto, deletePhoto, edtNamaLengkap;
    private EditText inputNick, inputNama;
    private Button saveNick, saveNama , savePhoto, enableEdit, disableEdit;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private Uri filePath;
    private String photoUrl, currentPhoto;
    private Dialog customDialogNick, customDialogPhoto, customDialogNama;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_akun_setting);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        usrNama = (TextView)findViewById(R.id.usr_namauser);
        usrEmail = (TextView)findViewById(R.id.usr_emailuser);
        usrNickname = (TextView)findViewById(R.id.usr_nickname);
        usrPhoto = (ImageView)findViewById(R.id.usr_userPhoto);
        edtNickname = (ImageView)findViewById(R.id.edit_nickname);
        edtNamaLengkap = (ImageView)findViewById(R.id.edit_namalengkap);
        edtPhoto = (ImageView)findViewById(R.id.edit_photouser);
        deletePhoto = (ImageView)findViewById(R.id.delete_photouser);
        enableEdit = (Button)findViewById(R.id.editor_enable);
        disableEdit = (Button)findViewById(R.id.editor_disable);

        String uid = mAuth.getCurrentUser().getUid();
        mDatabase.child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String getNama = dataSnapshot.child("Nama").getValue().toString();
                String getEmail = dataSnapshot.child("Email").getValue().toString();
                String getNick = dataSnapshot.child("Username").getValue().toString();
                currentPhoto = dataSnapshot.child("Userphoto").getValue().toString();

                usrNama.setText(getNama);
                usrEmail.setText(getEmail);
                usrNickname.setText(getNick);

                if (currentPhoto.equalsIgnoreCase("unknown")){
                    usrPhoto.setImageDrawable(getResources().getDrawable(R.drawable.ic_person));
                }else {
                    Context context = getApplicationContext();
                    Picasso.with(context).load(currentPhoto).into(usrPhoto);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        PhotoCustomDialog();
        NamaCustomDialog();
        NickCustomDialog();
        edtPhoto.setOnClickListener(openDialogPhoto);
        edtNickname.setOnClickListener(openDialogNick);
        edtNamaLengkap.setOnClickListener(openDialogNama);
        deletePhoto.setOnClickListener(deletePhotoUser);
        enableEdit.setOnClickListener(enableEditor);
        disableEdit.setOnClickListener(disableEditor);
    }

    private void NamaCustomDialog() {
        customDialogNama = new Dialog(AkunSetting.this);
        customDialogNama.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialogNama.setContentView(R.layout.edit_nama_dialog);
        customDialogNama.setCancelable(true);

        inputNama = customDialogNama.findViewById(R.id.input_namalengkap);
        saveNama = customDialogNama.findViewById(R.id.save_namalengkap);
        saveNama.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newnama = inputNama.getText().toString();
                if (TextUtils.isEmpty(newnama)){
                    inputNama.setError("Tidak Boleh Kosong");
                }else {
                    inputNama.setError(null);
                    String uid = mAuth.getCurrentUser().getUid();
                    DatabaseReference refChild = mDatabase.child("Users");
                    refChild.child(uid).child("Nama").setValue(newnama)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AkunSetting.this, "Success", Toast.LENGTH_SHORT).show();
                            customDialogNama.dismiss();
                        }
                    });
                }

            }
        });
    }

    private void PhotoCustomDialog() {
        customDialogPhoto = new Dialog(AkunSetting.this);
        customDialogPhoto.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialogPhoto.setContentView(R.layout.edit_photo_dialog);
        customDialogPhoto.setCancelable(true);

        uploadPhotoProgress = customDialogPhoto.findViewById(R.id.upload_photo_progress);
        selectPhoto = customDialogPhoto.findViewById(R.id.pick_Photo);
        selectPhoto.setOnClickListener(pickPhoto);
        savePhoto = customDialogPhoto.findViewById(R.id.save_photo);
        savePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (filePath != null){
                    if (currentPhoto.equalsIgnoreCase("unknown")){
                        uploadNewPhoto();
                    }else {
                        storageReference.getStorage().getReferenceFromUrl(currentPhoto).delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                uploadNewPhoto();
                            }
                        });
                    }

                }
            }
        });

    }

    private void uploadNewPhoto() {
        final StorageReference ref = storageReference.child("Photo/"+ UUID.randomUUID().toString());
        ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(AkunSetting.this, "Photo Uploaded", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progres = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                uploadPhotoProgress.setVisibility(View.VISIBLE);
                uploadPhotoProgress.setText("Please Wait..!! Uploading Photo "+(int)progres+"%");
                //progressDialog.setMessage("Uploaded "+(int)progres+"%");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AkunSetting.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    uploadPhotoProgress.setVisibility(View.GONE);
                    Uri downUri = task.getResult();
                    photoUrl = downUri.toString();
                    String uid = mAuth.getCurrentUser().getUid();
                    DatabaseReference refChild = mDatabase.child("Users").child(uid);
                    refChild.child("Userphoto").setValue(photoUrl);
                    selectPhoto.setImageDrawable(getResources().getDrawable(R.drawable.ic_person));
                    Toast.makeText(AkunSetting.this, "Success", Toast.LENGTH_SHORT).show();
                    customDialogPhoto.dismiss();
                }
            }
        });
    }

    private void NickCustomDialog() {
        customDialogNick = new Dialog(AkunSetting.this);
        customDialogNick.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialogNick.setContentView(R.layout.edit_nickname_dialog);
        customDialogNick.setCancelable(true);

        inputNick = customDialogNick.findViewById(R.id.input_nickname);
        saveNick = customDialogNick.findViewById(R.id.save_nickname);
        saveNick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String newNickname = inputNick.getText().toString();
                final String uid = mAuth.getCurrentUser().getUid();
                final DatabaseReference refChild = mDatabase.child("Users");
                refChild.orderByChild("Username").equalTo(newNickname).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    inputNick.setError("Sudah digunakan");
                                }else {
                                    refChild.child(uid).child("Username").setValue(newNickname).addOnSuccessListener(
                                            new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(AkunSetting.this, "Success", Toast.LENGTH_SHORT).show();
                                                    customDialogNick.dismiss();
                                                }
                                            }
                                    );
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        }
                );
            }
        });
    }

    public static final int PICK_IMAGE = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null){

            filePath = data.getData();
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                selectPhoto.setImageBitmap(bitmap);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private View.OnClickListener pickPhoto = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);
        }
    };

    private View.OnClickListener openDialogNama = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            customDialogNama.show();
        }
    };

    private View.OnClickListener openDialogNick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            customDialogNick.show();
        }
    };

    private View.OnClickListener openDialogPhoto = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            customDialogPhoto.show();
        }
    };

    private View.OnClickListener deletePhotoUser = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (currentPhoto.equalsIgnoreCase("unknown")){
                Toast.makeText(AkunSetting.this, "Photo belum ditambahkan", Toast.LENGTH_SHORT).show();
            }else {
                String uid = mAuth.getCurrentUser().getUid();
                mDatabase.child("Users").child(uid).child("Userphoto").setValue("unknown");
                storageReference.getStorage().getReferenceFromUrl(currentPhoto).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(AkunSetting.this, "Photo telah dihapus", Toast.LENGTH_SHORT).show();
                        usrPhoto.setImageDrawable(getResources().getDrawable(R.drawable.ic_person));
                    }
                });
            }
        }
    };

    private View.OnClickListener enableEditor = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            edtPhoto.setVisibility(View.VISIBLE);
            deletePhoto.setVisibility(View.VISIBLE);
            edtNamaLengkap.setVisibility(View.VISIBLE);
            edtNickname.setVisibility(View.VISIBLE);

            enableEdit.setVisibility(View.GONE);
            disableEdit.setVisibility(View.VISIBLE);
        }
    };

    private View.OnClickListener disableEditor = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            edtPhoto.setVisibility(View.GONE);
            deletePhoto.setVisibility(View.GONE);
            edtNamaLengkap.setVisibility(View.GONE);
            edtNickname.setVisibility(View.GONE);

            enableEdit.setVisibility(View.VISIBLE);
            disableEdit.setVisibility(View.GONE);
        }
    };

}
