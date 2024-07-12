import sys
import json

try:
    import pandas as pd
except ImportError:
    import subprocess

    subprocess.check_call([sys.executable, '-m', 'pip', 'install', 'pandas'])
    import pandas as pd

input_json = sys.stdin.read()

text_list = json.loads(input_json)

distinct_features = text_list["distinct_features"]
verbs = text_list["verbs"]
nouns = text_list["nouns"]

heat_matrix = pd.DataFrame(0, index=nouns.keys(), columns=verbs.keys())

for feature in distinct_features:
    for noun in nouns:
        if noun.lower() in feature.lower():
            for verb in verbs:
                if verb.lower() in feature.lower():
                    heat_matrix.at[noun, verb] += 1

heat_matrix.to_csv('verb_noun_heat_matrix.csv')
