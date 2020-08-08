import http.server, ssl
import socketserver
import logging
import json
import sqlite3
from database import Dbase
import cgi

dbase = Dbase()

class Serv(http.server.BaseHTTPRequestHandler):
    def handle_add_user(self, message):
        dbase.add_user(message)


    def handle_add_lecture(self, message):
        dbase.add_lecture(message)


    def handle_get_lectures(self, message):
        return dbase.get_lectures(message['begin'], message['end'])


    def _set_headers(self):
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()


    def do_HEAD(self):
        self._set_headers()


    def do_POST(self):
        logging.info('POST request')
        message = None

        try:
            length = int(self.headers.get('content-length'))
            message = json.loads(self.rfile.read(length))
        except Exception:
            self.send_response(400)
            logging.error('Bad request format')
            return

        logging.info(message)

        if message.get('type') == 'add_user':
            self.handle_add_user(message)
            self._set_headers()
        elif message.get('type') == 'add_lecture':
            self.handle_add_lecture(message)
            self._set_headers()
        elif message.get('type') == 'get_lectures':
            lectures = self.handle_get_lectures(message)
            self._set_headers()
            response = json.dumps(lectures).encode('utf-8')
            logging.info(response)
            self.wfile.write(response)





def run(log_path, server_class = http.server.HTTPServer, handler_class = Serv, port = 8000):
    logging.basicConfig(level = logging.INFO)
    #logging.basicConfig(filename = log_path, filemode = 'w', level = logging.INFO)
    serv_ip = '0.0.0.0' #'145.255.11.21'
    httpd = server_class((serv_ip, port), handler_class)

    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        logging.info('Server interrupted')

    self.dbase.close_db()
    httpd.server_close()
    logging.info('Stopping server...')
