import os
import random
import requests
from concurrent.futures import ThreadPoolExecutor

def send_request(url, payload):
    try:
        response = requests.post(url, data=payload)
        # Check if the request was successful (Status code 200)
        if response.status_code == 200:
            print("Anfrage erfolgreich gesendet.")
            return True
        else:
            print("Fehler beim Senden der Anfrage. Statuscode:", response.status_code)
            return False
    except Exception as e:
        print("Fehler beim Senden der Anfrage:", str(e))
        return False

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
    success_count = 0

    def task():
        nonlocal success_count
        # Randomly select a file from the list
        file_name = random.choice(files)
        file_path = os.path.join(directory_path, file_name)
        print(file_path)

        # Load the content from the selected file
        payload = load_file_content(file_path)

        if payload is not None:
            random_int = random.randint(0, 1)
            if random_int == 0:
                success = send_request(url, payload)
            else:
                success = send_request(url1, payload)
            if success:
                success_count += 1
        else:
            print("Dateiinhalt null.")

    with ThreadPoolExecutor(max_workers=max_workers) as executor:
        futures = [executor.submit(task) for _ in range(num_requests)]
        for future in futures:
            try:
                future.result()  # Wait for the result to handle exceptions
            except Exception as e:
                print("Fehler bei der Verarbeitung einer Anfrage:", str(e))

    print("Number of successful requests:", success_count)

url = "http://16.16.179.113:8001/enhanceimage"
url1 = "http://16.16.179.113:8001/blurimage"

directory_path = "D:\\programming\\Multilearning\\decoded"
num_requests = 50
send_requests(url, url1, directory_path, num_requests)
