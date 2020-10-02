package com.besteeth.vista;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.besteeth.R;
import com.besteeth.logica.ConexionOdoo;
import com.besteeth.logica.LogInGoogle;
import com.besteeth.logica.LogicaCliente;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

/**
 * Clase LoginActivity
 *
 * Primera actividad lanzada nada más arrancar la app.
 * Encargada del logueo hacia la aplicación, pasando por Google
 * y Odoo.
 *
 * @author IVAN
 * @version 1.0
 */
public class LoginActivity extends AppCompatActivity {

    //Atributos
    private GoogleSignInButton btLogInGoogle;
    private ProgressBar pbButton;
    private SharedPreferences sharedPref;
    private String email;
    private MiReceptor miReceptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Inits
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //FindsViewByIds
        pbButton = findViewById(R.id.pbButton);
        pbButton.setVisibility(View.INVISIBLE);
        btLogInGoogle = findViewById(R.id.btLogInGoogle);
        btLogInGoogle.setTextColor(Color.BLACK);
        btLogInGoogle.setTextSize(14);

        //Listeners
        btLogInGoogle.setOnClickListener(btsLogIn_OnClickListener);

        miReceptor = new MiReceptor();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConexionOdoo.LOGIN_REMOTE_CLIENT_ACTION);
        intentFilter.addAction(LogicaCliente.EXISTE_CLIENTE_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(miReceptor, intentFilter);
    }

    //Comprobamos si hay login ya o no desde el fichero SharedPreferences
    @Override
    protected void onStart() {
        super.onStart();
        if (sharedPref.getBoolean("hasLogin", false)) {
            pbButton.setVisibility(View.VISIBLE);
            email = sharedPref.getString("email", "");
            ConexionOdoo.getInstance().logInRemoteClient(getApplicationContext());
        }
    }

    //Listener botón LoginGoogle
    private View.OnClickListener btsLogIn_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == btLogInGoogle) {
                LogInGoogle.getInstance().signInGoogle(LoginActivity.this);
            }
        }
    };

    //OnActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LogInGoogle.GOOGLE_SIGN_IN:
                pbButton.setVisibility(View.VISIBLE);
                try {
                    GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                    if (account != null) {
                        email = account.getEmail();
                        ConexionOdoo.getInstance().logInRemoteClient(getApplicationContext());
                    }
                } catch (ApiException e) {
                    //Código de error 12501, es simplemente que cancelas el login, con lo cual no es un error como tal
                    pbButton.setVisibility(View.INVISIBLE);
                    if (e.getStatusCode() != 12501) {
                        Toast.makeText(this, R.string.msg_ApiExceptionGoogle, Toast.LENGTH_LONG).show();
                    }
                }
        }
    }

    //Receptor Broadcasts
    private class MiReceptor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case ConexionOdoo.LOGIN_REMOTE_CLIENT_ACTION:
                        if (intent.getBooleanExtra("res", false)) {
                            LogicaCliente.existeClienteEnOdoo_LoginActivity(getApplicationContext(), email);
                        } else {
                            pbButton.setVisibility(View.INVISIBLE);
                            LogInGoogle.getInstance().signOutGoogle(LoginActivity.this);
                            Toast.makeText(context, R.string.msg_ErrorConexionOdoo, Toast.LENGTH_LONG).show();
                        }
                        break;
                    case LogicaCliente.EXISTE_CLIENTE_ACTION:
                        if (intent.getBooleanExtra("res", false)) {
                            pbButton.setVisibility(View.INVISIBLE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("email", email);
                            editor.putBoolean("hasLogin", true);
                            editor.apply();
                            Intent intentOtherActivity = new Intent(context, CoreActivity.class);
                            intentOtherActivity.putExtra("email", email);
                            startActivity(intentOtherActivity);
                        } else {
                            pbButton.setVisibility(View.INVISIBLE);
                            LogInGoogle.getInstance().signOutGoogle(LoginActivity.this);
                            Toast.makeText(context, R.string.msg_ErrorNoClienteOdoo, Toast.LENGTH_LONG).show();
                        }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (miReceptor != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(miReceptor);
        }
    }
}
