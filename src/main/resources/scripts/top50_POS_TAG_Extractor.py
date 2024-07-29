import sys
import json
import re
import logging
from collections import Counter
import spacy
from argparse import ArgumentParser

# Load SpaCy model
try:
    spacy.load('en_core_web_sm')
except IOError:
    import subprocess

    subprocess.check_call([sys.executable, '-m', 'pip', 'install', 'spacy'])
    spacy.cli.download('en_core_web_sm')
    spacy.load('en_core_web_sm')

nlp = spacy.load('en_core_web_sm')


def clean_text(text):
    return ''.join(c if c.isalnum() or c.isspace() else ' ' for c in text)


def camel_case_split(identifier):
    return re.findall(r'[A-Z](?:[a-z]+|[A-Z]*(?=[A-Z]|$))', identifier)


def extraction(pos_tag_prefix):
    # 1) Get Feature and sentence from input
    input_text = sys.stdin.read()
    text_list = json.loads(input_text)
    word_counter = Counter()

    for text in text_list:
        # 2) Clean feature
        cleaned_feature = clean_text(text['feature'])
        feature_words = []
        doc_feature = nlp(cleaned_feature)
        for token in doc_feature:
            feature_words.extend(camel_case_split(token.text))
        feature_words_lower = [word.lower() for word in feature_words]

        # 2 bis) Clean Sentence
        cleaned_sentence = clean_text(text['sentence'])
        doc_sentence = nlp(cleaned_sentence)
        sentence_words_lower = [token.text.lower() for token in doc_sentence]

        # 3) Tag sentence
        tagged_sentence = [(token.text, token.pos_) for token in doc_sentence]

        # 4) Initialize matched positions
        matched_words_positions = {}
        for feature_word in feature_words_lower:
            if feature_word in sentence_words_lower:
                for i, sentence_word in enumerate(sentence_words_lower):
                    if sentence_word == feature_word:
                        tag = tagged_sentence[i][1]
                        tagged_word = tagged_sentence[i][0]
                        if sentence_word in matched_words_positions:
                            matched_words_positions[sentence_word]['tag'] = tag
                        else:
                            matched_words_positions[sentence_word] = {'tag': tag, 'tagged_word': tagged_word,
                                                                      'index': i}
            else:
                break

    # 5) Count most frequent POS TAG
    word_counter = Counter()
    for word, entry in matched_words_positions.items():
        if entry['tag'] in pos_tag_prefix:
            word_counter[word] += 1

    top_50_words = word_counter.most_common(50)
    top_50_words_list = [{'term': word, 'frequency': freq} for word, freq in top_50_words]
    print(json.dumps(top_50_words_list, indent=2))


if __name__ == "__main__":
    parser = ArgumentParser(description="Extract specific POS tags from text")
    parser.add_argument('--tag', choices=['NOUN', 'VERB'], required=True,
                        help="Specify 'NOUN' for noun extraction or 'VERB' for verb extraction")
    args = parser.parse_args()

    # Mapping POS tags to SpaCy's POS tags
    pos_tag_mapping = {
        'NOUN': {'NOUN', 'PROPN'},
        'VERB': {'VERB'}
    }

    pos_tag_prefix = pos_tag_mapping.get(args.tag)
    if not pos_tag_prefix:
        print(f"Invalid tag '{args.tag}' specified.")
        sys.exit(1)

    extraction(pos_tag_prefix)
