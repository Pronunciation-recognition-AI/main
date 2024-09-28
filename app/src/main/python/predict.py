import os
import librosa
import numpy as np
import joblib

def main(data_folder):
    # 새 음성 파일 예측
    def predict_word(file_path):
        features = extract_features(file_path)
        features = features.reshape(1, -1)
        prediction_probabilities = model.predict_proba(features)
        predicted_word = model.classes_[np.argmax(prediction_probabilities)]
        return predicted_word, prediction_probabilities

    # 특징 추출 함수
    def extract_features(file_path):
        y, sr = librosa.load(file_path, sr=None)
        mfccs = librosa.feature.mfcc(y=y, sr=sr, n_mfcc=13)
        return np.mean(mfccs.T, axis=0)

    model_path = os.path.join(data_folder, "model.pkl")
    model = joblib.load(model_path)

    test_file = 'test.wav'
    file_path = os.path.join(data_folder, test_file)
    predicted_word, probabilities = predict_word(file_path)

    for word, prob in zip(model.classes_, probabilities[0]):
        print(f'    단어: "{word}" 확률: {prob * 100:.2f}%')

    return predicted_word