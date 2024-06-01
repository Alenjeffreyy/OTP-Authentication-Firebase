package com.example.fire;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class VerifyActivity extends AppCompatActivity {
    EditText edtOTP;
    Button verifyOTPBtn;
    String verificationId;
    FirebaseAuth firebaseAuth;
    String number;
    private ProgressDialog pDialog;
    private Dialog sendingOtpDialog;
    private final Context context = this;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        firebaseAuth = FirebaseAuth.getInstance();

        edtOTP = findViewById(R.id.et_otp);
        verifyOTPBtn = findViewById(R.id.verify);

        number = getIntent().getStringExtra("phone");

        verifyOTPBtn.setOnClickListener(v -> {
            if (TextUtils.isEmpty(edtOTP.getText().toString())) {
                Toast.makeText(VerifyActivity.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            } else {
                verifyCode(edtOTP.getText().toString());
            }
        });

        sendVerificationCode(number);
        initSendingOtpDialog();
        sendingOtpDialog.show();
    }

    private void initSendingOtpDialog() {
        sendingOtpDialog = new Dialog(context);
        sendingOtpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        sendingOtpDialog.setContentView(R.layout.m_send_otp_wait);
        Objects.requireNonNull(sendingOtpDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        sendingOtpDialog.setCancelable(false);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
            firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            pDialog.dismiss();
                            Toast.makeText(context, "success", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(context, HomeActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            Toast.makeText(VerifyActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            pDialog.dismiss();
                        }
                    });
        }

        private void sendVerificationCode (String number){
            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(firebaseAuth)
                            .setPhoneNumber(number)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(this)
                            .setCallbacks(mCallBack)
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        }


        private PhoneAuthProvider.OnVerificationStateChangedCallbacks
                mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                if (s != null){
                    Log.e("otp_", "success");
                    sendingOtpDialog.dismiss();
                    verificationId = s;
                }

            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                final String code = phoneAuthCredential.getSmsCode();
                if (code != null) {
                    edtOTP.setText(code);
                    verifyCode(code);
                }
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(context, "onVerificationFailed " + e.toString(), Toast.LENGTH_SHORT).show();
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(context, "Invalid Request " + e, Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(context, "The SMS quota for the project has been exceeded " + e, Toast.LENGTH_SHORT).show();
                }
            }
        };


        private void verifyCode (String code){
            displayLoader();
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInWithCredential(credential);
        }

    private void displayLoader() {
            pDialog = new ProgressDialog(context, R.style.AppCompatAlertDialogStyle);
            pDialog.setMessage(getResources().getString(R.string.checking_otp_please_wait));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
    }
}
