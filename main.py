import sys, os

def gen_path(paths):
    full_path = os.path.dirname(os.path.abspath(__file__))
    for path in paths:
        full_path = os.path.join(full_path, path)
    return full_path

sys.path.append(gen_path(['servsrc']))

import server

if __name__ == '__main__':
    logs = gen_path(['logs', 'serv_work.log'])
    if len(sys.argv) == 2:
        server.run(log_path = logs, port = int(sys.argv[1]))
    else:
        server.run(log_path = logs)
