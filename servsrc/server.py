import http.server
import socketserver
import logging
import json
import sqlite3

def gen_path(paths):
    full_path = os.path.dirname(os.path.abspath(__file__))
    for path in paths:
        full_path = os.path.join(full_path, path)
    return full_path


class Serv(BaseHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super(Serv, self).__init__(*args, **kwargs)
        self.log_path = gen_path(['logs', 'serv_work.log'])
        self.users_db = sqlite3.connect(gen_path(['..', 'data', 'users.db'])

    def _set_headers(self):
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()

    def do_HEAD(self):
        self._set_headers()

    def do_POST(self):
        pass

    def create_users_db(self):




def run(server_class = http.server.HTTPServer, handler_class = http.server.BaseHTTPRequestHandler, port = 8000):
    logging.basicConfig(filename = log_path, filemode = 'w', level = logging.DEBUG)
    httpd = server_class(('localhost', port), handler_class)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        logging.info('Server interrupted')
    httpd.server_close()
    logging.info('Stopping server...\n')
