package com.glv.note_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

public class LoginActivity extends AppCompatActivity {
    private EditText login, pass;
    private FirebaseAuth mAuth;
    private Button logIN, sigIN, reload_check_inetrnet;
    private TextView InternetNot;




    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = findViewById(R.id.login);
        pass = findViewById(R.id.pass);
        logIN = findViewById(R.id.logIN);
        sigIN = findViewById(R.id.sigIN);
        reload_check_inetrnet = findViewById(R.id.reload_check_inetrnet);
        InternetNot = findViewById(R.id.InternetNot);
        mAuth = FirebaseAuth.getInstance();

        reload_check_inetrnet.setOnClickListener(v -> {
            if (Internet_check.InternetIsConn(this)) {
                onStart();
            } else {
                Toast.makeText(this, "Нет интернет-соединения", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void onClickLoginUp(View view){
        if(!TextUtils.isEmpty(login.getText().toString()) && !TextUtils.isEmpty(pass.getText().toString())) {
            mAuth.createUserWithEmailAndPassword(login.getText().toString(), pass.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Toast.makeText(getApplicationContext(), "Подтверждение отправлено на вашу электронную почту.", Toast.LENGTH_SHORT).show();
                    if(task.isSuccessful()){
                        EmailVer();
                    }
                }
            });


        }
    }

    public void onClickSigIn(View view){
        if(!TextUtils.isEmpty(login.getText().toString()) && !TextUtils.isEmpty(pass.getText().toString())) {
            mAuth.signInWithEmailAndPassword(login.getText().toString(), pass.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        FirebaseUser cUser = mAuth.getCurrentUser();
                        Intent i =new Intent(LoginActivity.this, MainActivity.class);
                        i.putExtra("EmailDB", cUser.getEmail());
                        i.putExtra("iduser", cUser.getUid());
                        startActivity(i);
                        Toast.makeText(getApplicationContext(), "Вы вошли успешно", Toast.LENGTH_SHORT).show();
                    }
                    else{

                        Toast.makeText(getApplicationContext(), "Неправильный пароль или имя пользователя", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }

    }
    @Override
    protected void onStart(){
        super.onStart();
        if (!Internet_check.InternetIsConn(this))
        {
            login.setVisibility(View.INVISIBLE);
            pass.setVisibility(View.INVISIBLE);
            logIN.setVisibility(View.INVISIBLE);
            sigIN.setVisibility(View.INVISIBLE);
            reload_check_inetrnet.setVisibility(View.VISIBLE);
            InternetNot.setVisibility(View.VISIBLE);
        }
        else
        {
            FirebaseUser cUser = mAuth.getCurrentUser();
            if (cUser!=null){
                Intent i =new Intent(LoginActivity.this, MainActivity.class);
                i.putExtra("EmailDB", cUser.getEmail());
                i.putExtra("iduser", cUser.getUid());
                startActivity(i);
            }
            else
            {
                login.setVisibility(View.VISIBLE);
                pass.setVisibility(View.VISIBLE);
                logIN.setVisibility(View.VISIBLE);
                sigIN.setVisibility(View.VISIBLE);
                reload_check_inetrnet.setVisibility(View.INVISIBLE);
                InternetNot.setVisibility(View.INVISIBLE);
            }
        }
    }



    private void EmailVer(){
        FirebaseUser user = mAuth.getCurrentUser();

        assert user!=null;

        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(), "Подтверждение отправлено на вашу электронную почту.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Подтверждение провалено", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}

