package com.besteeth.logica;

import android.os.AsyncTask;

import com.besteeth.modelo.Servicio;

import org.apache.xmlrpc.XmlRpcException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Clase LogicaServicio
 *
 * Toda la implementación en cuanto a recuperación y modificación de datos desde/hacia
 * Odoo mediante XMLRPC,
 * en lo que se refiere a los servicios
 *
 * @author IVAN
 * @version 1.0
 */
public class LogicaServicio {

    //CallBacks hacia CoreViewModel
    public interface LogicaServicioCallBack {
        void recuperarServiciosCallBack(List<Servicio> listadoServicios);
    }

    //Recupera todos los servicios disponibles
    public static void recuperarServicios(LogicaCliente.LogicaClienteCallBack callback) {
        AsyncTask_recuperarServicios asyncTask_recuperarServicios = new AsyncTask_recuperarServicios();
        Object[] params = new Object[1];
        params[0] = callback;
        asyncTask_recuperarServicios.execute(params);
    }

    public static class AsyncTask_recuperarServicios extends AsyncTask<Object, Void, List<Servicio>> {

        private LogicaServicioCallBack callback;

        @Override
        protected List<Servicio> doInBackground(Object... objects) {
            callback = (LogicaServicioCallBack) objects[0];
            List<Servicio> listadoServicios = new ArrayList<>();
            try {
                List<Object> serviciosOdoo = asList((Object[]) ConexionOdoo.getInstance().getModels().execute("execute_kw", asList(
                        ConexionOdoo.getDb(), ConexionOdoo.getInstance().getUid(), ConexionOdoo.getPassword(),
                        "besteeth.servicio", "search_read",
                        asList(asList()),
                        new HashMap() {{
                            put("fields", asList("id", "nombre", "precio"));
                            put("order", "precio asc");
                        }}
                )));

                Servicio servicioPlaceHolder = new Servicio();
                servicioPlaceHolder.setNombre("Seleccione un servicio");
                servicioPlaceHolder.setServicioPlaceHolder(true);
                listadoServicios.add(servicioPlaceHolder);

                if (serviciosOdoo.size() != 0) {
                    Servicio servicio;
                    HashMap servicioHash;
                    for (int i = 0; i < serviciosOdoo.size(); i++) {
                        servicio = new Servicio();
                        servicioHash = (HashMap) serviciosOdoo.get(i);
                        servicio.setId((Integer) servicioHash.get("id"));
                        servicio.setNombre((String) servicioHash.get("nombre"));
                        servicio.setPrecio((double) servicioHash.get("precio"));
                        listadoServicios.add(servicio);
                    }
                } else {
                    cancel(true);
                }
            } catch (XmlRpcException e) {
                e.printStackTrace();
                cancel(true);
            }
            return listadoServicios;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            List<Servicio> listadoServicios = new ArrayList<>();
            Servicio servicioPlaceHolder = new Servicio();
            servicioPlaceHolder.setNombre("Seleccione un servicio");
            servicioPlaceHolder.setServicioPlaceHolder(true);
            listadoServicios.add(servicioPlaceHolder);
            callback.recuperarServiciosCallBack(listadoServicios);
        }

        @Override
        protected void onPostExecute(List<Servicio> servicios) {
            super.onPostExecute(servicios);
            callback.recuperarServiciosCallBack(servicios);
        }
    }
}
