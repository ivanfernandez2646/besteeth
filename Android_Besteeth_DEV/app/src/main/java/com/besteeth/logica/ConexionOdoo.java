package com.besteeth.logica;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Clase ConexionOdoo
 *
 * Implementación necesaria para conectarnos con Odoo
 *
 * @author IVAN
 * @version 1.0
 */
public class ConexionOdoo {

    //OJO!! Cabe destacar que Odoo necesita de un usuario para realizar la conexión con BBDD PostgreSQL.
    //Para ello siempre estableceremos la conexión con Odoo mediante un usuario común registrado en res.partner de Odoo (RemoteClient) con los permisos de un cliente
    //Además, el usuario tendrá limitado el acceso siendo controlado este en código.

    //BroadcastReceivers
    public static final String LOGIN_REMOTE_CLIENT_ACTION = "com.besteeth.modelo.LoginRemoteClientAction";

    //Atributos estáticos referentes a la conexión
    private static ConexionOdoo conexionOdoo;
    //private static final String url = "http://10.0.2.2:8069";
    private static final String url = "http://192.168.18.13:8069";
    private static final String db = "besteeth";
    private static final String username = "remoteclient@besteeth.com";
    private static final String password = "123456";

    private Object uid;
    private final XmlRpcClient models;

    //Constructor Privado Singleton
    private ConexionOdoo() throws MalformedURLException {
        uid = null;
        models = new XmlRpcClient() {{
            setConfig(new XmlRpcClientConfigImpl() {{
                setServerURL(new URL(String.format("%s/xmlrpc/2/object", url)));
                setConnectionTimeout(5000);
            }});
        }};
    }

    //Getters Atributos Estáticos
    public static String getUrl() {
        return url;
    }

    public static String getDb() {
        return db;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    //Getters Atributos Instancia
    public Object getUid() {
        return uid;
    }

    public XmlRpcClient getModels() {
        return models;
    }

    public static ConexionOdoo getInstance() {
        if (conexionOdoo == null) {
            try {
                conexionOdoo = new ConexionOdoo();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return conexionOdoo;
    }

    //Método llamado para realizar el logIn a nivel de Odoo con el usuario RemoteClient
    public void logInRemoteClient(Context context) {
        Intent intentServicioLoginRemoteClient = new Intent(context, IntentService_logInRemoteClient.class);
        context.startService(intentServicioLoginRemoteClient);
    }

    public static class IntentService_logInRemoteClient extends IntentService {

        private Intent intentBroadcast;

        public IntentService_logInRemoteClient() {
            super("IntentService_logInRemoteClient");
        }

        @Override
        public void onCreate() {
            super.onCreate();
            intentBroadcast = new Intent();
            intentBroadcast.setAction(LOGIN_REMOTE_CLIENT_ACTION);
        }

        @Override
        protected void onHandleIntent(@Nullable Intent intent) {
            if (intent != null) {
                XmlRpcClient client = new XmlRpcClient();
                XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
                config.setConnectionTimeout(5000);
                config.setEnabledForExtensions(true);
                try {
                    config.setServerURL(new URL(url + "/xmlrpc/2/common"));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    intentBroadcast.putExtra("res", false);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentBroadcast);
                }
                client.setConfig(config);
                Object[] params = new Object[]{db, username, password};
                try {
                    conexionOdoo.uid = client.execute("login", params);
                    intentBroadcast.putExtra("res", true);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentBroadcast);
                } catch (XmlRpcException e) {
                    e.printStackTrace();
                    intentBroadcast.putExtra("res", false);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentBroadcast);
                }
            }
        }
    }
}
