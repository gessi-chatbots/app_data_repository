import sys
import json
import re
from collections import Counter
import nltk

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


def extract_nouns_from_text(text):
    cleaned_feature = clean_text(text['feature'])
    feature_words = []
    for word in nltk.word_tokenize(cleaned_feature):
        feature_words.extend(camel_case_split(word))

    cleaned_sentence = clean_text(text['sentence'])
    sentence_words = []
    for word in nltk.word_tokenize(cleaned_sentence):
        sentence_words.append(word)

    tagged_feature = nltk.pos_tag(feature_words)
    tagged_sentence = nltk.pos_tag(sentence_words)

    feature_nouns = {word for word, pos in tagged_feature if pos.startswith('NN')}
    sentence_nouns = {word for word, pos in tagged_sentence if pos.startswith('NN')}

    common_nouns = feature_nouns.intersection(sentence_nouns)
    return common_nouns


def main():
    input_text = sys.stdin.read()
    text_list = json.loads(input_text)

    noun_counter = Counter()

    for text in text_list:
        common_nouns = extract_nouns_from_text(text)
        for noun in common_nouns:
            noun_counter[noun] += 1

    top_50_nouns = noun_counter.most_common(50)

    top_50_nouns_list = [{'term': noun, 'frequency': freq} for noun, freq in top_50_nouns]

    print(json.dumps(top_50_nouns_list, indent=2))


if __name__ == "__main__":
    main()
