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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;


public class RegisterActivity extends AppCompatActivity
{
    private TextView AlreadyHaveAccountLink;
    private Button CreateAccountButton;
    private EditText userEmail,userPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog  loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        InitializeFields();

        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SendUserToLoginActivity();

            }

        });


        CreateAccountButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
               CreateNewAccount();

            }

        });

    }

    private void CreateNewAccount()
    {

        final String email = userEmail.getText().toString();
        final String password = userPassword.getText().toString();

        if(!validatePassword() |  !validateEmail() ){
            // If the functions continue to return false repeat validation until true
            return;

        }
        else{

            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait, While we are creating a new account for you...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

           mAuth.createUserWithEmailAndPassword(email,password)

              .addOnCompleteListener(new OnCompleteListener<AuthResult>()
              {
                  @Override
                  public void onComplete(@NonNull Task<AuthResult> task)
                  {
                      
                     if(task.isSuccessful())
                     {
                         HashMap<String, String> user = new HashMap<>();
                         user.put("Email: " ,email);
                         user.put("Password: ",password);
                         user.put("UID:",FirebaseAuth.getInstance().getCurrentUser().getUid());
                         FirebaseDatabase.getInstance().getReference("Users")
                                 .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                 .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                     @Override
                                     public void onComplete(@NonNull Task<Void> task) {

                                         if(task.isSuccessful()){

                                         FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                         user.sendEmailVerification();
                                         SendUserToLoginActivity();
                                         Toast.makeText(RegisterActivity.this, "Account Created Successfully. Please check your email to verify your email them login", Toast.LENGTH_SHORT).show();
                                         loadingBar.dismiss();
                                         finish();
                                     }else{

                                             Toast.makeText(RegisterActivity.this, "Error: Please Check your internet connection", Toast.LENGTH_SHORT).show();
                                             loadingBar.dismiss();
                                         }
                                     }
                                 });
                     }else {

                         Toast.makeText(RegisterActivity.this, "Error: Please Check your internet connection", Toast.LENGTH_SHORT).show();
                         loadingBar.dismiss();
                     }
                  }



              });
        }
    }


    private void InitializeFields()
    {
        CreateAccountButton = findViewById(R.id.register_button);
        userEmail = findViewById(R.id.register_email);
        userPassword =  findViewById(R.id.register_password);
        AlreadyHaveAccountLink = findViewById(R.id.already_have_account_link);
        loadingBar = new ProgressDialog(this);
    }

        private void SendUserToLoginActivity()
        {
            startActivity(new Intent( RegisterActivity.this,LoginActivity.class));
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
