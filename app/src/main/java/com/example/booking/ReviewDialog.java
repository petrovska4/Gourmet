package com.example.booking;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import androidx.annotation.NonNull;

public class ReviewDialog extends Dialog {

    public interface ReviewDialogListener {
        void onSaveReview(String comment, float rating);
    }

    public ReviewDialog(@NonNull Context context, ReviewDialogListener listener) {
        super(context);
        setContentView(R.layout.activity_review_dialog);

        EditText editTextComment = findViewById(R.id.editTextComment);
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(v -> {
            String comment = editTextComment.getText().toString();
            float rating = ratingBar.getRating();
            listener.onSaveReview(comment, rating);
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }
}
