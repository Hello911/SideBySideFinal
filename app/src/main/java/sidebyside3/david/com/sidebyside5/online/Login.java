package sidebyside3.david.com.sidebyside5.online;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import sidebyside3.david.com.sidebyside5.R;

public class Login extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{

    TextView signup;
    Switch rememberMe;
    Boolean rememberMeState;//if rememberMe is checked
    EditText email;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        pref=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
        editor=pref.edit();
        init();
    }

    private void init(){
        signup=(TextView)findViewById(R.id.signup);
        signup.setOnClickListener(this);

        rememberMeState=pref.getBoolean("rememberMeState",false);
        rememberMe=(Switch)findViewById(R.id.rememberLogin);
        rememberMe.setChecked(rememberMeState);
        rememberMe.setOnCheckedChangeListener(this);

        email=(EditText)findViewById(R.id.input_email);
        email.setText(pref.getString("email",""));
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(rememberMeState){
                    editor.putString("email",charSequence.toString());
                    editor.apply();
                }else{
                    editor.putString("email","");
                    editor.apply();
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.signup:
                Intent openRegister= new Intent(this,Register.class);
                startActivity(openRegister);
        }
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
        rememberMeState=isChecked;
        editor.putBoolean("rememberMeState",isChecked);
        editor.apply();
        if(isChecked){
            editor.putString("email",email.getText().toString());
            editor.apply();
        }else{
            editor.putString("email","");
            editor.apply();
        }
    }
}
