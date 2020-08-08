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
        self.curs = self.db.cursor()
        self.create_users_table()
        self.create_lectures_table()


    def connect_db(self):
        sqlite_db = sqlite3.connect(gen_path(['..', 'data', 'app_data.db']))
        return sqlite_db


    def create_users_table(self):
        self.curs.execute('''CREATE TABLE IF NOT EXISTS users (google_id TEXT PRIMARY KEY, display_name TEXT, google_mail TEXT UNIQUE, photo_url TEXT UNIQUE)''')
        self.db.commit()


    def add_user(self, user_info):
        self.curs.execute('INSERT OR IGNORE INTO users (google_id, display_name, google_mail, photo_url) VALUES (?, ?, ?, ?)', [user_info['google_id'],
        user_info['display_name'], user_info['google_mail'], user_info['photo_url']])
        self.db.commit()


    def create_lectures_table(self):
        self.curs.execute('''CREATE TABLE IF NOT EXISTS lectures (title TEXT, description TEXT,
        author TEXT, time BIGINT, UNIQUE(title, description, author, time))''')
        self.db.commit()


    def add_lecture(self, lecture_info):
        self.curs.execute('INSERT OR IGNORE INTO lectures (title, description, author, time) VALUES (?, ?, ?, ?)',
        [lecture_info['title'], lecture_info['description'], lecture_info['author'], lecture_info['time']])
        self.db.commit()


    def get_lectures(self, i, j):
        self.curs.execute('SELECT * FROM lectures WHERE time > ? ORDER BY time LIMIT ? OFFSET ?', [1, j - i, i])
        raw_data = self.curs.fetchall()
        return [{'title': note[0], 'description': note[1], 'author': note[2], 'time': note[3]} for note in raw_data]


    def close_db(self):
        self.db.close()
