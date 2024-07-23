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


def extract_verbs_from_text(text):
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

    feature_verbs = {word for word, pos in tagged_feature if pos.startswith('VB')}
    sentence_verbs = {word for word, pos in tagged_sentence if pos.startswith('VB')}

    common_verbs = feature_verbs.intersection(sentence_verbs)
    return common_verbs


def main():
    input_text = sys.stdin.read()
    text_list = json.loads(input_text)

    verb_counter = Counter()

    for text in text_list:
        common_verbs = extract_verbs_from_text(text)
        for verb in common_verbs:
            verb_counter[verb] += 1

    top_50_verbs = verb_counter.most_common(50)

    top_50_verbs_list = [{'term': verb, 'frequency': freq} for verb, freq in top_50_verbs]

    print(json.dumps(top_50_verbs_list, indent=2))


if __name__ == "__main__":
    main()
