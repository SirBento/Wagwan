package com.example.wagwan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {
    private  TextView NeedNewAccountLink, ForgetPasswordLink ;
    private  Button loginButton, phoneLoginButton;
    private  EditText userPassword,userEmail;
    private  FirebaseAuth mAuth;
    private  ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        InitializeFields();

        ForgetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent( LoginActivity.this,ForgotPassword.class));
            }
        });

        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendUserToRegisterActivity();
            }


        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });

        phoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent( LoginActivity.this,PhoneLoginActivity.class));
            }


        });

    }

    private void AllowUserToLogin()
    {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();

        if(!validatePassword() |  !validateEmail() ){

            YoYo.with(Techniques.Shake).duration(1000).repeat(2).playOn(userEmail);
            YoYo.with(Techniques.Shake).duration(1000).repeat(2).playOn(userPassword);
            return;
        }else {

            loadingBar.setTitle("Sign In");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){

                        //checking if the account is verified , if not the user has to first verify the account before logging in
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        if(user.isEmailVerified()){

                            SendUserToMainActivity();
                            Toast.makeText(LoginActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();

                        }else{

                            user.sendEmailVerification();
                            loadingBar.dismiss();
                            Toast.makeText(LoginActivity.this,
                                    "Account not verified. Please check your email to verify your account the Login",
                                    Toast.LENGTH_LONG).show();
                        }

                    }else {
                        loadingBar.dismiss();
                        Toast.makeText(LoginActivity.this, "LogIn Failed: Please Check Your Internet Connection!!! ", Toast.LENGTH_LONG).show();

                    }
                }
            });

        }

    }

    private void InitializeFields()
    {
         loginButton =  findViewById(R.id.login_button);
         phoneLoginButton = findViewById(R.id.phone_login_button);
         userEmail = findViewById(R.id.login_email);
         userPassword = findViewById(R.id.login_password);
         NeedNewAccountLink = findViewById(R.id.need_new_account_link);
         ForgetPasswordLink = findViewById(R.id.forget_password_link);
         loadingBar = new ProgressDialog(this);
    }

    private void SendUserToRegisterActivity()
    {
        startActivity(new Intent( LoginActivity.this,RegisterActivity.class));
        finish();
    }

    private void SendUserToMainActivity()
    {
        Toast.makeText(LoginActivity.this, "Logged In Successfully", Toast.LENGTH_SHORT).show();
        Intent MainIntent = new Intent(LoginActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }


    private boolean validateEmail(){
        String e_mail = userEmail.getText().toString().trim();

        //checking if the email text box is filled or not
        if(e_mail.isEmpty()){
            userEmail.setError("Email is required");
            userEmail.requestFocus();
            return false;

            //checking if the email entered is valid
        }else if(!Patterns.EMAIL_ADDRESS.matcher(e_mail).matches()){
            userEmail.setError("Please enter a email ");
            userEmail.requestFocus();
            return false;

        } else {
            userEmail.setError(null);
            return true;

        }

    }

    private boolean validatePassword(){

        String pass = userPassword.getText().toString().trim();

        // checking if the password text box is filled or not

        if(pass.isEmpty()){
            userPassword.setError("Password is required");
            userPassword.requestFocus();
            return false;

            // checking the password length due to firebase restrictions
        } else if (pass.length()< 6) {
            userPassword.setError("Your password is too short");
            userPassword.requestFocus();
            return false;

        } else {
            userPassword.setError(null);
            return true;

        }


    }
}
