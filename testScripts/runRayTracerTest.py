import os
import random
import requests

payload = """{"scene": "0  0  0\n0  0 -1\n0  1  0\n30\n\n3\n  0    0   0   0.2  0.2  0.2   1  0  0\n 10  100  10   1.0  1.0  1.0   1  0  0\n100  100 100   1.0  1.0  1.0   1  0  0\n\n3\nsolid        1  0  0\nsolid        0  1  0\nsolid        0  0  1\n\n2\n0.4  0.6  0.0  1    0  0  0\n0.4  0.6  0.7  500  0  0  0\n\n4\n0 1 sphere   3   3  -15    1\n1 0 sphere   1   0  -15    2\n2 1 sphere   5  -5  -25    3\n2 1 sphere  -5   0  -30    4\n", "texmap": []}"""
def send_request(url):
    try:
        response = requests.post(url, params=generate_random_params(), data=payload)

        print("URL:", response.request.url)
        print("Headers:", response.request.headers)
        print("Body:", response.request.body)
        print("Method:", response.request.method)
            
        if response.status_code == 200:
            print("Anfrage erfolgreich gesendet.")
        else:
            print("Fehler beim Senden der Anfrage. Statuscode:", response.status_code)
    except Exception as e:
        print("Fehler beim Senden der Anfrage:", str(e))

def generate_random_params():
    params = {
        "scols": random.randint(0, 1000),
        "srows": random.randint(0, 1000),
        "wcols": random.randint(0, 1000),
        "wrows": random.randint(0, 1000),
        "coff": random.randint(0, 1000),
        "roff": random.randint(0, 1000),
        "aa": "false",
        "mult": "false"
    }
    return params

def send_requests(url, num_requests):
    for i in range(num_requests):
        send_request(url)

def build_url(ip, port):
    url = "http://"
    url += ip
    url += ":"
    url += str(port)
    url += "/raytracer"
    return url


ip = "13.38.238.235"
port = 8000 
num_requests = 1

send_requests(build_url(ip, port), num_requests)