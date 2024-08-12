import os
import pandas as pd
from sklearn.model_selection import train_test_split

CSV_PATH = 'datasets/csv'
SPLIT_PATH = 'datasets/splits'
COLUMNS = ['app_name', 'categoryId', 'reviewId', 'sentenceId', 'feature', 'review', 'sentence', 'emotion-primary-agreement']
def read_csv_file(csv_file):
    csv = pd.read_csv(CSV_PATH + '/' + csv_file)
    cleaned_csv = csv[COLUMNS]
    stringified_csv = cleaned_csv.applymap(str)
    return stringified_csv

def merge_csv_files(csv_files):
    return pd.concat(csv_files)

def generate_datasets(file):
    datasets = []
    train_data, temp_data = train_test_split(file, test_size=0.3, random_state=42)
    datasets.append({'name': 'training_dataset', 'dataset' : train_data})
    val_data, test_data = train_test_split(temp_data, test_size=0.5, random_state=42)
    datasets.append({'name': 'validation_dataset', 'dataset' : val_data})
    datasets.append({'name': 'test_dataset', 'dataset' : test_data})
    return datasets


def save_datasets(datasets):
    for dataset_dic in datasets:
        if dataset_dic['name'] == 'training_dataset':
            path = os.path.join(SPLIT_PATH, 'training')
        elif dataset_dic['name'] == 'validation_dataset':
            path = os.path.join(SPLIT_PATH, 'validation')
        elif dataset_dic['name'] == 'test_dataset':
            path = os.path.join(SPLIT_PATH, 'test')

        dataset_dic['dataset'].to_csv(path_or_buf=os.path.join(path, dataset_dic['name'] + '.csv'))

def main():
    csv_files = os.listdir(CSV_PATH)
    read_csv_files = []

    for csv_file in csv_files:
        read_csv_files.append(read_csv_file(csv_file))

    merged_csv = merge_csv_files(read_csv_files)

    datasets = generate_datasets(merged_csv)
    save_datasets(datasets)


if __name__ == '__main__':
    main()