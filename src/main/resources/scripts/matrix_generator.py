import sys
import json
import pandas as pd
import logging

# Configure logging
logging.basicConfig(filename='script.log', level=logging.DEBUG, format='%(asctime)s - %(levelname)s - %(message)s')

def create_heat_matrix(input_json):
    try:
        text_list = json.loads(input_json)

        distinct_features = text_list["distinct_features"]
        verbs = text_list["verbs"].keys()
        nouns = text_list["nouns"].keys()

        heat_matrix = pd.DataFrame(0, index=nouns, columns=verbs)

        for feature in distinct_features:
            for noun in nouns:
                if noun.lower() in feature.lower():
                    for verb in verbs:
                        if verb.lower() in feature.lower():
                            heat_matrix.at[noun, verb] += 1

        heat_matrix.to_csv('verb_noun_heat_matrix.csv')
        logging.info('Heat matrix successfully saved to verb_noun_heat_matrix.csv')

    except Exception as e:
        logging.error(f'Error occurred: {e}')
        raise

if __name__ == "__main__":
    try:
        import pandas as pd
    except ImportError:
        import subprocess
        subprocess.check_call([sys.executable, '-m', 'pip', 'install', 'pandas'])
        import pandas as pd

    input_json = sys.stdin.read()
    logging.info('Starting script execution')
    create_heat_matrix(input_json)
    logging.info('Script execution finished')
