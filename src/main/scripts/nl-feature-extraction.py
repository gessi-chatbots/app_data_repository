import json
import requests

# insert url here
url = 'http://localhost:8080/derivedNLFeatures?documentType=DESCRIPTION'
req = requests.post(url)
if req.status_code == 200:
    print("Done!")
else:
    print("Error")