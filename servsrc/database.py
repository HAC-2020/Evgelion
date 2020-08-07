import sqlite3

def gen_path(paths):
    full_path = os.path.dirname(os.path.abspath(__file__))
    for path in paths:
        full_path = os.path.join(full_path, path)
    return full_path

class Dbase:
    def __init__(self):
        self.db = None

    def connect_db(self):
        self.db = sqlite3.connect(gen_path(['..', 'data', 'app_data.db']))

    def create_users_db(self):
        self.db.execute('''CREATE TABLE users (google_id BIGINT, google_mail TEXT)''')

    def add_user(self, user_info):
        self.db.execute('INSERT INTO users VALUES (?, ?, ?)', [user_info['google_id'], user_info['google_mail']])
