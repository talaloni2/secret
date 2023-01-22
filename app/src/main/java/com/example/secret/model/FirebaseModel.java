package com.example.secret.model;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.example.secret.interfaces.Listener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;

public class FirebaseModel {
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth auth;


    FirebaseModel() {
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        db.setFirestoreSettings(settings);
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public void getAllUsersSince(Long since, Listener<List<User>> callback) {
        db.collection(User.COLLECTION)
                .whereGreaterThanOrEqualTo(User.LAST_UPDATED, new Timestamp(since, 0))
                .get()
                .addOnCompleteListener(task -> {
                    List<User> list = new LinkedList<>();
                    if (task.isSuccessful()) {
                        QuerySnapshot jsonsList = task.getResult();
                        for (DocumentSnapshot json : jsonsList) {
                            User user = User.fromJson(json.getData());
                            list.add(user);
                        }
                    }
                    callback.onComplete(list);
                });
    }

    public void register(User user, String password, Listener<Void> successListener,
                         Listener<Void> failListener) {
        String TAG = "REGISTER";
        auth.createUserWithEmailAndPassword(user.email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "createUserWithEmail:success");
                FirebaseUser fbUser = auth.getCurrentUser();
                user.setId(fbUser.getUid());
                addUser(user, successListener);
            } else {
                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                failListener.onComplete(null);
            }
        });
    }

    public void addUser(User user, Listener<Void> listener) {
        db.collection(User.COLLECTION).document(user.getId()).set(user.toJson())
                .addOnCompleteListener(task -> listener.onComplete(null));
    }

    void uploadImage(String name, Bitmap bitmap, Listener<String> listener) {
        StorageReference storageRef = storage.getReference();
        StorageReference imagesRef = storageRef.child("images/" + name + ".jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imagesRef.putBytes(data);
        uploadTask
                .addOnFailureListener(exception -> listener.onComplete(null))
                .addOnSuccessListener(taskSnapshot ->
                        imagesRef.getDownloadUrl().addOnSuccessListener(
                                uri -> listener.onComplete(uri.toString())
                        ));

    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void signIn(String email, String password, Listener<Void> successListener, Listener<Void> failedListener) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                successListener.onComplete(null);
                return;
            }
            failedListener.onComplete(null);
        });
    }
}
