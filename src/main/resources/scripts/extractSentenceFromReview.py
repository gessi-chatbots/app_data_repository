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


def extract_sentence(text, feature):
    readable_feature = camel_case_to_readable(feature)
    sentences = nltk.sent_tokenize(text)
    for sent in sentences:
        if readable_feature in sent.lower():
            return sent.strip()
    return text.strip()


def main():
    try:
        input_text = sys.stdin.read()

        text_list = json.loads(input_text)
        results = []

        for dao in text_list:
            feature = dao['feature']
            sentence = dao['sentence']
            sentence_id = dao['sentenceId']

            extracted_sentence = extract_sentence(sentence, sentence_id)
            results.append({
                'sentence': extracted_sentence,
                'feature': feature
            })

        print(json.dumps(results))
    except Exception as e:
        print(str(e))
        sys.stderr.write(f"Error: {str(e)}\n")
        sys.exit(1)


if __name__ == "__main__":
    main()
