import json
import requests

def main():
    with open('oldestReviews.json') as f:
        uuids = json.load(f)

    url = "http://127.0.0.1:3003/reviews/{}"

    for uuid in uuids:
        delete_url = url.format(uuid)
        response = requests.delete(delete_url)
        if response.status_code == 204:
            print(f"Successfully deleted review with UUID {uuid}")
        else:
            print(f"Failed to delete review with UUID {uuid}. Status code: {response.status_code}")

if __name__ == "__main__":
    main()
