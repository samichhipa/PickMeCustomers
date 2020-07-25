package com.example.pickmecustomers.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.media.Rating;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.pickmecustomers.Model.Common;
import com.example.pickmecustomers.Model.DriversRating;
import com.example.pickmecustomers.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RatingActivity extends AppCompatActivity {

    MaterialRatingBar ratingBar;
    MaterialEditText txt_comment;
    Button btnComment;

    DatabaseReference reference;
    double rating_val = 0.0;

    ImageView cancelBtn;


    String driverTd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);


        reference = FirebaseDatabase.getInstance().getReference().child("DriversRating");

        ratingBar = findViewById(R.id.ratingbar);
        txt_comment = findViewById(R.id.txt_comment);
        btnComment = findViewById(R.id.commentBtn);
        cancelBtn = findViewById(R.id.cancelbtn);


        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        ratingBar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {

                rating_val = rating;

            }
        });

        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                submitRating(Common.driver_id);


            }
        });


    }

    private void submitRating(String driverId) {

        if (TextUtils.isEmpty(txt_comment.getText().toString())) {

            txt_comment.setError("Write a Comment");

            return;
        } else {

            final ProgressDialog dialog = new ProgressDialog(RatingActivity.this);
            dialog.setMessage("Please Wait...");
            dialog.show();

            DriversRating rating = new DriversRating();
            rating.setRating(String.valueOf(rating_val));
            rating.setCustomer_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
            rating.setComment(txt_comment.getText().toString());

            reference.child(driverId).push().setValue(rating).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        dialog.dismiss();
                        finish();


                    } else {
                        dialog.dismiss();
                        Toast.makeText(RatingActivity.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    dialog.dismiss();
                    Toast.makeText(RatingActivity.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();


                }
            });

        }

    }
}
