import ijson
import json
from rdflib import URIRef

SRJ_FILE = 'OldestReviews.srj'
RESULT_FILE = 'OldestReviews.json'


def map_srj_to_json():
    definedTerms = []
    with open(SRJ_FILE, 'r', encoding="utf8") as srj_file:
        for binding in ijson.items(srj_file, "results.bindings.item"):
            uri = str(URIRef(binding['s']['value']))
            if "DefinedTerm/" in uri:
                term = uri.split("DefinedTerm/")[1]
                if term not in definedTerms:
                    definedTerms.append(term)
            else:
                definedTerms.append(uri)
    return definedTerms


def srj_to_json():
    defined_terms = map_srj_to_json()
    return json.dumps(defined_terms, indent=4)


def main():
    json_data = srj_to_json()
    with open(RESULT_FILE, 'w', encoding="utf8") as result_file:
        result_file.write(json_data)


if __name__ == "__main__":
    main()
