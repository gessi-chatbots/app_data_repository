import os
from datasets import load_dataset
from dotenv import load_dotenv
from transformers import AutoTokenizer, AutoModelForSequenceClassification, Trainer, TrainingArguments


load_dotenv()

# JOY, SADNESS, ANGER, FEAR, TRUST, DISGUST, SURPRISE, ANTICIPATION, NEUTRAL, REJECT (??)
LABEL_QTY = 10
FOLD_QTY = 10

def evaluate_metrics(trainer):
    return trainer.evaluate()

def load_trainer(model, trainer_args, tokenizer, train_split, test_split):
    return Trainer(
        model=model,
        args=trainer_args,
        train_dataset=train_split,
        eval_dataset=test_split,
        tokenizer=tokenizer,
    )

def load_trainer_args(fold_index):
    return TrainingArguments(
        output_dir=f'./results/fold_{fold_index}',
        evaluation_strategy="epoch",
        save_strategy="epoch",
        learning_rate=2e-5,
        per_device_train_batch_size=16,
        per_device_eval_batch_size=64,
        num_train_epochs=3,
        weight_decay=0.01,
        load_best_model_at_end=True,
    )

def load_tokenizer():
    return AutoTokenizer.from_pretrained(os.getenv("TOKENIZER_ID"))

def preprocess(example, tokenizer):
    return tokenizer(example['sentence'], padding='max_length', truncation=True)

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
    for fold in range(1, FOLD_QTY + 1): # Folds go from 1 to 10
        train_split = dataset[f'train_fold_{fold}']
        test_split = dataset[f'test_fold_{fold}']
        trainer = train(model, tokenizer, train_split, test_split, fold)
        all_fold_metrics.append(evaluate_metrics(trainer))
        trainer.save_model(f"./model_fold_{fold}")

def load_hf_model():
    return AutoModelForSequenceClassification.from_pretrained(os.getenv("MODEL_ID"),
                                                              num_labels=LABEL_QTY)
def load_hf_dataset():
    return load_dataset(os.getenv("REPOSITORY_K10_ID"))

def preprocess_dataset(dataset, tokenizer):
    for fold in range(1, FOLD_QTY + 1): # Folds go from 1 to 10
        dataset[f'train_fold_{fold}'] = dataset[f'train_fold_{fold}'].map(lambda x: preprocess(x, tokenizer), batched=True)
        dataset[f'test_fold_{fold}'] = dataset[f'test_fold_{fold}'].map(lambda x: preprocess(x, tokenizer), batched=True)



def main():
    dataset = load_hf_dataset()
    model = load_hf_model()
    tokenizer = load_tokenizer()
    preprocess_dataset(dataset, tokenizer)
    train_model(model, tokenizer, dataset)



if __name__ == '__main__':
    main()