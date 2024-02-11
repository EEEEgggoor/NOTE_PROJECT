package com.glv.note_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
    private TextView your_name_text;
    private EditText login, pass;
    private Button logIN, main_act_btn, singOut;
    private FirebaseAuth mAuth;
    private Button sigIN;
    private String requesrQode;
    private int Logsost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logsost = 0;
        setContentView(R.layout.activity_login);
        login = findViewById(R.id.login);
        pass = findViewById(R.id.pass);
        main_act_btn = findViewById(R.id.main_act_btn);
        singOut = findViewById(R.id.back_in_out);
        your_name_text = findViewById(R.id.yout_name_text);
        sigIN = findViewById(R.id.sigIN);
        mAuth = FirebaseAuth.getInstance();
        logIN = findViewById(R.id.logIN);
    }
    public void onClickLoginUp(View view){
        if(!TextUtils.isEmpty(login.getText().toString()) && !TextUtils.isEmpty(pass.getText().toString())) {
            mAuth.createUserWithEmailAndPassword(login.getText().toString(), pass.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        FirebaseUser cUser = mAuth.getCurrentUser();
                        if (cUser!=null){
                            Signed();
                            String userName = "Вы вошли как:" + cUser.getEmail();
                            your_name_text.setText(userName);
                        }

                        Signed();
                        Toast.makeText(getApplicationContext(), "Sing", Toast.LENGTH_SHORT).show();

                    }
                    else{
                        notSigned();
                        Toast.makeText(getApplicationContext(), "not Sing", Toast.LENGTH_SHORT).show();

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
                        Signed();

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
        FirebaseUser cUser = mAuth.getCurrentUser();
        if (cUser!=null){
            Signed();
            String userName = "Вы вошли как:" + cUser.getEmail();
            your_name_text.setText(userName);
        }
        else{
            main_act_btn.setVisibility(View.GONE);
            your_name_text.setVisibility(View.GONE);
            singOut.setVisibility(View.GONE);

            login.setVisibility(View.VISIBLE);
            pass.setVisibility(View.VISIBLE);
            logIN.setVisibility(View.VISIBLE);
            sigIN.setVisibility(View.VISIBLE);




        }
    }
    public void onClickSignOut(View view){
        {
            FirebaseAuth.getInstance().signOut();
            notSigned();
        }
    }private void Signed(){
        main_act_btn.setVisibility(View.VISIBLE);
        your_name_text.setVisibility(View.VISIBLE);
        login.setVisibility(View.GONE);
        pass.setVisibility(View.GONE);
        logIN.setVisibility(View.GONE);
        sigIN.setVisibility(View.GONE);
        singOut.setVisibility(View.VISIBLE);
    }
    private void notSigned(){
        main_act_btn.setVisibility(View.GONE);
        your_name_text.setVisibility(View.GONE);
        singOut.setVisibility(View.GONE);
        login.setVisibility(View.VISIBLE);
        pass.setVisibility(View.VISIBLE);
        logIN.setVisibility(View.VISIBLE);
        sigIN.setVisibility(View.VISIBLE);

    }
    public void onClickStart(View view){
        FirebaseUser cUser = mAuth.getCurrentUser();


        Intent i =new Intent(LoginActivity.this, MainActivity.class);
        i.putExtra("EmailDB", cUser.getEmail());
        i.putExtra("iduser", cUser.getUid());
        Logsost = 1;
        i.putExtra("Logsost1", Logsost);

        startActivity(i);

    }

}

