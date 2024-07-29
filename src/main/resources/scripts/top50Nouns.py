import sys
import json
import re
from collections import Counter
import spacy

# Ensure SpaCy and its model are available
try:
    spacy.load('en_core_web_sm')
except IOError:
    import subprocess
    subprocess.check_call([sys.executable, '-m', 'pip', 'install', 'spacy'])
    import spacy
    spacy.cli.download("en_core_web_sm")

# Load the SpaCy model
nlp = spacy.load('en_core_web_sm')

def clean_text(text):
    return ''.join(c if c.isalnum() or c.isspace() else ' ' for c in text)

def camel_case_split(identifier):
    return re.findall(r'[A-Z](?:[a-z]+|[A-Z]*(?=[A-Z]|$))', identifier)

def split_into_list(text):
    return text.split()

def extraction(noun_tags):
    # 1) Get Feature and sentence from input
    input_text = sys.stdin.read()
    text_list = json.loads(input_text)
    word_counter = Counter()

    for text in text_list:
        # 2) Clean feature
        cleaned_feature = clean_text(text['feature'])
        feature_words = []
        for word in nlp(cleaned_feature):
            feature_words.extend(camel_case_split(word.text))
        feature_words_lower = [word.lower() for word in feature_words]

        # 2 bis) Clean Sentence
        cleaned_sentence = clean_text(text['sentence'])
        sentence_doc = nlp(cleaned_sentence)
        sentence_words_lower = [token.text.lower() for token in sentence_doc]

        # 3) Tag sentence
        tagged_sentence = [(token.text, token.pos_) for token in sentence_doc]

        # 4) Initialize matched positions
        matched_words_positions = []
        for feature_word in feature_words_lower:
            if feature_word not in sentence_words_lower:
                continue

            for i, sentence_word in enumerate(sentence_words_lower):
                if feature_word == sentence_word:
                    tag = tagged_sentence[i][1]
                    tagged_word = tagged_sentence[i][0]

                    # Check if the word is already in matched_words_positions
                    found = False
                    for entry in matched_words_positions:
                        if entry['tagged_word'] == tagged_word:
                            entry['tag'] = tag
                            found = True
                            break

                    if not found:
                        matched_words_positions.append({'tag': tag,
                                                        'tagged_word': tagged_word,
                                                        'index': i})

        # 5) Count most frequent POS TAGs
        for entry in matched_words_positions:
            if entry['tag'] in noun_tags:
                word_counter[entry['tagged_word']] += 1

    top_50_words = word_counter.most_common(50)
    top_50_words_list = [{'term': word, 'frequency': freq} for word, freq in top_50_words]
    print(json.dumps(top_50_words_list, indent=2))

def find_word_in_matched_words(word, matched_words):
    return any(matched_word['tagged_word'] == word for matched_word in matched_words)

if __name__ == "__main__":
    noun_tags = {'NOUN', 'PROPN'}  # Adjusted to SpaCy's tags for nouns
    extraction(noun_tags)
