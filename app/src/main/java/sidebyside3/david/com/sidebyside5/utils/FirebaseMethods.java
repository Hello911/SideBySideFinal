package sidebyside3.david.com.sidebyside5.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Gongwei (David) Chen on 3/30/2019.
 */

public class FirebaseMethods {
    private static final String TAG="FirebaseMethods";
    private FirebaseAuth mAuth;
    private Context mContext;
    private String userID;
    private DatabaseReference mDatabase;

    public FirebaseMethods(Context context){
        mAuth=FirebaseAuth.getInstance();
        mContext=context;
        if(mAuth.getCurrentUser() !=null){
            userID=mAuth.getCurrentUser().getUid();
        }
        mDatabase=FirebaseDatabase.getInstance().getReference();
    }

    /**
     * register a new user in Firebase. Send verification email if successful.
     *
     * @param email emailed input by user at sign in page
     * @param username
     * @param password
     */
    public void registerNewEmail(String email, final String username, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(mContext, "Registering successful. Check email.",
                                    Toast.LENGTH_SHORT).show();
                            verifyEmail();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(mContext, "Registering failed: "+task.getException().toString(),
                                    Toast.LENGTH_LONG).show();

                        }

                        // ...
                    }
                });
    }

    /**
     * Send a verification email with link, will Toast with error description if email cannot be sent
     */
    public void verifyEmail(){
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                //good,email sent
                            }else{
                                Toast.makeText(mContext, "Couldn't sent verification email: "+task.getException().toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    /**
     * Add username entered at registering to database
     * @param username
     */
    public void addUser(String username){
        Log.d("Register.JAVA","adding user: "+username+" UserID: "+userID);
        mDatabase.push().setValue("Hello");

    }

    public boolean ifUsernameExist(String username, DataSnapshot datasnapshot){
        User user=new User();
        for(DataSnapshot ds: datasnapshot.child(userID).getChildren()){
            user.setUsername(ds.getValue(User.class).getUsername());
            if(StringManipulator.expandUsername(user.getUsername()).equals(username)){
                return true;
            }
        }
        return false;
    }

}
