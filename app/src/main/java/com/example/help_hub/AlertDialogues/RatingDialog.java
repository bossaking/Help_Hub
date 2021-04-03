package com.example.help_hub.AlertDialogues;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.help_hub.OtherClasses.Opinion;
import com.example.help_hub.OtherClasses.User;
import com.example.help_hub.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RatingDialog extends DialogFragment {


    private RatingBar ratingBar;
    private Button ratingButton;
    private EditText commentEditText;

    private String userId, otherUserId;

    private ratingChangedListener ratingChangedListener;

    public RatingDialog(String otherUserId, String userId, Context context) {
        this.otherUserId = otherUserId;
        this.userId = userId;
        this.ratingChangedListener = (ratingChangedListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.rating_dialog_layout, null);

        ratingBar = view.findViewById(R.id.rating_bar);
        ratingBar.setRating(5);

        ratingButton = view.findViewById(R.id.rating_button);
        ratingButton.setOnClickListener(v -> {
            sendRating();
        });

        commentEditText = view.findViewById(R.id.comment_edit_text);

        return view;
    }

    private void sendRating() {

        if (ratingBar.getRating() < 1) {
            Toast.makeText(getContext(), getString(R.string.minimum_rating), Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(otherUserId).get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);

            if (Objects.requireNonNull(user).getAllRating() == 0) {
                user.setAllRating(ratingBar.getRating());
                user.setAllOpinionsCount(1);
                user.setUserRating(ratingBar.getRating());
            } else {
                user.setAllRating(user.getAllRating() + ratingBar.getRating());
                user.setAllOpinionsCount(user.getAllOpinionsCount() + 1);
                user.setUserRating(user.getAllRating() / user.getAllOpinionsCount());
            }

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("AllRating", user.getAllRating());
            userMap.put("UserRating", user.getUserRating());
            userMap.put("AllOpinionsCount", user.getAllOpinionsCount());

            FirebaseFirestore.getInstance().collection("users").document(otherUserId).update(userMap).addOnSuccessListener(unused -> {
                Toast.makeText(getContext(), getString(R.string.success), Toast.LENGTH_SHORT).show();


                if (!commentEditText.getText().toString().isEmpty()) {

                    Opinion opinion = new Opinion();

                    opinion.setOpinionText(commentEditText.getText().toString());
                    opinion.setRating(ratingBar.getRating());

                    FirebaseFirestore.getInstance().collection("users").document(userId).get().addOnSuccessListener(documentSnapshot1 -> {
                        opinion.setUserNickname(documentSnapshot1.getString("Name"));
                        FirebaseFirestore.getInstance().collection("users").document(otherUserId).collection("opinions")
                                .add(opinion).addOnSuccessListener(documentReference -> {
                            ratingChangedListener.ratingChanged();
                            this.dismiss();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        });
                    });


                } else {
                    ratingChangedListener.ratingChanged();
                    this.dismiss();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            });


        });

    }

    @Override
    public void onResume() {
        super.onResume();

        Window window = Objects.requireNonNull(getDialog()).getWindow();
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
    }

    public interface ratingChangedListener {
        void ratingChanged();
    }

}
