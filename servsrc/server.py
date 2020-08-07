import http.server
import socketserver
import logging

class Serv(BaseHTTPRequestHandler):



def run(log_path, server_class = http.server.HTTPServer, handler_class = http.server.BaseHTTPRequestHandler, port = 8000):
    logging.basicConfig(filename = log_path, filemode = 'w', level = logging.DEBUG)
    httpd = server_class(('', port), handler_class)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        logging.info('Server interrupted')
    httpd.server_close()
    logging.info('Stopping server...\n')
