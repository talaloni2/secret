package com.example.secret.model;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.secret.interfaces.Listener;
import com.google.firebase.Timestamp;
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
import java.util.Map;

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

    public void register(User user, String password, Listener<Void> successListener,
                         Listener<Void> failListener) {
        String TAG = "REGISTER";
        auth.createUserWithEmailAndPassword(user.email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "createUserWithEmail:success");
                FirebaseUser fbUser = auth.getCurrentUser();
                user.setId(fbUser.getUid());
                setUser(user, successListener, failListener);
            } else {
                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                failListener.onComplete(null);
            }
        });
    }

    public void setUser(User user, Listener<Void> successListener, Listener<Void> failListener) {
        db.collection(User.COLLECTION).document(user.getId()).set(user.toJson())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        successListener.onComplete(null);
                        return;
                    }
                    Log.e("SetUser", "Could not set User");
                    failListener.onComplete(null);
                });
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

    public boolean isUserConnected() {
        return auth.getCurrentUser() != null;
    }
    public String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public void getCurrentUser(Listener<User> onCurrentUserReceived, Listener<Void> onCurrentUserNotReceived) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            onCurrentUserNotReceived.onComplete(null);
            return;
        }

        db.collection(User.COLLECTION).document(firebaseUser.getUid()).get().addOnCompleteListener(
                task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            User u = User.fromJson(document.getData());
                            onCurrentUserReceived.onComplete(u);
                            return;
                        }
                    }
                    Log.w("GetCurrentUser", "Could not get user from store, auth was successful");
                    onCurrentUserNotReceived.onComplete(null);
                }
        );
    }

    public void signIn(String email, String password, Listener<Void> successListener, Listener<Void> failedListener) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                successListener.onComplete(null);
                return;
            }
            failedListener.onComplete(null);
        });
    }

    public void signOut() {
        auth.signOut();
    }

    public void setComment(Comment comment, Listener<Void> successListener, Listener<Void> failListener) {
        db.collection(Comment.COLLECTION).document(comment.getId()).set(comment.toJson())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        successListener.onComplete(null);
                        return;
                    }
                    Log.e("SetComment", "Could not set Comment");
                    failListener.onComplete(null);
                });
    }

    public void getAllCommentsSince(Long since, Listener<List<Comment>> callback) {
        db.collection(Comment.COLLECTION)
                .whereGreaterThanOrEqualTo(Comment.LAST_UPDATED, new Timestamp(since + 1, 0))
                .orderBy(Comment.LAST_UPDATED)
                .limit(10000)
                .get()
                .addOnCompleteListener(task -> {
                    List<Comment> comments = new LinkedList<>();
                    if (task.isSuccessful()) {
                        QuerySnapshot jsonsList = task.getResult();
                        for (DocumentSnapshot json : jsonsList) {
                            Comment comment = Comment.fromJson(json.getData());
                            comments.add(comment);
                        }
                    }
                    callback.onComplete(comments);
                });
    }

    public void setPost(Post post, Listener<Void> successListener, Listener<Void> failListener) {
        db.collection(Post.COLLECTION).document(post.getId()).set(post.toJson())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        successListener.onComplete(null);
                        return;
                    }
                    Log.e("SetPost", "Could not set Post");
                    failListener.onComplete(null);
                });
    }

    public void getPost(String postId, Listener<Map<String, Object>> successListener, Listener<Void> failedListener) {
        db.collection(Post.COLLECTION).document(postId).get()
                .addOnSuccessListener(t -> {
                    if (t.exists()) {
                        successListener.onComplete(t.getData());
                        return;
                    }
                    failedListener.onComplete(null);
                })
                .addOnFailureListener(t -> failedListener.onComplete(null));
    }

    public void getAllPostsSince(Long since, Listener<List<Post>> callback) {
        db.collection(Post.COLLECTION)
                .whereGreaterThanOrEqualTo(Post.LAST_UPDATED, new Timestamp(since + 1, 0))
                .orderBy(Post.LAST_UPDATED)
                .limit(1000)
                .get()
                .addOnCompleteListener(task -> {
                    List<Post> list = new LinkedList<>();
                    if (task.isSuccessful()) {
                        QuerySnapshot jsonsList = task.getResult();
                        for (DocumentSnapshot json : jsonsList) {
                            Post post = Post.fromJson(json.getData());
                            list.add(post);
                        }
                    }
                    callback.onComplete(list);
                });
    }

    public void checkForNicknameExistence(String nickname, String userId, Listener<Boolean> onCheckSuccess, Listener<Exception> onCheckFailed) {
        db.collection(User.COLLECTION).whereEqualTo(User.NICKNAME, nickname).whereNotEqualTo(User.ID, userId).limit(1).get()
                .addOnSuccessListener(t-> onCheckSuccess.onComplete(!t.isEmpty()))
                .addOnFailureListener(onCheckFailed::onComplete);
    }
}
