import sys
import json
from collections import Counter
import nltk

# Ensure required NLTK resources are available
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

def extract_verbs_from_text(text):
    words = nltk.word_tokenize(text)
    tagged_words = nltk.pos_tag(words)
    verbs = [word for word, pos in tagged_words if pos.startswith('VB')]
    return verbs

input_text = sys.stdin.read()

text_list = json.loads(input_text)

all_verbs = []

for text in text_list:
    verbs = extract_verbs_from_text(text)
    all_verbs.extend(verbs)

verb_freq = Counter(all_verbs)

# Get the 50 most common verbs with their frequencies
top_50_verbs = verb_freq.most_common(50)

# Prepare the output list with verb and frequency
top_50_verbs_list = [{'verb': verb, 'frequency': freq} for verb, freq in top_50_verbs]

# Print the top 50 verbs as a JSON array with frequencies
print(json.dumps(top_50_verbs_list))