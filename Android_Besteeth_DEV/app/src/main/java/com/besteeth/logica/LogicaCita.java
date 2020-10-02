package com.besteeth.logica;

import android.os.AsyncTask;

import com.besteeth.modelo.Cita;
import com.besteeth.modelo.Cliente;
import com.besteeth.modelo.Configuracion;
import com.besteeth.modelo.FiltroCitas;
import com.besteeth.modelo.Pago;
import com.besteeth.modelo.Servicio;
import com.wdullaer.materialdatetimepicker.time.Timepoint;

import org.apache.xmlrpc.XmlRpcException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;

/**
 * Clase LogicaCita
 * <p>
 * Toda la implementación en cuanto a recuperación y modificación de datos desde/hacia
 * Odoo mediante XMLRPC,
 * en lo que se refiere a las citas del cliente logueado
 *
 * @author IVAN
 * @version 1.0
 */
public class LogicaCita {

    //CallBacks hacia CoreViewModel
    public interface LogicaCitaCallBack {
        void recuperarProximasCitasCallBack(List<Cita> citas);

        void recuperarCitas(List<Cita> citas);

        void altaCitaCallBack(boolean res);

        void bajaCitaCallBack(boolean res);

        void aplazarCita(boolean res);

        void recuperarHorasOcupadasDiaCallBack(Timepoint[] horasOcupadas, Configuracion configuracion);
    }

    //Recupera las citas próximas a la fecha y hora actual
    public static void recuperarProximasCitas(LogicaCitaCallBack callback, String email) {
        AsyncTask_recuperarProximasCitas asyncTask_recuperarProximasCitas = new AsyncTask_recuperarProximasCitas();
        Object[] params = new Object[2];
        params[0] = callback;
        params[1] = email;
        asyncTask_recuperarProximasCitas.execute(params);
    }

    public static class AsyncTask_recuperarProximasCitas extends AsyncTask<Object, Void, List<Cita>> {

        private LogicaCitaCallBack callback;

        @Override
        protected List<Cita> doInBackground(Object... objects) {
            callback = (LogicaCitaCallBack) objects[0];
            String email = (String) objects[1];
            List<Cita> listadoCitas = new ArrayList<>();

            DateTime fechaActual = DateTime.now(DateTimeZone.UTC);
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(DateTimeZone.UTC);
            String fechaActualS = dateTimeFormatter.print(fechaActual);

            try {
                List<Object> citasOdoo = asList((Object[]) ConexionOdoo.getInstance().getModels().execute("execute_kw", asList(
                        ConexionOdoo.getDb(), ConexionOdoo.getInstance().getUid(), ConexionOdoo.getPassword(),
                        "besteeth.cita", "search_read",
                        asList(asList(
                                asList("cliente_id.email", "=", email),
                                asList("fecha_hora", ">=", fechaActualS),
                                asList("atendida", "=", false))),
                        new HashMap() {{
                            put("fields", asList("id", "fecha_hora", "doctor_id", "cliente_id", "servicio_id", "pago_iniciado"));
                            put("order", "fecha_hora asc");
                        }}
                )));

                for (int i = 0; i < citasOdoo.size(); i++) {
                    Cita cita = new Cita();
                    HashMap citaHash = (HashMap) citasOdoo.get(i);

                    cita.setId((Integer) citaHash.get("id"));
                    DateTime dateTime = dateTimeFormatter.parseDateTime((String) Objects.requireNonNull(citaHash.get("fecha_hora"))).withZone(DateTimeZone.forID("Europe/Madrid"));
                    cita.setFechaHora(dateTime);

                    if (!(citaHash.get("doctor_id") instanceof Boolean)) {
                        cita.setNameDoctor((String) ((Object[]) Objects.requireNonNull(citaHash.get("doctor_id")))[1]);
                    }

                    Cliente cliente = new Cliente();
                    cliente.setId((Integer) ((Object[]) citaHash.get("cliente_id"))[0]);
                    cita.setCliente(cliente);
                    cita.setPagoIniciado((Boolean) citaHash.get("pago_iniciado"));

                    Servicio servicio = new Servicio();
                    servicio.setNombre((String) ((Object[]) Objects.requireNonNull(citaHash.get("servicio_id")))[1]);

                    HashMap servicioOdoo = (HashMap) (asList((Object[]) ConexionOdoo.getInstance().getModels().execute("execute_kw", asList(
                            ConexionOdoo.getDb(), ConexionOdoo.getInstance().getUid(), ConexionOdoo.getPassword(),
                            "besteeth.servicio", "search_read",
                            asList(asList(
                                    asList("nombre", "=", servicio.getNombre()))),
                            new HashMap() {{
                                put("fields", asList("precio"));
                            }}
                    )))).get(0);

                    servicio.setPrecio((Double) servicioOdoo.get("precio"));
                    cita.setServicio(servicio);
                    listadoCitas.add(cita);
                }
            } catch (XmlRpcException e) {
                e.printStackTrace();
                cancel(true);
            }
            return listadoCitas;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            callback.recuperarProximasCitasCallBack(null);
        }

        @Override
        protected void onPostExecute(List<Cita> listadoCitas) {
            super.onPostExecute(listadoCitas);
            callback.recuperarProximasCitasCallBack(listadoCitas);
        }
    }

    //Recupera las citas a partir del filtro pasado
    public static void recuperarCitas(LogicaCitaCallBack callback, String email, FiltroCitas filtroCitas) {
        AsyncTask_recuperarCitas asyncTask_recuperarCitas = new AsyncTask_recuperarCitas();
        Object[] params = new Object[3];
        params[0] = callback;
        params[1] = email;
        params[2] = filtroCitas;
        asyncTask_recuperarCitas.execute(params);
    }

    public static class AsyncTask_recuperarCitas extends AsyncTask<Object, Void, List<Cita>> {

        private LogicaCitaCallBack callback;

        @Override
        protected List<Cita> doInBackground(Object... objects) {
            callback = (LogicaCitaCallBack) objects[0];
            String email = (String) objects[1];
            FiltroCitas filtroCitas = (FiltroCitas) objects[2];
            List<Cita> listadoCitas = new ArrayList<>();


            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(DateTimeZone.UTC);
            String fechaHoraFiltro = dateTimeFormatter.print(filtroCitas.getFecha());


            try {
                List<Object> citasOdoo = asList((Object[]) ConexionOdoo.getInstance().getModels().execute("execute_kw", asList(
                        ConexionOdoo.getDb(), ConexionOdoo.getInstance().getUid(), ConexionOdoo.getPassword(),
                        "besteeth.cita", "search_read",
                        asList(asList(
                                asList("cliente_id.email", "=", email),
                                asList("fecha_hora", ">=", fechaHoraFiltro),
                                filtroCitas.getServicio() != null ? asList("servicio_id.id", "=", filtroCitas.getServicio().getId()) : asList("servicio_id.id", ">=", 0),
                                asList("cerrada", "=", filtroCitas.isCerrada()),
                                filtroCitas.getModalidadPago() == Pago.MODALIDAD_PAGO.SIN_FILTRO ? asList("pago_iniciado", "<=", true) : asList("pago_iniciado", "=", true)
                        )),
                        new HashMap() {{
                            put("fields", asList("id", "fecha_hora", "cerrada", "doctor_id", "cliente_id", "servicio_id", "pago_iniciado"));
                            put("order", "fecha_hora asc");
                        }}
                )));

                for (int i = 0; i < citasOdoo.size(); i++) {
                    Cita cita = new Cita();
                    HashMap citaHash = (HashMap) citasOdoo.get(i);

                    cita.setId((Integer) citaHash.get("id"));
                    DateTime dateTimeCita = dateTimeFormatter.parseDateTime((String) Objects.requireNonNull(citaHash.get("fecha_hora"))).withZone(DateTimeZone.forID("Europe/Madrid"));
                    cita.setFechaHora(dateTimeCita);

                    if (!(citaHash.get("doctor_id") instanceof Boolean)) {
                        cita.setNameDoctor((String) ((Object[]) Objects.requireNonNull(citaHash.get("doctor_id")))[1]);
                    }

                    Cliente cliente = new Cliente();
                    cliente.setId((Integer) ((Object[]) citaHash.get("cliente_id"))[0]);
                    cita.setCliente(cliente);
                    cita.setPagoIniciado((Boolean) citaHash.get("pago_iniciado"));
                    cita.setCerrada((Boolean) citaHash.get("cerrada"));

                    Servicio servicio = new Servicio();
                    servicio.setId((Integer) ((Object[]) Objects.requireNonNull(citaHash.get("servicio_id")))[0]);
                    servicio.setNombre((String) ((Object[]) Objects.requireNonNull(citaHash.get("servicio_id")))[1]);

                    HashMap servicioOdoo = (HashMap) (asList((Object[]) ConexionOdoo.getInstance().getModels().execute("execute_kw", asList(
                            ConexionOdoo.getDb(), ConexionOdoo.getInstance().getUid(), ConexionOdoo.getPassword(),
                            "besteeth.servicio", "search_read",
                            asList(asList(
                                    asList("id", "=", servicio.getId()))),
                            new HashMap() {{
                                put("fields", asList("precio"));
                            }}
                    )))).get(0);

                    List<Object> pagosOdoo;
                    String modalidadPagoOdoo = null;

                    switch (filtroCitas.getModalidadPago()) {
                        case PAGO_UNICO:
                        case CUOTAS:
                            modalidadPagoOdoo = Normalizer.normalize(filtroCitas.getModalidadPago().toString().replace(" ", "_"), Normalizer.Form.NFD)
                                    .replaceAll("[^\\p{ASCII}]", "");
                            break;
                        case CUALQUIERA:
                        case SIN_FILTRO:
                            modalidadPagoOdoo = "";
                            break;
                    }

                    pagosOdoo = asList((Object[]) ConexionOdoo.getInstance().getModels().execute("execute_kw", asList(
                            ConexionOdoo.getDb(), ConexionOdoo.getInstance().getUid(), ConexionOdoo.getPassword(),
                            "besteeth.pago", "search_read",
                            asList(asList(
                                    asList("cliente_id.email", "=", email),
                                    asList("cita_id", "=", cita.getId()),
                                    asList("modalidad_pago", "ilike", modalidadPagoOdoo)
                            )),
                            new HashMap() {{
                                put("fields", asList("fecha_hora", "modalidad_pago", "pago_cuota", "cantidad_cuotas", "cuotas_pagadas", "cuotas_restantes", "pago_total"));
                            }}
                    )));


                    boolean anadirCita = false;
                    if (filtroCitas.getModalidadPago() == Pago.MODALIDAD_PAGO.SIN_FILTRO) {
                        if (pagosOdoo.size() >= 1) {
                            servicio.setPrecio((Double) ((HashMap) pagosOdoo.get(0)).get("pago_total"));
                        } else {
                            servicio.setPrecio((Double) servicioOdoo.get("precio"));
                        }
                        cita.setServicio(servicio);
                        anadirCita = true;
                    } else {
                        if (pagosOdoo.size() >= 1) {
                            if (((String) Objects.requireNonNull(((HashMap) pagosOdoo.get(0)).get("modalidad_pago"))).equalsIgnoreCase(modalidadPagoOdoo)) {
                                servicio.setPrecio((Double) ((HashMap) pagosOdoo.get(0)).get("pago_total"));
                            }
                            cita.setServicio(servicio);
                            anadirCita = true;
                        }
                    }

                    for (int j = 0; j < pagosOdoo.size(); j++) {
                        Pago pago = new Pago();
                        HashMap pagoHash = (HashMap) pagosOdoo.get(j);

                        DateTime dateTimePago = dateTimeFormatter.parseDateTime((String) Objects.requireNonNull(pagoHash.get("fecha_hora"))).withZone(DateTimeZone.forID("Europe/Madrid"));
                        pago.setFechaHora(dateTimePago);
                        pago.setModalidadPago(Pago.MODALIDAD_PAGO.valueOf(((String) pagoHash.get("modalidad_pago")).toUpperCase()));
                        pago.setPagoCuota((Double) pagoHash.get("pago_cuota"));
                        pago.setCantidadCuotas((Integer) pagoHash.get("cantidad_cuotas"));
                        pago.setCuotasPagadas((Integer) pagoHash.get("cuotas_pagadas"));
                        pago.setCuotasRestantes((Integer) pagoHash.get("cuotas_restantes"));
                        pago.setPagoTotal((Double) pagoHash.get("pago_total"));
                        pago.setServicio(cita.getServicio());
                        cita.getPagos().add(pago);
                    }

                    if (anadirCita) {
                        listadoCitas.add(cita);
                    }
                }
            } catch (XmlRpcException e) {
                e.printStackTrace();
                cancel(true);
            }
            return listadoCitas;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            callback.recuperarCitas(null);
        }

        @Override
        protected void onPostExecute(List<Cita> listadoCitas) {
            super.onPostExecute(listadoCitas);
            callback.recuperarCitas(listadoCitas);
        }
    }

    //Da de alta una cita
    public static void altaCita(LogicaCitaCallBack callback, String email, Cita cita) {
        AsyncTask_altaCita asyncTask_altaCita = new AsyncTask_altaCita();
        Object[] params = new Object[3];
        params[0] = callback;
        params[1] = email;
        params[2] = cita;
        asyncTask_altaCita.execute(params);
    }

    public static class AsyncTask_altaCita extends AsyncTask<Object, Void, Boolean> {

        private LogicaCitaCallBack callback;

        @Override
        protected Boolean doInBackground(Object... objects) {
            callback = (LogicaCitaCallBack) objects[0];
            String email = (String) objects[1];
            Cita cita = (Cita) objects[2];
            int idServicio = cita.getServicio().getId();

            try {
                //Primero buscamos el id del cliente, el id del servicio ya lo tenemos. Buscamos el id del cliente aunque no sea eficiente,
                //pero así nos aseguramos que en un cambio de datos desde otro equipo no hay problemas de consistencia y si hay un cambio,
                //esta query reporte su fallo
                HashMap idClienteOdoo = (HashMap) asList((Object[]) ConexionOdoo.getInstance().getModels().execute("execute_kw", asList(
                        ConexionOdoo.getDb(), ConexionOdoo.getInstance().getUid(), ConexionOdoo.getPassword(),
                        "besteeth.cliente", "search_read",
                        asList(asList(
                                asList("email", "=", email))),
                        new HashMap() {{
                            put("fields", asList("id"));
                        }}
                ))).get(0);

                final int idCliente = (int) idClienteOdoo.get("id");

                boolean existeCitaOdoo = existeCitaOdoo(cita.getFechaHoraUTCString());

                if (existeCitaOdoo) {
                    return false;
                } else {
                    ConexionOdoo.getInstance().getModels().execute("execute_kw", asList(
                            ConexionOdoo.getDb(), ConexionOdoo.getInstance().getUid(), ConexionOdoo.getPassword(),
                            "besteeth.cita", "create",
                            asList(
                                    new HashMap() {{
                                        put("fecha_hora", cita.getFechaHoraUTCString());
                                        put("cliente_id", idCliente);
                                        put("servicio_id", idServicio);
                                        put("pago_iniciado", cita.isPagoIniciado());
                                        put("atendida", cita.isAtendida());
                                    }}
                            )
                    ));

                }
            } catch (Exception e) {
                e.printStackTrace();
                cancel(true);
            }
            return true;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            callback.altaCitaCallBack(false);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            callback.altaCitaCallBack(aBoolean);
        }
    }

    //Da de baja una cita
    public static void bajaCita(LogicaCitaCallBack callback, String email, Cita cita) {
        AsyncTask_bajaCita asyncTask_bajaCita = new AsyncTask_bajaCita();
        Object[] params = new Object[3];
        params[0] = callback;
        params[1] = email;
        params[2] = cita;
        asyncTask_bajaCita.execute(params);
    }

    public static class AsyncTask_bajaCita extends AsyncTask<Object, Void, Boolean> {

        private LogicaCitaCallBack callback;

        @Override
        protected Boolean doInBackground(Object... objects) {
            callback = (LogicaCitaCallBack) objects[0];
            String email = (String) objects[1];
            Cita cita = (Cita) objects[2];
            try {

                boolean existeCitaOdoo = existeCitaOdoo(cita.getFechaHoraUTCString());
                boolean existeClienteOdoo = existeClienteOdoo(email);

                if (!existeCitaOdoo || !existeClienteOdoo) {
                    return false;
                } else {
                    final int idCita = cita.getId();
                    ConexionOdoo.getInstance().getModels().execute("execute_kw", asList(
                            ConexionOdoo.getDb(), ConexionOdoo.getInstance().getUid(), ConexionOdoo.getPassword(),
                            "besteeth.cita", "unlink",
                            asList(asList(idCita))));
                }
            } catch (XmlRpcException e) {
                e.printStackTrace();
                cancel(true);
            }
            return true;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            callback.bajaCitaCallBack(false);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            callback.bajaCitaCallBack(aBoolean);
        }
    }

    //Aplaza una cita
    public static void aplazarCita(LogicaCitaCallBack callback, String email, Cita cita) {
        AsyncTask_aplazarCita asyncTask_aplazarCita = new AsyncTask_aplazarCita();
        Object[] params = new Object[3];
        params[0] = callback;
        params[1] = email;
        params[2] = cita;
        asyncTask_aplazarCita.execute(params);
    }

    public static class AsyncTask_aplazarCita extends AsyncTask<Object, Void, Boolean> {

        private LogicaCitaCallBack callback;

        @Override
        protected Boolean doInBackground(Object... objects) {
            callback = (LogicaCitaCallBack) objects[0];
            String email = (String) objects[1];
            Cita cita = (Cita) objects[2];

            try {
                boolean existeCitaOdoo = existeCitaOdoo(cita.getFechaHoraAntiguaUTCString());
                boolean existeClienteOdoo = existeClienteOdoo(email);

                if (!existeCitaOdoo || !existeClienteOdoo) {
                    return false;
                } else {
                    final int idCita = cita.getId();
                    ConexionOdoo.getInstance().getModels().execute("execute_kw", asList(
                            ConexionOdoo.getDb(), ConexionOdoo.getInstance().getUid(), ConexionOdoo.getPassword(),
                            "besteeth.cita", "write",
                            asList(
                                    asList(idCita),
                                    new HashMap() {{
                                        put("fecha_hora", cita.getFechaHoraUTCString());
                                    }}
                            )
                    ));
                }
            } catch (XmlRpcException e) {
                e.printStackTrace();
                cancel(true);
            }
            return true;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            callback.aplazarCita(false);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            callback.aplazarCita(aBoolean);
        }
    }

    //Recupera todas las horas ya ocupadas por una cita para la fecha pasada
    public static void recuperarHorasOcupadasDia(LogicaCitaCallBack callback, String fechaSeleccionada) {
        AsyncTask_recuperarHorasOcupadasDia asyncTask_recuperarHorasOcupadasDia = new AsyncTask_recuperarHorasOcupadasDia();
        Object[] params = new Object[2];
        params[0] = callback;
        params[1] = fechaSeleccionada;
        asyncTask_recuperarHorasOcupadasDia.execute(params);
    }

    public static class AsyncTask_recuperarHorasOcupadasDia extends AsyncTask<Object, Void, Object[]> {

        private LogicaCitaCallBack callback;

        @Override
        protected Object[] doInBackground(Object... objects) {
            callback = (LogicaCitaCallBack) objects[0];
            String fechaSeleccionada = (String) objects[1];
            String fechaInicialS = "";
            String fechaFinalS = "";
            fechaInicialS = fechaSeleccionada + " 00:00";
            fechaFinalS = fechaSeleccionada + " 23:59";

            List<Timepoint> listadoHorasOcupadasTP = new ArrayList<>();
            Configuracion configuracion = new Configuracion();

            try {
                List<Object> horasOcupadasOdoo = asList((Object[]) ConexionOdoo.getInstance().getModels().execute("execute_kw", asList(
                        ConexionOdoo.getDb(), ConexionOdoo.getInstance().getUid(), ConexionOdoo.getPassword(),
                        "besteeth.cita", "search_read",
                        asList(asList(
                                asList("fecha_hora", ">=", fechaInicialS),
                                asList("fecha_hora", "<=", fechaFinalS))),
                        new HashMap() {{
                            put("fields", asList("fecha_hora"));
                        }}
                )));

                for (int i = 0; i < horasOcupadasOdoo.size(); i++) {
                    Timepoint horaOcupada = null;
                    HashMap horasHash = (HashMap) horasOcupadasOdoo.get(i);
                    DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(DateTimeZone.UTC);
                    DateTime dateTime = dateTimeFormatter.parseDateTime((String) Objects.requireNonNull(horasHash.get("fecha_hora"))).withZone(DateTimeZone.forID("Europe/Madrid"));
                    horaOcupada = new Timepoint(dateTime.getHourOfDay(), dateTime.getMinuteOfDay());
                    listadoHorasOcupadasTP.add(horaOcupada);
                }

                //Ahora recuperamos la configuración
                HashMap configuracionOdoo = (HashMap) asList((Object[]) ConexionOdoo.getInstance().getModels().execute("execute_kw", asList(
                        ConexionOdoo.getDb(), ConexionOdoo.getInstance().getUid(), ConexionOdoo.getPassword(),
                        "besteeth.configuracion", "search_read",
                        asList(),
                        new HashMap() {{
                            put("fields", asList("hora_apertura", "hora_cierre", "intervalo_minutos"));
                        }}
                ))).get(0);

                LocalTime horaApertura = LocalTime.fromMillisOfDay((long) ((double) configuracionOdoo.get("hora_apertura")) * 3600 * 1000);
                LocalTime horaCierre = LocalTime.fromMillisOfDay((long) ((double) configuracionOdoo.get("hora_cierre")) * 3600 * 1000);
                configuracion.setHoraApertura(horaApertura);
                configuracion.setHoraCierre(horaCierre);
                configuracion.setIntervaloMinutosCita((Integer) configuracionOdoo.get("intervalo_minutos"));

            } catch (XmlRpcException e) {
                e.printStackTrace();
                cancel(true);
            }

            //Ahora creamos un array, con la cantidad de timepoints + 1, porque la última posición será la que llevará un objeto configuración
            Object[] res = new Object[listadoHorasOcupadasTP.size() + 1];
            listadoHorasOcupadasTP.toArray(res);
            res[res.length - 1] = configuracion;
            return res;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            callback.recuperarHorasOcupadasDiaCallBack(null, null);
        }

        @Override
        protected void onPostExecute(Object[] objects) {
            super.onPostExecute(objects);
            Timepoint[] timepoints = new Timepoint[objects.length - 1];
            System.arraycopy(objects, 0, timepoints, 0, objects.length - 1);
            Configuracion configuracion = (Configuracion) objects[objects.length - 1];
            callback.recuperarHorasOcupadasDiaCallBack(timepoints, configuracion);
        }
    }

    //Comprueba si existe una cita en Odoo mediante una fecha en formato UTC
    private static boolean existeCitaOdoo(String fechaHoraCitaUTC) {
        try {
            return (Integer) ConexionOdoo.getInstance().getModels().execute("execute_kw", asList(
                    ConexionOdoo.getDb(), ConexionOdoo.getInstance().getUid(), ConexionOdoo.getPassword(),
                    "besteeth.cita", "search_count",
                    asList(asList(
                            asList("fecha_hora", "=", fechaHoraCitaUTC)))
            )) != 0;
        } catch (XmlRpcException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Comprueba si existe cliente para evitar errores al aplazar o dar de baja una cita
    //y el usuario haya sido borrado de la BBDD
    private static boolean existeClienteOdoo(String emailCliente) {
        try {
            int existeClienteOdoo = (Integer) ConexionOdoo.getInstance().getModels().execute("execute_kw", asList(
                    ConexionOdoo.getDb(), ConexionOdoo.getInstance().getUid(), ConexionOdoo.getPassword(),
                    "besteeth.cliente", "search_count",
                    asList(asList(
                            asList("email", "=", emailCliente)))
            ));

            if (existeClienteOdoo != 1) {
                return false;
            }
        } catch (XmlRpcException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
