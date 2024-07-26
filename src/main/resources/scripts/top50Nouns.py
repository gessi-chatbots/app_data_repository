import sys
import json
import re
import logging
from collections import Counter
import nltk
from argparse import ArgumentParser

try:
    nltk.data.find('tokenizers/punkt')
    nltk.data.find('taggers/averaged_perceptron_tagger')
except LookupError:
    import subprocess

    subprocess.check_call([sys.executable, '-m', 'pip', 'install', 'nltk'])
    nltk.download('punkt')
    nltk.download('averaged_perceptron_tagger')


def clean_text(text):
    return ''.join(c if c.isalnum() or c.isspace() else ' ' for c in text)


def camel_case_split(identifier):
    return re.findall(r'[A-Z](?:[a-z]+|[A-Z]*(?=[A-Z]|$))', identifier)


def split_into_list(text):
    return text.split()


def extraction(pos_tag_prefix):
    # 1) Get Feature and sentence from input
    input_text = sys.stdin.read()
    text_list = json.loads(input_text)

    for text in text_list:
        # 2) Clean feature
        cleaned_feature = clean_text(text['feature'])
        feature_words = []
        for word in nltk.word_tokenize(cleaned_feature):
            feature_words.extend(camel_case_split(word))
        feature_words_lower = [word.lower() for word in feature_words]

        # 2 bis) Clean Sentence
        cleaned_sentence = clean_text(text['sentence'])
        sentence_words = []
        for word in nltk.word_tokenize(cleaned_sentence):
            sentence_words.append(word)
        sentence_words_lower = [word.lower() for word in sentence_words]

        # 3) Tag sentence
        tagged_sentence = nltk.pos_tag(sentence_words)

        # 4) Initialize counter and matched positions
        matched_words_positions = {}
        for feature_word in feature_words_lower:
            logging.info(f"Searching feature word '{feature_word}' in sentence...")
            if feature_word in sentence_words_lower:
                for i, sentence_word in enumerate(sentence_words):
                    if sentence_word == feature_word:
                        tag = tagged_sentence[i][1]
                        tagged_word = tagged_sentence[i][0]
                        logging.info(f"Hit: Word '{tagged_word}' with POS TAG {tag}")
                        if sentence_word in matched_words_positions:
                            matched_words_positions[sentence_word].append(
                                {'tag': tag, 'tagged_word': tagged_word, 'index': i})
                        else:
                            matched_words_positions[sentence_word] = [
                                {'tag': tag, 'tagged_word': tagged_word, 'index': i}]
            else:
                logging.info(f"Word '{feature_word}' from feature not found in sentence")
                break
            logging.info(f'----------------------------------')

    # 5) Count most frequent POS TAG
    word_counter = Counter()
    logging.info(f"Searching for Hit results with POS TAG '{pos_tag_prefix}'...")
    for word, entries in matched_words_positions.items():
        for entry in entries:
            if entry['tag'].startswith(pos_tag_prefix):
                logging.info(f"Detected '{word}' with POS TAG '{pos_tag_prefix}'")
                word_counter[word] += 1

    top_50_words = word_counter.most_common(50)

    top_50_words_list = [{'term': word, 'frequency': freq} for word, freq in top_50_words]
    logging.info(f'----------------------------------')
    logging.info(f'Result')
    print(json.dumps(top_50_words_list, indent=2))


if __name__ == "__main__":
    parser = ArgumentParser(description="Extract NN or VV from text")
    parser.add_argument('--tag', choices=['NN', 'VV'], required=True,
                        help="Specify 'NN' for noun extraction or 'VV' for verb extraction")
    args = parser.parse_args()

    logging.basicConfig(filename='analysis.log', level=logging.INFO, format='%(asctime)s - %(message)s')
    logging.info('Starting analysis')
    logging.info(f'----------------------------------')
    pos_tag_prefix = 'NN' if args.tag == 'NN' else 'VB'
    extraction(pos_tag_prefix)
    logging.info(f'----------------------------------')
    logging.info('Analysis completed')
