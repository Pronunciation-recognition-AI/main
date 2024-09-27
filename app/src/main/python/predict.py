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
        print("extract_features 함수 실행")
        y, sr = librosa.load(file_path, sr=None)
        mfccs = librosa.feature.mfcc(y=y, sr=sr, n_mfcc=13)
        return np.mean(mfccs.T, axis=0)

    model_path = os.path.join(data_folder, "model.pkl")
    model = joblib.load(model_path)
    print("모델이 성공적으로 불러와졌습니다.")

    test_files = ['hello_test.wav', 'thank_you_test.wav', 'no_test.wav', 'yes_test.wav']

    for test_file in test_files:
        file_path = os.path.join(data_folder, test_file)
        predicted_word, probabilities = predict_word(file_path)
        print(f'파일: "{test_file}"의 예측된 단어: {predicted_word}')
        for word, prob in zip(model.classes_, probabilities[0]):
            print(f'    단어: "{word}" 확률: {prob * 100:.2f}%')

    return "음성 인식 결과는 여기에 표시"