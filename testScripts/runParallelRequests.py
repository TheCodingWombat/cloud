import requests
import threading
import time

# URL des Endpunkts auf deinem lokalen Server
url = "http://localhost:8000/test"
num_requests = 50  # Anzahl der Anfragen

# Semaphore, um parallele Ausführung zu steuern
semaphore = threading.Semaphore()

# Funktion, um eine Anfrage an die URL zu senden
def send_request(request_num):
    response = requests.get(url)
    if response.status_code == 200:
        with semaphore:
            print(f"Anfrage {request_num}: Erfolgreich - URL: {url} - Statuscode: {response.status_code} - Inhalt der Antwort: {response.text}")
    else:
        with semaphore:
            print(f"Anfrage {request_num}: Fehler - URL: {url} - Statuscode: {response.status_code}")

# Threads für parallele Anfragen erstellen und starten
threads = []
for i in range(1, num_requests + 1):
    thread = threading.Thread(target=send_request, args=(i,))
    threads.append(thread)
    thread.start()
    time.sleep(0.1)  # Kurze Pause zwischen dem Starten der Threads

user_input = input("Drücke 'y' und Enter zum Beenden: ")
while user_input != 'y':
    user_input = input("Falsche Eingabe. Drücke 'y' und Enter zum Beenden: ")