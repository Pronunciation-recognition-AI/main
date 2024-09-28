import os
import librosa
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.svm import SVC
from sklearn.preprocessing import StandardScaler
from sklearn.pipeline import make_pipeline
import joblib

# 음성 데이터를 저장한 폴더
def run_feature_extraction(data_folder):
    words = ['안녕하세요', '감사합니다', '네', '아니요']

    # 데이터를 저장할 리스트
    X = []
    y = []

    # 특징 추출 함수
    def extract_features(file_path):
        y, sr = librosa.load(file_path, sr=None)
        mfccs = librosa.feature.mfcc(y=y, sr=sr, n_mfcc=13)
        return np.mean(mfccs.T, axis=0)

    # 데이터 읽기 및 특징 추출
    for word in words:
        for file_name in os.listdir(data_folder):
            if word in file_name:
                file_path = os.path.join(data_folder, file_name)
                features = extract_features(file_path)
                X.append(features)
                y.append(word)

    # 데이터를 numpy 배열로 변환하여 저장
    X = np.array(X)
    y = np.array(y)

    # 학습 데이터와 테스트 데이터 분리
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    # 모델 학습
    model = make_pipeline(StandardScaler(), SVC(kernel='linear', probability=True))
    model.fit(X_train, y_train)

    # 모델 평가
    accuracy = model.score(X_test, y_test)

    model_path = os.path.join(data_folder, "model.pkl")

    # 학습된 모델을 저장
    joblib.dump(model, model_path)

    return f'모델 정확도: {accuracy * 100:.2f}%'






