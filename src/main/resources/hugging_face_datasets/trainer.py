import os
import numpy as np
from datasets import load_dataset
from dotenv import load_dotenv
from transformers import BertTokenizer, BertForSequenceClassification, Trainer, TrainingArguments, PushToHubCallback
import evaluate

load_dotenv()

# Number of labels and folds
FOLD_QTY = 10
LABEL_QTY = 10
LABEL_MAP = {
    'Joy': 0, 'Sadness': 1, 'Anger': 2, 'Fear': 3, 'Trust': 4, 'Disgust': 5,
    'Surprise': 6, 'Anticipation': 7, 'Neutral': 8, 'Reject': 9
}
metric = evaluate.load("accuracy")


def compute_metrics(eval_pred):
    logits, labels = eval_pred
    predictions = np.argmax(logits, axis=-1)
    return metric.compute(predictions=predictions, references=labels)


def evaluate_metrics(trainer):
    return trainer.evaluate()


def load_trainer(model, trainer_args, tokenizer, train_split, test_split):
    return Trainer(
        model=model,
        args=trainer_args,
        train_dataset=train_split,
        eval_dataset=test_split,
        tokenizer=tokenizer,
        compute_metrics=compute_metrics
    )


def load_trainer_args(fold_index):
    return TrainingArguments(
        output_dir=f'./results/fold_{fold_index}',
        eval_strategy="epoch",
        save_strategy="epoch",
        learning_rate=2e-5,
        per_device_train_batch_size=16,
        per_device_eval_batch_size=64,
        num_train_epochs=3,
        weight_decay=0.01,
        load_best_model_at_end=True,
    )


def load_tokenizer():
    return BertTokenizer.from_pretrained(os.getenv("TOKENIZER_ID"))


def preprocess(examples, tokenizer):
    tokens = tokenizer(
        examples['sentence'],
        padding='max_length',
        truncation=True,
        max_length=128,
        return_tensors=None
    )

    labels = [LABEL_MAP.get(emotion, -1) for emotion in examples['emotion-primary-agreement']]

    if any(label >= LABEL_QTY for label in labels):
        raise ValueError("Label out of bounds!")

    return {
        'input_ids': tokens['input_ids'],
        'attention_mask': tokens['attention_mask'],
        'labels': labels
    }


def train(model, tokenizer, train_split, test_split, fold_index):
    trainer = load_trainer(model,
                           load_trainer_args(fold_index),
                           tokenizer,
                           train_split,
                           test_split)
    trainer.train()
    return trainer


def train_model(model, tokenizer, dataset):
    all_fold_metrics = []
    for fold in range(1, FOLD_QTY + 1):  # Folds go from 1 to 10
        train_split = dataset[f'train_fold_{fold}']
        test_split = dataset[f'test_fold_{fold}']
        trainer = train(model, tokenizer, train_split, test_split, fold)
        all_fold_metrics.append(evaluate_metrics(trainer))
        trainer.save_model(f"./model_fold_{fold}")
    return all_fold_metrics


def load_hf_model():
    return BertForSequenceClassification.from_pretrained(os.getenv("MODEL_ID"), num_labels=LABEL_QTY)


def load_hf_dataset():
    return load_dataset(os.getenv("REPOSITORY_K10_ID"))


def preprocess_dataset(dataset, tokenizer):
    for fold in range(1, FOLD_QTY + 1):  # Folds go from 1 to 10
        train_split = dataset[f'train_fold_{fold}']
        test_split = dataset[f'test_fold_{fold}']

        train_split = train_split.map(lambda x: preprocess(x, tokenizer), batched=True)
        test_split = test_split.map(lambda x: preprocess(x, tokenizer), batched=True)

        dataset[f'train_fold_{fold}'] = train_split
        dataset[f'test_fold_{fold}'] = test_split


def save_metrics_to_file(metrics, filename):
    with open(filename, 'w') as file:
        file.write("Evaluation Metrics per Fold:\n\n")
        for fold_index, metric in enumerate(metrics, 1):
            file.write(f"Fold {fold_index} Metrics:\n")
            for key, value in metric.items():
                file.write(f"{key}: {value}\n")
            file.write("\n")


def push_model_to_hf():
    model_name = os.getenv("MODEL_RESULT_ID")
    for fold in range(1, FOLD_QTY + 1):
        print(f"Fold {fold} model pushed to Hugging Face Hub.")
        model = BertForSequenceClassification.from_pretrained(f"./model_fold_{fold}")
        tokenizer = BertTokenizer.from_pretrained(os.getenv("TOKENIZER_ID"))
        model.push_to_hub(f"{model_name}_fold_{fold}")
        tokenizer.push_to_hub(f"{model_name}_fold_{fold}")


def main():
    # dataset = load_hf_dataset()
    # model = load_hf_model()
    # tokenizer = load_tokenizer()
    # preprocess_dataset(dataset, tokenizer)

    # metrics = train_model(model, tokenizer, dataset)
    # save_metrics_to_file(metrics, 'metrics.txt')
    push_model_to_hf()


if __name__ == '__main__':
    main()
