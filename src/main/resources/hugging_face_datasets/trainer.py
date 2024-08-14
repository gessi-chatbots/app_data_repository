

FOLD_QTY = 10
MODEL_NAME = "quim-motger/reviewBERT-base"
def evaluate_metrics(trainer):
    return trainer.evaluate()

def load_trainer(trainer_args):
    return None

def load_trainer_args():
    return None

def train(model):
    trainer = load_trainer(load_trainer_args())
    trainer.train()
    return trainer

def train_model(model, dataset):
    all_fold_metrics = []
    for fold in range(FOLD_QTY):
        train_split = ""
        test_split = ""
        trainer = train(model)
        fold_metrics = evaluate_metrics(trainer)




def load_model():
    return None

def load_dataset():
    return None

def main():
    dataset = load_dataset()

    model = load_model()

    train_model(model, dataset)





if __name__ == '__main__':
    main()