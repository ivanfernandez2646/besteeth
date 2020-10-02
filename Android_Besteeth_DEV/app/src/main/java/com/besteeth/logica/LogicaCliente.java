package com.besteeth.logica;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.besteeth.modelo.Cliente;

import org.apache.xmlrpc.XmlRpcException;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Clase LogicaCliente
 * <p>
 * Toda la implementación en cuanto a recuperación y modificación de datos desde/hacia
 * Odoo mediante XMLRPC,
 * en lo que se refiere al cliente logueado
 *
 * @author IVAN
 * @version 1.0
 */
public class LogicaCliente {

    //String distinguir servicio EXISTE_CLIENTE_ACTIOn
    public static final String EXISTE_CLIENTE_ACTION = "com.besteeth.modelo.ExisteClienteAction";

    //CallBacks hacia CoreViewModel
    public interface LogicaClienteCallBack {
        void recuperarInfoClienteCallBack(Cliente cliente);

        void guardarDatosPersonalesCallBack(boolean res);

        void existeClienteOdooCallBack(boolean res);
    }

    //Este método llama utiliza el IntentService declarado más abajo para comprobar si existe el cliente
    //en Odoo tras pasar el logueo con Google. Si existe procede al inicio de sesión
    public static void existeClienteEnOdoo_LoginActivity(Context context, String email) {
        Intent intentServicioExisteCliente = new Intent(context, IntentService_existeClienteEnOdoo.class);
        intentServicioExisteCliente.putExtra("email", email);
        context.startService(intentServicioExisteCliente);
    }

    public static class IntentService_existeClienteEnOdoo extends IntentService {

        private Intent intentBroadcast;

        public IntentService_existeClienteEnOdoo() {
            super("IntentService_existeClienteEnOdoo");
        }

        @Override
        public void onCreate() {
            super.onCreate();
            intentBroadcast = new Intent();
            intentBroadcast.setAction(EXISTE_CLIENTE_ACTION);
        }

        @Override
        protected void onHandleIntent(@Nullable Intent intent) {
            String email = null;

            if (intent != null) {
                email = intent.getStringExtra("email");
            }

            try {
                int cantidadClientesOdoo = (Integer) ConexionOdoo.getInstance().getModels().execute("execute_kw", asList(
                        ConexionOdoo.getDb(), ConexionOdoo.getInstance().getUid(), ConexionOdoo.getPassword(),
                        "besteeth.cliente", "search_count",
                        asList(asList(
                                asList("email", "=", email)))
                ));

                if (cantidadClientesOdoo == 1) {
                    intentBroadcast.putExtra("res", true);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentBroadcast);
                } else {
                    intentBroadcast.putExtra("res", false);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentBroadcast);
                }
            } catch (XmlRpcException e) {
                e.printStackTrace();
                intentBroadcast.putExtra("res", false);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentBroadcast);
            }
        }
    }

    //Este método similar al de arriba, se apoya en el asynctask inferior para  comprobar
    //si existe el cliente en Odoo, pero para temas de bajas y ediciones, mientras que el superior
    //es solo para el login y lo realiza mediante un IntentService
    public static void existeClienteEnOdoo(LogicaClienteCallBack callback, String email) {
        AsyncTask_existeClienteEnOdoo asyncTask_existeClienteEnOdoo = new AsyncTask_existeClienteEnOdoo();
        Object[] params = new Object[2];
        params[0] = callback;
        params[1] = email;
        asyncTask_existeClienteEnOdoo.execute(params);
    }

    public static class AsyncTask_existeClienteEnOdoo extends AsyncTask<Object, Void, Boolean> {

        private LogicaClienteCallBack callback;

        @Override
        protected Boolean doInBackground(Object... objects) {
            callback = (LogicaClienteCallBack) objects[0];
            String email = (String) objects[1];
            try {
                int cantidadClientesOdoo = (Integer) ConexionOdoo.getInstance().getModels().execute("execute_kw", asList(
                        ConexionOdoo.getDb(), ConexionOdoo.getInstance().getUid(), ConexionOdoo.getPassword(),
                        "besteeth.cliente", "search_count",
                        asList(asList(
                                asList("email", "=", email)))
                ));

                if (cantidadClientesOdoo != 0) {
                    cancel(true);
                }
            } catch (XmlRpcException e) {
                e.printStackTrace();
                cancel(true);
            }
            return false;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            callback.existeClienteOdooCallBack(true);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            callback.existeClienteOdooCallBack(aBoolean);
        }
    }

    //Recupera la información del cliente logueado
    public static void recuperarInfoCliente(LogicaClienteCallBack callback, String email) {
        AsyncTask_recuperarInfoClienteOdoo asyncTask_recuperarInfoClienteOdoo = new AsyncTask_recuperarInfoClienteOdoo();
        Object[] params = new Object[2];
        params[0] = callback;
        params[1] = email;
        asyncTask_recuperarInfoClienteOdoo.execute(params);
    }

    public static class AsyncTask_recuperarInfoClienteOdoo extends AsyncTask<Object, Void, Cliente> {

        private LogicaClienteCallBack callback;

        @Override
        protected Cliente doInBackground(Object... objects) {
            callback = (LogicaClienteCallBack) objects[0];
            String email = (String) objects[1];
            Cliente cliente = new Cliente();
            try {
                List<Object> clientesOdoo = asList((Object[]) ConexionOdoo.getInstance().getModels().execute("execute_kw", asList(
                        ConexionOdoo.getDb(), ConexionOdoo.getInstance().getUid(), ConexionOdoo.getPassword(),
                        "besteeth.cliente", "search_read",
                        asList(asList(
                                asList("email", "=", email))),
                        new HashMap() {{
                            put("fields", asList("id", "dni", "nombre", "direccion", "telefono", "email", "gastos_totales", "foto"));
                        }}
                )));

                if (clientesOdoo.get(0) != null && clientesOdoo.size() == 1) {
                    HashMap clienteHash = (HashMap) clientesOdoo.get(0);
                    cliente.setId((Integer) clienteHash.get("id"));
                    cliente.setDni((String) clienteHash.get("dni"));
                    cliente.setNombre((String) clienteHash.get("nombre"));
                    cliente.setDireccion((String) clienteHash.get("direccion"));
                    cliente.setTelefono((String) clienteHash.get("telefono"));
                    cliente.setEmail((String) clienteHash.get("email"));
                    cliente.setGastosTotales((double) clienteHash.get("gastos_totales"));

                    try {
                        String base64foto = (String) clienteHash.get("foto");
                        if (!base64foto.equalsIgnoreCase("")) {
                            byte[] bitmapdata = Base64.decode(base64foto, Base64.DEFAULT);
                            cliente.setFoto(BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length));
                        }
                    } catch (Exception e) {
                        cliente.setFoto(null);
                    }
                } else {
                    cancel(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                cancel(true);
            }
            return cliente;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            callback.recuperarInfoClienteCallBack(null);
        }

        @Override
        protected void onPostExecute(Cliente cliente) {
            super.onPostExecute(cliente);
            callback.recuperarInfoClienteCallBack(cliente);
        }
    }

    //Para guardar las posibles modificaciones de los datos personales del cliente
    public static void guardarDatosPersonales(LogicaClienteCallBack callback, Cliente cliente, String emailAntiguo) {
        AsyncTask_guardarDatosPersonales asyncTask_guardarDatosPersonales = new AsyncTask_guardarDatosPersonales();
        Object[] params = new Object[3];
        params[0] = callback;
        params[1] = cliente;
        params[2] = emailAntiguo;
        asyncTask_guardarDatosPersonales.execute(params);
    }

    public static class AsyncTask_guardarDatosPersonales extends AsyncTask<Object, Void, Boolean> {

        private LogicaClienteCallBack callback;

        @Override
        protected Boolean doInBackground(Object... objects) {
            callback = (LogicaClienteCallBack) objects[0];
            Cliente cliente = (Cliente) objects[1];
            String emailAntiguo = (String) objects[2];

            if (cliente != null) {
                try {
                    HashMap idClienteOdoo = (HashMap) asList((Object[]) ConexionOdoo.getInstance().getModels().execute("execute_kw", asList(
                            ConexionOdoo.getDb(), ConexionOdoo.getInstance().getUid(), ConexionOdoo.getPassword(),
                            "besteeth.cliente", "search_read",
                            asList(asList(
                                    asList("email", "=", emailAntiguo))),
                            new HashMap() {{
                                put("fields", asList("id"));
                            }}
                    ))).get(0);

                    int id = (int) idClienteOdoo.get("id");
                    String fotoCliente = null;

                    if (cliente.getFoto() != null) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        cliente.getFoto().compress(Bitmap.CompressFormat.JPEG, 80, stream);
                        byte[] byteArray = stream.toByteArray();
                        fotoCliente = Base64.encodeToString(byteArray, Base64.DEFAULT);
                    }

                    String finalFotoCliente = fotoCliente;
                    ConexionOdoo.getInstance().getModels().execute("execute_kw", asList(
                            ConexionOdoo.getDb(), ConexionOdoo.getInstance().getUid(), ConexionOdoo.getPassword(),
                            "besteeth.cliente", "write",
                            asList(
                                    asList(id),
                                    new HashMap() {{
                                        put("nombre", cliente.getNombre());
                                        put("direccion", cliente.getDireccion());
                                        put("telefono", cliente.getTelefono());
                                        put("email", cliente.getEmail());
                                        put("foto", finalFotoCliente == null ? false : finalFotoCliente);
                                    }}
                            )
                    ));
                } catch (XmlRpcException e) {
                    e.printStackTrace();
                    cancel(true);
                }
            } else {
                cancel(true);
            }
            return true;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            callback.guardarDatosPersonalesCallBack(false);
        }

        @Override
        protected void onPostExecute(Boolean res) {
            super.onPostExecute(res);
            callback.guardarDatosPersonalesCallBack(res);
        }
    }
}
