import json
import requests

# insert url here
url = 'http://localhost:8080/insert'

data = json.load(open('merged-apps-with-categories.json', 'r', encoding='utf-8'))
headers = {
    'Content-type': 'application/json'
}

for i, app in enumerate(data):
    data_to_send = json.dumps([app], indent=4)
    req = requests.post(url, data_to_send, headers=headers)
    if req.status_code == 200:
        print("Done app #", i + 1)
    else:
        with open('error.json', 'w', encoding='utf-8') as f:
            print(data_to_send, file=f)
        print("Error")
