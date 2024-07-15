import json
import requests

# insert url here
url = 'http://localhost:8080/derivedNLFeatures?documentType=REVIEWS'
req = requests.post(url)
if req.status_code == 200:
    print("Done!")
else:
    print("Error")