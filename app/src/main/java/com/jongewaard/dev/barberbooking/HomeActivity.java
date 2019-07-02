package com.jongewaard.dev.barberbooking;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;
import com.jongewaard.dev.barberbooking.Common.Common;
import com.jongewaard.dev.barberbooking.Fragments.HomeFragment;
import com.jongewaard.dev.barberbooking.Fragments.ShoppingFragment;
import com.jongewaard.dev.barberbooking.Model.User;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;

public class HomeActivity extends AppCompatActivity {


    @BindView(R.id.bottom_navigation)
    BottomNavigationView mBottomNavigationView;

    BottomSheetDialog mBottomSheetDialog;

    CollectionReference userRef;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(HomeActivity.this);

        //Init
        userRef = FirebaseFirestore.getInstance().collection("User");
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        /* Check intent, if is login = true, enable full access
        * if is login = false, just let user around shopping to view
        * */
        if(getIntent() != null){

            boolean isLogin = getIntent().getBooleanExtra(Common.IS_LOGIN, false);

            if(isLogin){

                dialog.show();

                //Check if user exits
                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(final Account account) {
                        if(account != null){

                            DocumentReference currentuser = userRef.document(account.getPhoneNumber().toString());
                            currentuser.get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                           if(task.isSuccessful()){

                                               DocumentSnapshot userSnapShot = task.getResult();

                                               if(!userSnapShot.exists()) {
                                                   showUpdateDialog(account.getPhoneNumber().toString());
                                               }
                                               else{

                                                   //If User already is available in our System!!!!
                                                   Common.currentUser = userSnapShot.toObject(User.class);
                                                   mBottomNavigationView.setSelectedItemId(R.id.action_home);


                                               }


                                               if(dialog.isShowing())
                                                   dialog.dismiss();


                                           }
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {
                        Toast.makeText(HomeActivity.this, ""+accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        //View
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            Fragment fragment = null;

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                if(menuItem.getItemId() == R.id.action_home){
                    fragment = new HomeFragment();
                }else if(menuItem.getItemId() == R.id.action_shopping)
                    fragment = new ShoppingFragment();

                return loadFragment(fragment);

            }
        });

    }

    private boolean loadFragment(Fragment fragment) {
        if(fragment != null){

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void showUpdateDialog(final String phoneNumber) {
         //Init dialog
        mBottomSheetDialog = new BottomSheetDialog(this);
        mBottomSheetDialog.setCanceledOnTouchOutside(false);
        mBottomSheetDialog.setCancelable(false);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_update_information, null);

        Button btn_update =(Button)sheetView.findViewById(R.id.btn_update);
        final TextInputEditText edt_name = (TextInputEditText)sheetView.findViewById(R.id.edt_name);
        final TextInputEditText edt_address = (TextInputEditText)sheetView.findViewById(R.id.edt_address);

        btn_update.setOnClickListener(new View.OnClickListener() {



            @Override
            public void onClick(View view) {

                if(!dialog.isShowing())
                    dialog.show();

                final User user = new User(edt_name.getText().toString(),
                        edt_address.getText().toString(),
                        phoneNumber);

                userRef.document(phoneNumber)
                        .set(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mBottomSheetDialog.dismiss();
                                if(dialog.isShowing())
                                    dialog.dismiss();

                                Common.currentUser = user;
                                mBottomNavigationView.setSelectedItemId(R.id.action_home); 

                                Toast.makeText(HomeActivity.this,
                                        "Thank you",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mBottomSheetDialog.dismiss();
                        Toast.makeText(HomeActivity.this,
                                ""+e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();

    }
}
