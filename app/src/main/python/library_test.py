import os
import librosa
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.svm import SVC
from sklearn.preprocessing import StandardScaler
from sklearn.pipeline import make_pipeline

# 음성 데이터를 저장한 폴더
def run_feature_extraction(data_folder, output_x_path, output_y_path):
    print("1")
    words = ['안녕하세요', '감사합니다', '네', '아니요']

    # 데이터를 저장할 리스트
    X = []
    y = []
    print("2")
    # 특징 추출 함수
    def extract_features(file_path):
        print("extract_features")
        y, sr = librosa.load(file_path, sr=None)
        mfccs = librosa.feature.mfcc(y=y, sr=sr, n_mfcc=13)
        return np.mean(mfccs.T, axis=0)

    def predict_word(file_path):
        features = extract_features(file_path)
        features = features.reshape(1, -1)
        prediction = model.predict(features)
        prediction_probabilities = model.predict_proba(features)
        return prediction[0], prediction_probabilities
    print("3")
    # 데이터 읽기 및 특징 추출
    for word in words:
        #word_folder = os.path.join(data_folder, word)
        word_folder = data_folder
        print(word_folder)
        for file_name in os.listdir(word_folder):
            print(file_name)
            if word in file_name:
                print("11")
                file_path = os.path.join(word_folder, file_name)
                print(file_path)
                features = extract_features(file_path)
                print("12")
                X.append(features)
                y.append(word)
                print("13")
    print("4")
    # 데이터를 numpy 배열로 변환하여 저장
    X = np.array(X)
    y = np.array(y)
    print("5")
<<<<<<< HEAD
    print("X : ", X)
    print("y : ", y)
=======
    print(y)
>>>>>>> d07e9fd8a1867babdd2c1549bcb451e5297a79fa
    np.save(output_x_path, X)
    np.save(output_y_path, y)
    print("6")

    return "Feature extraction completed successfully"
