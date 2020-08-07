import http.server
import socketserver
import logging
import json
import sqlite3
from database import Dbase
import cgi

class Serv(http.server.BaseHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super(Serv, self).__init__(*args, **kwargs)
        #self.dbase = Dbase()

    def _set_headers(self):
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()

    def do_HEAD(self):
        self._set_headers()

    def do_POST(self):
        length = int(self.headers.get('content-length'))
        message = json.loads(self.rfile.read(length))
        message['received'] = 'ok'
        self._set_headers()
        self.wfile.write(json.dumps(message).encode('utf-8'))


def run(log_path, server_class = http.server.HTTPServer, handler_class = Serv, port = 8000):
    logging.basicConfig(filename = log_path, filemode = 'w', level = logging.DEBUG)
    httpd = server_class(('localhost', port), handler_class)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        logging.info('Server interrupted')
    httpd.server_close()
    logging.info('Stopping server...\n')
