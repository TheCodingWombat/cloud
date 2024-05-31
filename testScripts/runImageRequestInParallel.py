import os
import random
import requests
from concurrent.futures import ThreadPoolExecutor

def send_request(url, payload):
    try:
        response = requests.post(url, data=payload)
        # Überprüfen, ob die Anfrage erfolgreich war (Statuscode 200)
        if response.status_code == 200:
            print("Anfrage erfolgreich gesendet.")
        else:
            print("Fehler beim Senden der Anfrage. Statuscode:", response.status_code)
    except Exception as e:
        print("Fehler beim Senden der Anfrage:", str(e))

def load_file_content(file_path):
    try:
        with open(file_path, 'r') as file:
            content = file.read()
        return content
    except Exception as e:
        print("Fehler beim Laden der Datei:", str(e))
        return None

def send_requests(url, url1, directory_path, num_requests, max_workers=10):
    files = os.listdir(directory_path)

    def task():
        # Zufällige Auswahl einer Datei aus der Liste
        file_name = random.choice(files)
        file_path = os.path.join(directory_path, file_name)
        print(file_path)

        # Laden des Inhalts aus der ausgewählten Datei
        payload = load_file_content(file_path)

        if payload is not None:
            randomInt = random.randint(0, 1)
            if randomInt == 0 :
                send_request(url, payload)
            else :
                send_request(url1, payload)
        else:
            print("Dateiinhalt null.")

    with ThreadPoolExecutor(max_workers=max_workers) as executor:
        futures = [executor.submit(task) for _ in range(num_requests)]
        for future in futures:
            try:
                future.result()  # Warten auf das Ergebnis, um Ausnahmen zu behandeln
            except Exception as e:
                print("Fehler bei der Verarbeitung einer Anfrage:", str(e))

url = "http://16.16.179.113:8001/enhanceimage"
url1 = "http://16.16.179.113:8001/blurimage"

directory_path = "D:\programming\Multilearning\decoded"
num_requests = 50
send_requests(url, url1 , directory_path, num_requests)