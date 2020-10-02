# -*- coding: utf-8 -*-

from datetime import datetime, timedelta

from lxml import etree
from stdnum.cr import cr

from odoo import models, fields, api, exceptions, SUPERUSER_ID
from odoo.tools import DEFAULT_SERVER_DATETIME_FORMAT
import re
import pytz

tz = pytz.timezone('Europe/Madrid')


class Doctor(models.Model):
    _name = "besteeth.doctor"
    _sql_constraints = [
        ('constraint_dni_unique', 'unique (dni)', 'Error. DNI ya existe!!')]

    dni = fields.Char(string="DNI", required=True)
    nombre = fields.Char(string="Nombre", required=True)
    apellidos = fields.Char(string="Apellidos")
    edad = fields.Integer(string="Edad", required=True, default=18)
    sueldo = fields.Float(string="Sueldo", required=True, default=950)
    foto = fields.Binary()
    cita_ids = fields.One2many("besteeth.cita", "doctor_id", readonly=True)

    # NameGet para obtener el nombre
    def name_get(self):
        res = []
        for r in self:
            name = r.dni + ' - ' + r.nombre
            res.append((r.id, name))
        return res

    # Controlar el formato del DNI
    @api.onchange('dni')
    def _controlar_dni(self):
        if self.dni:
            pattern = re.compile("^\\d{8}[A-Z]$")
            if not pattern.match(self.dni):
                self.dni = ""
                warning = {
                    'title': ('Error.'),
                    'message': (
                        'DNI incorrecto. Recuerde que su formato es: (00000000A)')}
                return {'warning': warning}

    # Controlar la Edad
    @api.onchange('edad')
    def _controlar_edad(self):
        if self.edad:
            if self.edad > 80:
                self.edad = 80
                warning = {
                    'title': ('Error.'),
                    'message': (
                        'Edad incorrecta. La edad no puede ser superior a 80 años.')}
                return {'warning': warning}
            elif self.edad < 18:
                self.edad = 18
                warning = {
                    'title': ('Error.'),
                    'message': (
                        'Edad incorrecta. La edad mínima no puede ser inferior a 18 años.')}
                return {'warning': warning}

    # Controlar el Sueldo
    @api.onchange('sueldo')
    def _controlar_sueldo(self):
        if self.sueldo:
            if self.sueldo < 950:
                self.sueldo = 950
                warning = {
                    'title': ('Error.'),
                    'message': (
                        'Sueldo incorrecto. El sueldo mínimo es el SMI, actualmente 950€.')}
                return {'warning': warning}


class Servicio(models.Model):
    _name = "besteeth.servicio"
    _sql_constraints = [('constraint_nombre_unique', 'unique(nombre)', 'Error. Ya existe un servicio con ese nombre')]

    nombre = fields.Text(string="Nombre", required=True)
    precio = fields.Float(string="Precio", required=True, default=1)

    # NameGet para obtener el nombre
    def name_get(self):
        res = []
        for r in self:
            name = r.nombre
            res.append((r.id, name))
        return res

    # Restricción para no repetir un nombre de un servicio ya sea en minús/mayús
    @api.constrains('nombre')
    def _check_duplicate_code(self):
        servicios = self.search([])
        for s in servicios:
            if self.nombre.lower() == s.nombre.lower() and self.id != s.id:
                raise exceptions.ValidationError("Error. Servicio ya existente. Cuidado con el uso de mayús/minús")

    # Controlar el Precio
    @api.onchange('precio')
    def _controlar_precio(self):
        if self.precio:
            if self.precio < 1:
                self.precio = 1
                warning = {
                    'title': ('Error.'),
                    'message': (
                        'Precio incorrecto. No puede haber un servicio con un precio menor a 1€')}
                return {'warning': warning}


class Cliente(models.Model):
    _name = "besteeth.cliente"
    _sql_constraints = [
        ('constraint_dni_unique', 'unique (dni)', 'Error. DNI ya existe!!'),
        ('constraint_email_unique', 'unique (email)', 'Error. Email ya existe!!')]

    dni = fields.Char(string="DNI", required=True)
    nombre = fields.Char(string="Nombre", required=True)
    direccion = fields.Char(string="Dirección", required=True)
    telefono = fields.Char(string="Teléfono", required=True)
    email = fields.Char(string="Email")
    foto = fields.Binary(store=True)
    gastos_totales = fields.Float(string="Gastos Totales", compute="_calcular_gastos_totales", store=True,
                                  readonly=True)
    cita_ids = fields.One2many("besteeth.cita", "cliente_id", string="Citas")
    pago_ids = fields.One2many("besteeth.pago", "cliente_id", string="Pagos", readonly=True)

    # NameGet para obtener el nombre
    def name_get(self):
        res = []
        for r in self:
            name = r.dni + ' - ' + r.nombre
            res.append((r.id, name))
        return res

    # Controlar el formato del DNI
    @api.onchange('dni')
    def _controlar_dni(self):
        if self.dni:
            pattern = re.compile("^\\d{8}[A-Z]$")
            if not pattern.match(self.dni):
                self.dni = ""
                warning = {
                    'title': ('Error.'),
                    'message': (
                        'DNI incorrecto. Recuerde que su formato es: (00000000A)')}
                return {'warning': warning}

    # Controlar el formato del Email
    @api.onchange('email')
    def _controlar_email(self):
        if self.email:
            pattern = re.compile("^\\S+@\\S+\\.\\S+$")
            if not pattern.match(self.email):
                self.email = ""
                warning = {
                    'title': ('Error.'),
                    'message': (
                        'Email incorrecto. Su formato no corresponde a una dirección de correo electrónico')}
                return {'warning': warning}

    # Controlar el formato del Teléfono
    @api.onchange('telefono')
    def _controlar_telefono(self):
        if self.telefono:
            pattern = re.compile("^[0-9]{9}$")
            if not pattern.match(self.telefono):
                self.telefono = ""
                warning = {
                    'title': ('Error.'),
                    'message': (
                        'Teléfono incorrecto. Asegúrese de que tiene 9 dígitos')}
                return {'warning': warning}

    # Calcular los Gastos Totales en función de los pagos de las distintas citas
    @api.depends('pago_ids')
    def _calcular_gastos_totales(self):
        for r in self:
            r.gastos_totales = 0
            if len(r.pago_ids) > 0:
                for pago in r.pago_ids:
                    if pago.modalidad_pago == "cuotas":
                        r.gastos_totales += pago.pago_cuota
                    elif pago.modalidad_pago == "pago_unico":
                        r.gastos_totales += pago.pago_total
            else:
                r.gastos_totales = 0.0


class Pago(models.Model):
    _name = "besteeth.pago"

    fecha_hora = fields.Datetime(string="Fecha Hora", default=lambda self: fields.datetime.now(), readonly=True)
    modalidad_pago = fields.Selection([('pago_unico', 'Pago Único'), ('cuotas', 'Cuotas')], 'Modalidad Pago',
                                      required=True)
    pago_cuota = fields.Float(string="Precio(€) por cuota", readonly=True, group_operator=False)
    cantidad_cuotas = fields.Integer(string="Cantidad de cuotas", group_operator=False)
    cuotas_pagadas = fields.Integer(string="Cuotas pagadas", readonly=True, group_operator=False)
    cuotas_restantes = fields.Integer(string="Cuotas restantes", readonly=True, group_operator=False)
    pago_total = fields.Float(string="Pago Total(€)", readonly=True, store=True, group_operator=False)
    cita_id = fields.Many2one("besteeth.cita", string="Cita", readonly=True, ondelete="cascade")
    cliente_id = fields.Many2one("besteeth.cliente", string="Cliente", readonly=True, ondelete="cascade")
    servicio_id = fields.Many2one("besteeth.servicio", string="Servicio", readonly=True, ondelete="cascade")

    # NameGet para obtener el nombre
    def name_get(self):
        res = []
        for r in self:
            fecha_hora_cita = str(fields.Datetime.context_timestamp(self, datetime.strptime(str(r.cita_id.fecha_hora),
                                                                                            DEFAULT_SERVER_DATETIME_FORMAT)))[
                              :-6]
            fecha_hora_pago = str(fields.Datetime.context_timestamp(self, datetime.strptime(str(r.fecha_hora),
                                                                                            DEFAULT_SERVER_DATETIME_FORMAT)))[
                              :-6]
            name = 'Cita: ' + fecha_hora_cita + ' --- Pago: ' + fecha_hora_pago
            res.append((r.id, name))
        return res

    # Método para controlar el borrado de los pagos
    def unlink(self):
        for record in self:
            if record.modalidad_pago == "cuotas":
                if len(record.cita_id.pago_ids) > 1:
                    for pago in record.cita_id.pago_ids:
                        if pago.cuotas_restantes < record.cuotas_restantes:
                            raise exceptions.ValidationError(
                                'ERROR. Sólo puedes borrar el último pago realizado en una cita')
        return super(Pago, self).unlink()


class WizardPago(models.TransientModel):
    _name = "besteeth.wizardpago"

    def _default_citas(self):
        return self.env['besteeth.cita'].browse(self._context.get('active_ids'))

    @api.depends('servicio_id')
    def _calcular_pago_total(self):
        self.pago_total = self.servicio_id.precio

    cita_ids = fields.Many2many('besteeth.cita', string="Cita", default=_default_citas, readonly=True,
                                ondelete="cascade")
    fecha_hora = fields.Datetime(string="Fecha Hora", default=lambda self: fields.datetime.now())
    modalidad_pago = fields.Selection([('pago_unico', 'Pago Único'), ('cuotas', 'Cuotas')], 'Modalidad Pago',
                                      required=True)
    pago_cuota = fields.Float(string="Precio(€) por cuota")
    cantidad_cuotas = fields.Integer(string="Cantidad de cuotas")
    cuotas_pagadas = fields.Integer(string="Cuotas pagadas")
    cuotas_restantes = fields.Integer(string="Cuotas restantes")
    pago_total = fields.Float(string="Pago Total(€)", store=True, compute=_calcular_pago_total)
    cliente_id = fields.Many2one("besteeth.cliente", string="Cliente", readonly=True, ondelete="cascade")
    servicio_id = fields.Many2one("besteeth.servicio", string="Servicio", readonly=True)

    # Método para guardar un pago en la cita al cerrar el wizard
    def add_pago(self):
        for r in self:
            if self.modalidad_pago == "cuotas":
                self.cuotas_pagadas += 1
                self.cuotas_restantes -= 1
            r.cita_ids.write({'pago_ids': [(0, 0, {'fecha_hora': self.fecha_hora, 'modalidad_pago': self.modalidad_pago,
                                                   'pago_cuota': self.pago_cuota,
                                                   'cantidad_cuotas': self.cantidad_cuotas,
                                                   'cuotas_pagadas': self.cuotas_pagadas,
                                                   'cuotas_restantes': self.cuotas_restantes,
                                                   'pago_total': self.pago_total, 'cita_id': r.cita_ids,
                                                   'cliente_id': self.cliente_id.id,
                                                   'servicio_id': str(self.servicio_id.id)})]})

    # Controlar el cambio en el campo servicio_id cuando aún no se ha creado el primer pago
    @api.onchange('servicio_id')
    def _controlar_cambio_servicio(self):
        if self.servicio_id:
            if self.modalidad_pago == "cuotas" and self.cuotas_pagadas < 1:
                self.cantidad_cuotas = 2
            elif self.modalidad_pago == "pago_unico":
                self.cantidad_cuotas = 0
                self.cuotas_pagadas = 0
                self.cuotas_restantes = 0
                self.pago_cuota = 0

    # Método clave para setear los datos nada más entrar al wizard, nos ayuda a establecer los campos
    @api.onchange('cita_ids')
    def _establecer_datos(self):
        if len(self.cita_ids[0].pago_ids) >= 1 and self.cita_ids[0].pago_ids[
            len(self.cita_ids[0].pago_ids) - 1].modalidad_pago == "cuotas":
            ultimaCita = len(self.cita_ids[0].pago_ids) - 1
            self.cuotas_pagadas = self.cita_ids[0].pago_ids[ultimaCita].cuotas_pagadas
            self.pago_cuota = self.cita_ids[0].pago_ids[ultimaCita].pago_cuota
            self.pago_total = self.cita_ids[0].pago_ids[ultimaCita].pago_total
            self.cantidad_cuotas = self.cita_ids[0].pago_ids[ultimaCita].cantidad_cuotas
            self.cuotas_restantes = self.cita_ids[0].pago_ids[ultimaCita].cuotas_restantes
            self.modalidad_pago = "cuotas"
        else:
            self.modalidad_pago = "pago_unico"

    # Controlar el cambio en el campo cantidad_cuotas cuando aún no se ha hecho el primer pago
    @api.onchange('cantidad_cuotas')
    def _calcular_precios_cuotas(self):
        if self.modalidad_pago == "cuotas" and self.cuotas_pagadas < 1:
            if self.cantidad_cuotas < 2:
                self.cantidad_cuotas = 2
                self.cuotas_restantes = 2
                self.cuotas_pagadas = 0
                self.pago_cuota = self.pago_total / self.cantidad_cuotas
                warning = {
                    'title': ('Error.'),
                    'message': (
                        'La cantidad de cuotas es de mínimo 2 para procesar la cita')}
                return {'warning': warning}
            elif self.cantidad_cuotas > 5:
                self.cantidad_cuotas = 2
                self.cuotas_restantes = 2
                self.cuotas_pagadas = 0
                self.pago_cuota = self.pago_total / self.cantidad_cuotas
                warning = {
                    'title': ('Error.'),
                    'message': (
                        'La cantidad de cuotas no puede ser superior a 5 meses')}
                return {'warning': warning}
            else:
                self.cuotas_restantes = self.cantidad_cuotas
                self.cuotas_pagadas = 0
                self.pago_cuota = self.pago_total / self.cantidad_cuotas

    # Controlar el cambio en el campo modalidad_pago cuando aún no se ha hecho el primer pago
    @api.onchange('modalidad_pago')
    def _controlar_modalidades_pago(self):
        if self.cuotas_pagadas < 1:
            if self.modalidad_pago == "cuotas":
                self.cantidad_cuotas = 2
            if self.modalidad_pago == "pago_unico":
                self.cantidad_cuotas = 0
                self.cuotas_pagadas = 0
                self.cuotas_restantes = 0
                self.pago_cuota = 0


class Cita(models.Model):
    _name = "besteeth.cita"
    _sql_constraints = [
        ('constraint_fecha_hora', 'unique (fecha_hora)', 'Error. Ya hay una cita registrada en esa hora!!')]

    name = fields.Char(string="Nombre", compute="_calcular_name", store="True")
    fecha_hora = fields.Datetime(string="Fecha Hora", required=True, store=True)
    cerrada = fields.Boolean(string="¿Cerrada?", compute="_comprobar_estado_cita", store=True)
    doctor_id = fields.Many2one("besteeth.doctor", string="Doctor", ondelete="set null")
    cliente_id = fields.Many2one("besteeth.cliente", string="Cliente", required=True, ondelete="cascade")
    servicio_id = fields.Many2one("besteeth.servicio", string="Servicio", required=True, ondelete="cascade", store=True)
    pago_ids = fields.One2many("besteeth.pago", "cita_id", string="Pagos")
    pago_iniciado = fields.Boolean(compute="_comprobar_pagos_iniciados", store=True)
    atendida = fields.Boolean(string="¿Atendida?", store=True)

    # Para setear name al cambiar la fecha de la cita
    @api.depends("fecha_hora")
    def _calcular_name(self):
        for r in self:
            if r.cliente_id.dni and r.fecha_hora:
                fecha_hora = str(fields.Datetime.context_timestamp(self, datetime.strptime(str(r.fecha_hora),
                                                                                           DEFAULT_SERVER_DATETIME_FORMAT)))[
                             :-9]
                r.name = r.cliente_id.dni + ' - ' + fecha_hora

    # Validar fecha es correcta
    @api.onchange("fecha_hora")
    def _controlar_fecha_hora(self):
        if self.fecha_hora:
            configuracion = self.env['besteeth.configuracion'].search([])[0]

            fecha_hora_seleccionadas = self.fecha_hora

            if datetime.strptime(str(fecha_hora_seleccionadas), "%Y-%m-%d %H:%M:%S").date().weekday() >= 5:
                self.fecha_hora = ""
                warning = {
                    'title': ('Error.'),
                    'message': (
                        'No se puede seleccionar un día en fin de semana!!')}
                return {'warning': warning}

            fecha_hora_seleccionadas_s = str(self.fecha_hora)
            sql_string_query = "select * from besteeth_cita where fecha_hora ='" + fecha_hora_seleccionadas_s + "'"
            self.env.cr.execute(sql_string_query)
            res = self.env.cr.fetchall()

            tiempo_seleccionado = str(fecha_hora_seleccionadas.hour + 2) + ':' + str(
                fecha_hora_seleccionadas.minute) + ':00'
            tiempo_seleccionado_array = tiempo_seleccionado.split(':')
            tiempo_seleccionado_float = ((int(tiempo_seleccionado_array[0])) * 60 * 60 + int(
                tiempo_seleccionado_array[1]) * 60 + int(tiempo_seleccionado_array[2])) / 3600

            hora_apertura_precision4 = round(configuracion.hora_apertura, 4)
            hora_cierre_precision4 = round(configuracion.hora_cierre, 4)
            tiempo_seleccionado_float_precision4 = round(tiempo_seleccionado_float, 4)

            if res:
                self.fecha_hora = ""
                warning = {
                    'title': ('Error.'),
                    'message': (
                        'Ya hay una cita registrada en esa hora!!')}
                return {'warning': warning}
            else:
                if tiempo_seleccionado_float_precision4 < hora_apertura_precision4:
                    self.fecha_hora = ""
                    warning = {
                        'title': ('Error.'),
                        'message': (
                            'La hora de la cita tiene que ser igual o superior a la hora de apertura')}
                    return {'warning': warning}
                elif tiempo_seleccionado_float_precision4 > hora_cierre_precision4:
                    self.fecha_hora = ""
                    warning = {
                        'title': ('Error.'),
                        'message': (
                            'La hora de la cita tiene que ser igual o inferior a la hora de cierre')}
                    return {'warning': warning}

    # NameGet para obtener el nombre
    def name_get(self):
        res = []
        for r in self:
            fecha_hora = str(fields.Datetime.context_timestamp(self, datetime.strptime(str(r.fecha_hora),
                                                                                       DEFAULT_SERVER_DATETIME_FORMAT)))[
                         :-9]
            name = r.cliente_id.dni + ' - ' + fecha_hora
            res.append((r.id, name))
        return res

    # Controlar si una cita ya ha sido cerrada o no, consultando sus pagos
    @api.depends('pago_ids')
    def _comprobar_estado_cita(self):
        for r in self:
            r.cerrada = False
            if len(r.pago_ids) > 0:
                for pago in r.pago_ids:
                    if pago.modalidad_pago == "pago_unico":
                        r.cerrada = True
                        break
                    elif pago.modalidad_pago == "cuotas":
                        if pago.cuotas_restantes == 0:
                            r.cerrada = True
                            break

    # Controlar si se ha realizado un primer pago para deshabilitar la edición del campo fecha hora, y que esa cita no pueda cambiar de hora
    @api.depends('pago_ids')
    def _comprobar_pagos_iniciados(self):
        for r in self:
            r.pago_iniciado = False
            if len(r.pago_ids) > 0:
                r.pago_iniciado = True

    # Para el report del recordatorio de una cita
    def imprimir_recordatorio_cita(self):
        for r in self:
            return self.env.ref("besteeth.report_recordatorio_cita").report_action(r)


class Configuracion(models.Model):
    _name = "besteeth.configuracion"

    hora_apertura = fields.Float(string="Hora de apertura", default=9)
    hora_cierre = fields.Float(string="Hora de cierre", default=20)
    intervalo_minutos = fields.Integer(string="Intervalo minutos para una cita (APP Móvil)", default=5)

    # Controlar el formato de la hora de apertura
    @api.onchange('hora_apertura')
    def _controlar_hora_apertura(self):
        if self.hora_apertura:
            if self.hora_apertura >= 23.83 or self.hora_apertura >= self.hora_cierre:
                warning = {
                    'title': ('Error.'),
                    'message': (
                        'Hora de apertura incorrecta. Recuerde no debe de ser superior a la hora de cierre y respetar el formato HH:MM')}
                return {'warning': warning}

    # Controlar el formato de la hora de cierre
    @api.onchange('hora_cierre')
    def _controlar_hora_cierre(self):
        if self.hora_cierre:
            if self.hora_cierre >= 23.83 or self.hora_cierre <= self.hora_apertura:
                self.hora_cierre = 0
                warning = {
                    'title': ('Error.'),
                    'message': (
                        'Hora de cierre incorrecta. Recuerde no debe de ser inferior a la hora de apertura y respetar el formato HH:MM')}
                return {'warning': warning}

    # Controlar el intervalo de minutos
    @api.onchange('intervalo_minutos')
    def _controlar_intervalo_minutos(self):
        if self.intervalo_minutos > 55:
            self.intervalo_minutos = 5
        elif self.intervalo_minutos < 5:
            self.intervalo_minutos = 55

    # Controla que aparezca o desaparezca el botón de Crear, dependiendo
    # de si ya hay un registro de configuración o no
    @api.model
    def fields_view_get(self, view_id=None, view_type='form',
                        toolbar=False, submenu=False):
        res = super(Configuracion, self).fields_view_get(
            view_id=view_id, view_type=view_type,
            toolbar=toolbar, submenu=submenu)
        configuracion = self.env['besteeth.configuracion'].search([])
        cantidad_configuraciones = 0
        for x in configuracion:
            cantidad_configuraciones = cantidad_configuraciones + 1
        if cantidad_configuraciones > 0:
            root = etree.fromstring(res['arch'])
            root.set('create', 'false')
            res['arch'] = etree.tostring(root)
        return res
