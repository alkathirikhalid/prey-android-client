/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;
import com.prey.util.KeyboardStatusDetector;
import com.prey.util.KeyboardVisibilityListener;

import org.json.JSONException;
import org.json.JSONObject;

public class SignUpActivity extends AppCompatActivity implements  GoogleApiClient.OnConnectionFailedListener{
    private static final int RC_SIGN_IN = 9001;
    private static final int ERROR = 1;
    private String error = null;
    private String email = null;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;
    private TextView mStatusTextView;
    private static final String GRAPH_PATH = "me/permissions";

    public void onResume() {
        PreyLogger.i("onResume of SignUpActivity");
        super.onResume();

    }

    @Override
    public void onPause() {
        PreyLogger.i("onPause of SignUpActivity");
        super.onPause();
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.setContentView(R.layout.signup);
        PreyLogger.i("onCreate of SignUpActivity");

        callbackManager = CallbackManager.Factory.create();


        loginButton = (LoginButton) findViewById(R.id.button_social_facebook);
        loginButton.setReadPermissions("email", "public_profile", "user_friends");


        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        // [END build_client]

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code



                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                PreyLogger.i( response.toString());
                                try {
                                    PreyLogger.i("__object:"+object.toString());
                                    // Application code
                                    String email = object.getString("email");

                                    String name=object.getString("name");
                                    PreyLogger.i("email:" + email);

                                    Intent intent = new Intent(getApplicationContext(), SignUpActivity2.class);
                                    intent.putExtra("signup_email", email);
                                    intent.putExtra("signup_name", name);

                                    startActivity(intent);
                                    finish();

                                    GraphRequest.Callback callback = new GraphRequest.Callback() {
                                        @Override
                                        public void onCompleted(GraphResponse response) {
                                            try {

                                                    LoginManager.getInstance().logOut();

                                            } catch (Exception ex) { /* no op */ }
                                        }
                                    };
                                    GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(),
                                            GRAPH_PATH, new Bundle(), HttpMethod.DELETE, callback);
                                    request.executeAsync();
                                }catch(Exception e){

                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();


            }

            @Override
            public void onCancel() {
                // App code

            }

            @Override
            public void onError(FacebookException exception) {
                // App code

                PreyLogger.i("onError:"+exception.getMessage());
            }
        });


        final EditText nameText=((EditText)findViewById(R.id.editTextName));
        final EditText emailText=((EditText)findViewById(R.id.editTextEmailAddress));
        //final EditText passwordText=((EditText)findViewById(R.id.editTextPassword));
        Button buttonSignup = (Button) findViewById(R.id.buttonSignup);

        final TextView linkSignup = (TextView) findViewById(R.id.linkSignup);

        Button button_social_facebook=(Button) findViewById(R.id.button_social_facebook);
        SignInButton signInButton=(SignInButton) findViewById(R.id.button_social_google);


        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);

               /* Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                // [START_EXCLUDE]
                                updateUI(false);
                                // [END_EXCLUDE]
                            }
                        });


                Intent intent = new Intent(getApplicationContext(), SignUpActivity2.class);
                startActivity(intent);
                finish();  */
            }
        });


        Typeface magdacleanmonoRegular = Typeface.createFromAsset(getAssets(), "fonts/MagdaClean/magdacleanmono-regular.ttf");
        Typeface titilliumWebBold = Typeface.createFromAsset(getAssets(), "fonts/Titillium_Web/TitilliumWeb-Bold.ttf");

        TextView textViewInit1=(TextView)findViewById(R.id.textViewInit1);
        TextView textViewInit2=(TextView)findViewById(R.id.textViewInit2);
        textViewInit1.setTypeface(magdacleanmonoRegular);
        textViewInit2.setTypeface(titilliumWebBold);



        linkSignup.setTypeface(titilliumWebBold);
        buttonSignup.setTypeface(titilliumWebBold);

        nameText.setTypeface(magdacleanmonoRegular);
        emailText.setTypeface(magdacleanmonoRegular);
        //passwordText.setTypeface(magdacleanmonoRegular);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);



        final int halfHeight=metrics.heightPixels/3;
        KeyboardStatusDetector keyboard = new KeyboardStatusDetector();

        keyboard.registerActivity(this); // or register to an activity
        keyboard.setVisibilityListener(new KeyboardVisibilityListener() {

            @Override
            public void onVisibilityChanged(boolean keyboardVisible) {
                try {


                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) linkSignup.getLayoutParams();
                    if (keyboardVisible) {
                        PreyLogger.d("key on");

                        params.setMargins(20, 0, 20, halfHeight);
                    } else {
                        PreyLogger.d("key off");

                        params.setMargins(20, 0, 20, 20);
                    }
                    linkSignup.setLayoutParams(params);
                } catch (Exception e) {
                    PreyLogger.i("error:" + e.getMessage());
                }
            }
        });


        buttonSignup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String name = nameText.getText().toString();
                email = emailText.getText().toString();

                Intent intent = new Intent(getApplicationContext(), SignUpActivity2.class);
                intent.putExtra("signup_email", email);
                intent.putExtra("signup_name", name);
                startActivity(intent);
                finish();


               /* String password = passwordText.getText().toString();
                Context ctx = getApplicationContext();
                if (email == null || email.equals("") || password == null || password.equals("")) {
                    Toast.makeText(ctx, R.string.error_all_fields_are_required, Toast.LENGTH_LONG).show();
                } else {
                    if (email.length() < 6 || email.length() > 100) {
                        Toast.makeText(ctx, ctx.getString(R.string.error_mail_out_of_range, "6", "100"), Toast.LENGTH_LONG).show();
                    } else {
                        if (password.length() < 6 || password.length() > 32) {
                            Toast.makeText(ctx, ctx.getString(R.string.error_password_out_of_range, "6", "32"), Toast.LENGTH_LONG).show();
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                                new CreateAccount().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, name, email, password);
                            else
                                new CreateAccount().execute(name, email, password);
                        }
                    }
                }*/

            }
        });


        linkSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(intent);
                finish();

            }
        });
    }



    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), InitActivity.class);
        startActivity(intent);
        finish();
    }



    // [START signIn]
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    // [START signOut]
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END revokeAccess]

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog pass = null;
        switch (id) {

            case ERROR:
                return new AlertDialog.Builder(SignUpActivity.this).setIcon(R.drawable.error).setTitle(R.string.error_title).setMessage(error)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setCancelable(false).create();
        }
        return pass;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        PreyLogger.i("onConnectionFailed:" + connectionResult);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }


    private void updateUI(boolean signedIn) {

    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_social_google:
                signIn();
                break;

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }else{
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

    }



    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        PreyLogger.i( "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            PreyLogger.i( acct.getDisplayName());
            PreyLogger.i( acct.getEmail());
            updateUI(true);

            Intent intent = new Intent(getApplicationContext(), SignUpActivity2.class);
            intent.putExtra("signup_email", acct.getEmail());
            intent.putExtra("signup_name", acct.getDisplayName());
            startActivity(intent);
            finish();

            Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            // [START_EXCLUDE]
                            updateUI(false);
                            // [END_EXCLUDE]
                        }
                    });

        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }
    // [END handleSignInResult]
}
