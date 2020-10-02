# -*- coding: utf-8 -*-
{
    'name': "BesTeeth",

    'summary': """
        Módulo de gestión de Clínica Dental(BesTeeth)""",

    'description': """
        Aplicación compacta para el manejo de una clínica dental (citas, doctores, clientes)...
    """,

    'author': "Iván Fernández Campos",
    'website': "http://www.nopage.com",

    # Categories can be used to filter modules in modules listing
    # Check https://github.com/odoo/odoo/blob/13.0/odoo/addons/base/data/ir_module_category_data.xml
    # for the full list
    'category': 'Gestión',
    'version': '0.1',

    # any module necessary for this one to work correctly
    'depends': ['base'],

    # always loaded
    'data': [
        'demo/demo.xml',
        'security/security.xml',
        'security/ir.model.access.csv',
        'report/cita.xml',
        'report/recordatorio_cita.xml',
        'report/pago.xml',
        'views/cliente.xml',
        'views/servicio.xml',
        'views/wizardpago.xml',
        'views/cita.xml',
        'views/pago.xml',
        'views/doctor.xml',
        'views/configuracion.xml',
        'views/menus.xml'
    ],
    # only loaded in demonstration mode
    'demo': [
        'demo/demo.xml',
    ],
}
