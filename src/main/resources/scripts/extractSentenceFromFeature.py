import sys
import json
import re
import nltk
import subprocess


def ensure_nltk_data():
    try:
        nltk.data.find('tokenizers/punkt')
        nltk.data.find('taggers/averaged_perceptron_tagger')
    except LookupError:
        subprocess.check_call([sys.executable, '-m', 'pip', 'install', 'nltk'])
        nltk.download('punkt')
        nltk.download('averaged_perceptron_tagger')


ensure_nltk_data()


def camel_case_to_readable(text):
    readable_text = re.sub('([a-z])([A-Z])', r'\1 \2', text)
    return readable_text.lower()


def extract_sentences(text, feature):
    readable_feature = camel_case_to_readable(feature)
    sentences = nltk.sent_tokenize(text)
    matching_sentences = [sent.strip() for sent in sentences if readable_feature in sent.lower()]
    if matching_sentences is None or len(matching_sentences) == 0:
        matching_sentences = [text]
    return matching_sentences


def main():
    try:
        input_data = json.load(sys.stdin)
        feature = input_data['feature']
        sentence = input_data['sentence']
        result = extract_sentences(sentence, feature)
        output_data = {'extracted_sentences': result}
        print(json.dumps(output_data))
    except Exception as e:
        print(str(e))
        sys.stderr.write(f"Error: {str(e)}\n")
        sys.exit(1)


if __name__ == "__main__":
    main()
