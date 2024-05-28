import os
import random
import requests

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

def send_requests(url, directory_path, num_requests):
 
    files = os.listdir(directory_path)
    
 

    for i in range(num_requests):
        print("Anfrage", i+1)
        # Zufällige Auswahl einer Datei aus der Liste
        file_name = random.choice(files)
        file_path = directory_path + file_name

        # Laden des Inhalts aus der ausgewählten Datei
        payload = load_file_content(file_path)

        if payload is not None:
            send_request(url, payload)
        else:
            print("Dateiinhalt null.")



url = "http://localhost:8001/blurimage"
directory_path = "C:\\Users\\herrt\\git\\cloud\\testScripts\\base64_encoded_pictures\\"
num_requests = 10
send_requests(url, directory_path, num_requests)