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
    import sys

    subprocess.check_call([sys.executable, '-m', 'pip', 'install', 'nltk'])
    import nltk

    nltk.download('punkt')
    nltk.download('averaged_perceptron_tagger')


def clean_text(text):
    cleaned_text = ''.join(c if c.isalnum() or c.isspace() else ' ' for c in text)
    return cleaned_text

def camel_case_split(identifier):
    return re.findall(r'[A-Z](?:[a-z]+|[A-Z]*(?=[A-Z]|$))', identifier)


def extract_nouns_from_text(inp):
    cleaned_text = clean_text(inp)

    words = []
    for word in nltk.word_tokenize(cleaned_text):
        words.extend(camel_case_split(word))

    tagged_words = nltk.pos_tag(words)
    n = [word for word, pos in tagged_words if pos.startswith('NN')]

    return n


input_text = sys.stdin.read()

text_list = json.loads(input_text)

all_nouns = []

for text in text_list:
    nouns = extract_nouns_from_text(text)
    all_nouns.extend(nouns)

noun_freq = Counter(all_nouns)

top_50_nouns = noun_freq.most_common(50)

top_50_nouns_list = [{'term': noun, 'frequency': freq} for noun, freq in top_50_nouns]

print(json.dumps(top_50_nouns_list))
