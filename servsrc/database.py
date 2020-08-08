import sqlite3, os
import pandas as pd

def gen_path(paths):
    full_path = os.path.dirname(os.path.abspath(__file__))
    for path in paths:
        full_path = os.path.join(full_path, path)
    return full_path

class Dbase:
    def __init__(self):
        self.db = self.connect_db()
        self.create_users_db()
        self.create_lectures_db()
        #df = pd.read_sql_query('SELECT * FROM users', self.db)
        #print(df)


    def connect_db(self):
        sqlite_db = sqlite3.connect(gen_path(['..', 'data', 'app_data.db']))
        return sqlite_db


    def create_users_db(self):
        self.db.execute('''CREATE TABLE IF NOT EXISTS users (google_id TEXT PRIMARY KEY, display_name TEXT, google_mail TEXT UNIQUE, photo_url TEXT UNIQUE)''')
        self.db.commit()


    def add_user(self, user_info):
        self.db.execute('INSERT OR IGNORE INTO users (google_id, display_name, google_mail, photo_url) VALUES (?, ?, ?, ?)', [user_info['google_id'],
        user_info['display_name'], user_info['google_mail'], user_info['photo_url']])
        self.db.commit()


    def create_lectures_db(self):
        self.db.execute('''CREATE TABLE IF NOT EXISTS lectures (title TEXT, description TEXT,
        lector TEXT, time INTEGER, UNIQUE(title, description, lector, time))''')
        self.db.commit()


    def add_lecture(self, lecture_info):
        self.db.execute('INSERT OR IGNORE INTO lectures (title, description, lector, time) VALUES (?, ?, ?, ?)',
        [lecture_info['title'], lecture_info['description'], lecture_info['lector'], lecture_info['time']])
        self.db.commit()


    def close_db(self):
        self.db.close()
