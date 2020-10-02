# -*- coding: utf-8 -*-
# from odoo import http


# class Besteeth(http.Controller):
#     @http.route('/besteeth/besteeth/', auth='public')
#     def index(self, **kw):
#         return "Hello, world"

#     @http.route('/besteeth/besteeth/objects/', auth='public')
#     def list(self, **kw):
#         return http.request.render('besteeth.listing', {
#             'root': '/besteeth/besteeth',
#             'objects': http.request.env['besteeth.besteeth'].search([]),
#         })

#     @http.route('/besteeth/besteeth/objects/<model("besteeth.besteeth"):obj>/', auth='public')
#     def object(self, obj, **kw):
#         return http.request.render('besteeth.object', {
#             'object': obj
#         })
