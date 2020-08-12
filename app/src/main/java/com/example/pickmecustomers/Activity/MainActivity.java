package com.example.pickmecustomers.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.pickmecustomers.Model.Customers;
import com.example.pickmecustomers.Model.empvalidation;
import com.example.pickmecustomers.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    Button btnLogin, btnRegister;
    MaterialEditText txt_email, txt_password, txt_phone, txt_name;

    DatabaseReference reference;
    FirebaseAuth auth;

android.app.AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        alertDialog=new SpotsDialog.Builder().setContext(this).setCancelable(false).setTheme(R.style.Custom).build();
        alertDialog.setMessage("Loading...");

        reference = FirebaseDatabase.getInstance().getReference().child("Customers");
        auth = FirebaseAuth.getInstance();

        init();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.register_layout, null);

                txt_email = v.findViewById(R.id.regiser_email);
                txt_password = v.findViewById(R.id.register_password);
                txt_name = v.findViewById(R.id.register_txt_name);
                txt_phone = v.findViewById(R.id.register_phone);
                Button btnSignUp = v.findViewById(R.id.register_registerBtn);
                Button btncancel = v.findViewById(R.id.registerCancelBtn);

                builder.setView(v);

                final AlertDialog alertDialog = builder.create();


                btnSignUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String email, pass, name, phone;

                        email = txt_email.getText().toString();
                        pass = txt_password.getText().toString();
                        name = txt_name.getText().toString();
                        phone = txt_phone.getText().toString();


                        if (TextUtils.isEmpty(name)) {

                            txt_name.setError("Enter Name");
                            return;

                        } else if (TextUtils.isEmpty(email)) {

                            txt_email.setError("Enter Email");
                            return;
                        } else if (!empvalidation.validateEMAIL(email)) {

                            txt_email.setError("Invalid Format");
                            return;
                        } else if (TextUtils.isEmpty(pass)) {
                            txt_password.setError("Enter Password");
                            return;

                        } else if (pass.length() <= 6) {
                            txt_password.setError("Password should be greater than 6 characters");
                            return;

                        } else if (TextUtils.isEmpty(phone)) {

                            txt_phone.setError("Enter Phone");
                            return;

                        }  else {

                            Customers customers = new Customers();
                            customers.setName(name);
                            customers.setCustomer_email(email);
                            customers.setPassword(pass);
                            customers.setCustomer_phone(phone);

                            SignUp(customers);

                            alertDialog.dismiss();
                        }


                    }
                });

                btncancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.login_layout, null);

                txt_email = v.findViewById(R.id.login_email);
                txt_password = v.findViewById(R.id.login_password);
                Button btnLogin = v.findViewById(R.id.login_SignIn);
                Button btncancel = v.findViewById(R.id.login_cancel);

                builder.setView(v);

                final AlertDialog alertDialog = builder.create();

                btnLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String email, pass;

                        email = txt_email.getText().toString();
                        pass = txt_password.getText().toString();


                        if (TextUtils.isEmpty(email)) {

                            txt_email.setError("Enter Email");
                            return;
                        } else if (!empvalidation.validateEMAIL(email)) {

                            txt_email.setError("Invalid Format");
                            return;
                        } else if (TextUtils.isEmpty(pass)) {
                            txt_password.setError("Enter Password");
                            return;

                        } else if (pass.length() <= 6) {
                            txt_password.setError("Password should be greater than 6 characters");
                            return;

                        } else {

                            login(email, pass);
                            alertDialog.dismiss();

                        }


                    }
                });

                btncancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();


            }
        });


    }



    private void init() {

        btnLogin = findViewById(R.id.loginBtn);
        btnRegister = findViewById(R.id.registerBtn);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser=auth.getCurrentUser();
        if (firebaseUser!=null){

            Intent intent=new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();

        }else{
        Log.d("LOG","NEW");
    }

    }


    private void SignUp(final Customers customers) {


        alertDialog.show();
        auth.createUserWithEmailAndPassword(customers.getCustomer_email(), customers.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {


                    customers.setId(auth.getCurrentUser().getUid());
                    reference.child(customers.getId()).setValue(customers);
                    txt_name.setText("");
                    txt_email.setText("");
                    txt_password.setText("");
                    txt_phone.setText("");
                    Toast.makeText(MainActivity.this, "Registered Successfull..", Toast.LENGTH_SHORT).show();

                    alertDialog.dismiss();

                } else {

                    Toast.makeText(MainActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();


                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {


                alertDialog.dismiss();
                Toast.makeText(MainActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void login(String email, String pass) {


        alertDialog.show();

        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    alertDialog.dismiss();

                    Toast.makeText(MainActivity.this, "Login Successfull..", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                alertDialog.dismiss();
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }
}
