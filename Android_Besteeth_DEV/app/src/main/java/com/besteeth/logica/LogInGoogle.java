package com.besteeth.logica;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

/**
 * Clase LogInGoogle
 *
 * Encargada de lo relacionado al logueo mediante una cuenta
 * de Google
 *
 * @author IVAN
 * @version 1.0
 */
public class LogInGoogle {

    public static final int GOOGLE_SIGN_IN = 1;

    private static LogInGoogle logInGoogle;
    private GoogleSignInClient mGoogleSignInClient;

    public static LogInGoogle getInstance(){
        if (logInGoogle == null){
            logInGoogle = new LogInGoogle();
        }
        return logInGoogle;
    }

    //Constructor privado
    private LogInGoogle(){
        mGoogleSignInClient = null;
    }

    //Inicia la sesión
    public void signInGoogle(Activity logInActivity){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(logInActivity.getApplicationContext(), gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        logInActivity.startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    //Cierra la sesión
    public void signOutGoogle(Activity otherActivity){
        //mGoogleSignInCliente es != null si la sesión ya estaba iniciada de anteriores accesos
        if(mGoogleSignInClient != null){
            mGoogleSignInClient.signOut();
            mGoogleSignInClient.revokeAccess();
        }else{
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(otherActivity.getApplicationContext(), gso);
            mGoogleSignInClient.signOut();
            mGoogleSignInClient.revokeAccess();
        }
    }
}
