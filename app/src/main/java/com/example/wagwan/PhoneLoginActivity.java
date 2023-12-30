package com.example.wagwan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button SendVerificationCodeButton ,VerifyButton;
    private EditText InputPhoneNumber,InputVerificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private  String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth=  FirebaseAuth.getInstance();
        SendVerificationCodeButton = findViewById(R.id.send_verification_code_button);
        VerifyButton = findViewById(R.id.verify_button);
        InputPhoneNumber =  findViewById(R.id.phone_number_input);
        InputVerificationCode =  findViewById(R.id.verification_code_input);
        loadingBar = new ProgressDialog(PhoneLoginActivity.this);

        SendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                String phoneNumber = InputPhoneNumber.getText().toString();

                if(!validatePhoneNumber()){
                    // If the functions continue to return false repeat validation until true
                    YoYo.with(Techniques.Shake).duration(1000).repeat(2).playOn(InputPhoneNumber);
                    return;

                }
                else
                {
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please wait, were are authenticating your phone... ");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,
                            60,
                            TimeUnit.SECONDS,
                            PhoneLoginActivity.this,
                            callbacks
                    );
                }


            }
        });


        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                String VerificationCode = InputVerificationCode.getText().toString();

                if (TextUtils.isEmpty(VerificationCode))
                {
                    YoYo.with(Techniques.Shake).duration(1000).repeat(2).playOn(InputVerificationCode);
                    InputPhoneNumber.setError("Please Enter The Code First!!!");
                    InputPhoneNumber.requestFocus();

                }else{
                    loadingBar.setTitle("Code Verification");
                    loadingBar.setMessage("Please wait, were are verifying your phone... ");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,VerificationCode);
                    signWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential)
            {
                signWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e)
            {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number, Please Enter A Valid Number", Toast.LENGTH_SHORT).show();

                SendVerificationCodeButton.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);
                VerifyButton.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);

            }

            public  void onCodeSent(String verificationId,
                                    PhoneAuthProvider.ForceResendingToken token){

              mVerificationId = verificationId;
              mResendToken = token;
              loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code Sent Successfully, Please  Check The Code And Verify... ", Toast.LENGTH_SHORT).show();

                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                VerifyButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);
            }
        };

    }

 public void signWithPhoneAuthCredential(PhoneAuthCredential credential)
 {
     mAuth.signInWithCredential(credential)
             .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(@NonNull Task<AuthResult> task)
                 {
                     if (task.isSuccessful())
                     {
                         loadingBar.dismiss();
                         Toast.makeText(PhoneLoginActivity.this, "Logged In Successfully", Toast.LENGTH_SHORT).show();
                         SendUserToMainActivity();
                     }
                     else
                     {
                        String  message  = task.getException().toString();
                         Toast.makeText(PhoneLoginActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();

                     }

                 }
             });

 }

    private void SendUserToMainActivity()
    {
        Intent MainIntent = new Intent(PhoneLoginActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }

    private boolean validatePhoneNumber(){

        // Get entered phone number, trim to remove extra white space
        String paynum = InputPhoneNumber.getText().toString().trim();

        // checking if the phone number field is empty or not
        if (paynum.isEmpty()) {

            InputPhoneNumber.setError("A phone number is required");
            InputPhoneNumber.requestFocus();
            return false;
            //checking the length and validity of the phone number
        }else if (paynum.length() > 13 || paynum.length() < 13) {

            InputPhoneNumber.setError("Enter a valid number with your Country Code");
            InputPhoneNumber.requestFocus();
            return false ;

        } else {
            InputPhoneNumber.setError(null);
            return true;

        }

    }

}
