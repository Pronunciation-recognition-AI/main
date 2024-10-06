import json
import time
import firebase_admin
from firebase_admin import credentials, db, storage
import random
import subprocess
from fastapi import FastAPI
import whisper
import sys
from safetensors.flax import load_file
import os
import numpy as np
import evaluate
import torch
import torchaudio
import csv
from datasets import load_dataset
from transformers import WhisperTokenizer, WhisperFeatureExtractor, WhisperForConditionalGeneration, Seq2SeqTrainer, \
    Seq2SeqTrainingArguments, WhisperProcessor, DataCollatorWithPadding, EarlyStoppingCallback
from scipy.io.wavfile import read
from collator import DataCollatorSpeechSeq2SeqWithPadding
from pprint import pprint

app = FastAPI()

# Firebase Admin SDK 초기화
cred = credentials.Certificate("speety-4d4e1-firebase-adminsdk-osorm-f6bd83a079.json")
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://speety-4d4e1-default-rtdb.asia-southeast1.firebasedatabase.app/',
    'storageBucket': 'speety-4d4e1.appspot.com'
})

# Firebase Realtime Database 경로 참조
ref = db.reference('signals')


# Firebase에서 신호 수신 시 파일 다운로드
@app.on_event("startup")
async def startup_event():
    # Firebase Database에서 신호를 감시하는 리스너 등록
    ref.listen(lambda event: handle_trigger(event))


def handle_trigger(event):
    time.sleep(3)
    data = event.data
    if data and data.get("action") == "study":
        user_id = data.get("userId")
        if user_id:
            download_files(user_id)

    elif data and data.get("action") == "speech":
        print("speech")
        user_id = data.get("userId")
        message = "Server Send"
        download_speech_test_file(user_id)


def download_files(user_id):
    bucket = storage.bucket()
    blobs = bucket.list_blobs(prefix=f"audio/{user_id}/train")
    local_directory = f"firebase/{user_id}/train"

    if not os.path.exists(local_directory):
        os.makedirs(local_directory)
        print("train Dir create")

    for blob in blobs:
        if blob.name.endswith('.wav'):  # 오디오 파일만 다운로드
            local_path = f"firebase/{user_id}/train/{blob.name.split('/')[-1]}"  # 로컬 경로 설정
            print(f"Downloading {blob.name} to firebase")
            blob.download_to_filename(local_path)
            print(f"{blob.name} audio_file {local_path} downloaded successfully.")

    convert_to_16kHz(local_directory)
    json_path_edit(local_directory)
    split_train_test_json(local_directory)
    whisper_training(local_directory)
    send_train_result_to_firebase(user_id)


def download_speech_test_file(user_id):
    bucket = storage.bucket()
    local_directory = f"firebase/{user_id}/test"

    if not os.path.exists(local_directory):
        os.makedirs(local_directory)

    # 디렉토리에 파일이 있으면 모두 삭제
    for filename in os.listdir(local_directory):
        file_path = os.path.join(local_directory, filename)
        try:
            if os.path.isfile(file_path):
                os.remove(file_path)
                print(f"Deleted {file_path}")
        except Exception as e:
            print(f"Error deleting file {file_path}: {e}")

    blob = bucket.blob(f"audio/{user_id}/test/test.wav")
    local_path = f"firebase/{user_id}/test/{blob.name.split('/')[-1]}"

    print(f"Downloading {blob.name} to firebase")
    blob.download_to_filename(local_path)
    print("Download Complete")
    convert_to_16kHz(local_directory)
    run_whisper(user_id)


def send_train_result_to_firebase(user_id):
    ref = db.reference(f'users/{user_id}/train')
    ref.set(1)


def send_signal_to_firebase(user_id, message):
    print(str(time.time()))
    signal_data = {
        "message": message,
    }

    # Firebase에 user_id에 맞는 경로로 신호를 저장
    user_ref = ref.child(user_id)  # signals/{user_id}
    user_ref.set(signal_data)  # 데이터를 해당 사용자 ID 아래에 저장


def run_whisper(user_id):
    model = whisper.load_model("small")
    fine_tuned_model_path = f"firebase/{user_id}/train/results/model.safetensors"
    if os.path.exists(fine_tuned_model_path):
        state_dict = load_file(fine_tuned_model_path)

        # 파인튜닝된 가중치를 Whisper 모델에 적용
        model.load_state_dict(state_dict, strict=False)

    # 오디오 파일이 있는 폴더 경로
    audio_file = f"firebase/{user_id}/test/test.wav"

    if os.path.exists(audio_file):
        print(f"Processing {audio_file}...")

        # 오디오 파일 텍스트 변환
        result = model.transcribe(audio_file, language='ko')  # 한국어(Korean)로 설정

        # 결과 텍스트 출력
        print(f"Transcription for {audio_file}:")
        print(result['text'])
        if result['text'] == "":
            print("암것도 안나옴")
            send_signal_to_firebase(user_id, "No return")
        else:
            print("뭐 있음")
            send_signal_to_firebase(user_id, result['text'])

        # 파일 처리 후 파일 삭제
        try:
            os.remove(audio_file)
            print(f"Deleted {audio_file}.")
        except Exception as e:
            print(f"Error deleting file {audio_file}: {e}")
    else:
        print(f"Audio file {audio_file} not found.")


def convert_to_16kHz(local_directory):
    print("conver_to_16kHz Start")
    input_dir = local_directory
    # 입력 디렉토리의 모든 파일 확인
    for filename in os.listdir(input_dir):
        if filename.endswith(".wav"):
            input_path = os.path.join(input_dir, filename)
            temp_output_path = os.path.join(input_dir, "temp_" + filename)  # 임시 파일 경로

            # ffmpeg 명령어를 사용해 16kHz로 변환
            command = [
                'ffmpeg', '-i', input_path, '-ar', '16000', temp_output_path
            ]

            # ffmpeg 실행
            subprocess.run(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            print(f"Temp file convert complete: {temp_output_path}")

            # 변환 완료 후, 원본 파일을 삭제하고 임시 파일을 원본 파일 이름으로 변경
            os.remove(input_path)  # 원본 파일 삭제
            os.rename(temp_output_path, input_path)  # 임시 파일을 원본 이름으로 변경
            print(f"File paste complete: {input_path}")


def json_path_edit(local_directory):
    print("json_path_edit Start")
    json_file_path = "firebase/data_transcripts.json"
    audio_directory = local_directory
    new_json_file_path = local_directory + "/data_transcripts.json"

    with open(json_file_path, "r", encoding='utf-8') as f:
        json_data = json.load(f)

    num = 1
    for audio_file in json_data['audio_files']:
        num_str = str(num)
        old_path = audio_file['path']
        new_path = local_directory + "\\chunk" + num_str + ".wav"
        audio_file['path'] = new_path
        num += 1

    with open(new_json_file_path, "w", encoding='utf-8') as f:
        json.dump(json_data, f,ensure_ascii=False, indent=4)


def split_train_test_json(local_directory):
    print("split_train_test_json Start")
    json_file_path = local_directory + "/data_transcripts.json"
    train_path = local_directory + "/train.json"
    test_path = local_directory + "/test.json"
    train_ratio = 0.8
    with open(json_file_path, "r", encoding='utf-8') as f:
        json_data = json.load(f)

    audio_files = json_data.get("audio_files")
    random.shuffle(audio_files)

    split_index = int(len(audio_files) * train_ratio)

    train_data = {"audio_files": audio_files[:split_index]}
    test_data = {"audio_files": audio_files[split_index:]}

    with open(train_path, 'w', encoding='utf-8') as f:
        json.dump(train_data, f, ensure_ascii=False, indent=4)

    # test.json 저장
    with open(test_path, 'w', encoding='utf-8') as f:
        json.dump(test_data, f, ensure_ascii=False, indent=4)

    print("train.json, test.json completely saved")


def whisper_training(local_directory):

    data_files = {
        "train": f"{local_directory}/train.json",  # 학습 데이터
        "test": f"{local_directory}/test.json"  # 평가 데이터
    }

    # Load dataset from JSON file
    dataset = load_dataset("json", data_files=data_files)


    def flatten_audio_files(batch):
        paths = []
        transcriptions = []
        for sublist in batch["audio_files"]:
            for item in sublist:
                print(item["path"])
                print(item["transcription"])
                paths.append(item["path"])
                transcriptions.append(item["transcription"])
        print(paths)
        print(transcriptions)
        return {"path": paths, "transcription": transcriptions}

    # JSON의 audio_files 필드에서 path와 transcription 필드를 추출
    dataset = dataset.map(flatten_audio_files, batched=True, remove_columns=["audio_files"])
    print(dataset)
    # 2. Whisper 모델 및 한국어 지원 토크나이저 로드
    model_name = "openai/whisper-small"
    tokenizer = WhisperTokenizer.from_pretrained(model_name, language="ko", task="transcribe")
    feature_extractor = WhisperFeatureExtractor.from_pretrained(model_name)
    processor = WhisperProcessor.from_pretrained(model_name, language="ko", task="transcribe")

    model = WhisperForConditionalGeneration.from_pretrained(model_name)

    data_collator = DataCollatorSpeechSeq2SeqWithPadding(
        processor=processor,
        decoder_start_token_id=model.config.decoder_start_token_id,
    )


    def enforce_fine_tune_lang():
        model.config.suppress_tokes = []
        model.generation_config.suppress_tokens = []
        model.config.forced_decoder_ids = processor.tokenizer.get_decoder_prompt_ids(language="korean", task="transcribe")
        model.generation_config.forced_decoder_ids = processor.tokenizer.get_decoder_prompt_ids(language="ko", task="transcribe")

    # 3. 데이터 전처리 함수 정의
    def preprocess(batch):
        # 음성 파일 경로로부터 오디오를 로드
        audio = batch["path"]
        _, data = read(audio)
        audio_array = np.array(data, dtype=np.float32)
        batch["input_features"] = feature_extractor(audio_array, sampling_rate=16000).input_features[0]
        batch["labels"] = tokenizer(batch["transcription"]).input_ids

        return batch

    def compute_metrics(pred):
        metric = evaluate.load("cer")
        pred_ids = pred.predictions
        label_ids = pred.label_ids
        label_ids[label_ids == -100] = tokenizer.pad_token_id
        pred_str = tokenizer.batch_decode(pred_ids, skip_special_tokens=True)
        label_str = tokenizer.batch_decode(label_ids, skip_special_tokens=True)
        cer = 100 * metric.compute(predictions=pred_str, references=label_str)
        return {"cer": cer}


    # 전처리 적용
    processed_dataset = dataset.map(preprocess, remove_columns=dataset.column_names["train"])
    processed_test_dataset = dataset.map(preprocess, remove_columns=dataset.column_names["test"])

    print("done")
    print(processed_dataset)
    print(processed_test_dataset)


    training_args = Seq2SeqTrainingArguments(
        output_dir="./results",  # change to a repo name of your choice
        num_train_epochs=1,
        per_device_train_batch_size=16,
        gradient_accumulation_steps=2,  # increase by 2x for every 2x decrease in batch size
        learning_rate=1e-5,
        warmup_steps=100,
        # max_steps=5000,
        gradient_checkpointing=True,
        fp16=True,
        evaluation_strategy="steps",
        per_device_eval_batch_size=8,
        predict_with_generate=True,
        generation_max_length=225,
        save_steps=100,
        eval_steps=100,
        logging_steps=50,
        # report_to=["tensorboard"],
        load_best_model_at_end=True,
        metric_for_best_model="cer",
        greater_is_better=False,
        push_to_hub=False,
        remove_unused_columns=False,
        weight_decay=0.01,
    )

    trainer = Seq2SeqTrainer(
        args=training_args,
        model=model,
        train_dataset=processed_dataset["train"],
        eval_dataset=processed_test_dataset["test"],
        data_collator=data_collator,
        compute_metrics=compute_metrics,
        tokenizer=processor.feature_extractor,
        callbacks=[EarlyStoppingCallback(early_stopping_patience=2)],
    )

    print("2")
    # 5. 모델 학습
    trainer.train()
    print(f"training_args : {training_args}")
    # 6. 모델 저장
    save_dir = f"{local_directory}/results"
    trainer.save_model(save_dir)
    print("test fine_tuned_model")
    result_dic = trainer.evaluate(eval_dataset=processed_test_dataset["test"])
    pprint(result_dic)
    filename = "evaluate_results.csv"
    file_path = save_dir + "/" + filename

    # 파일을 쓰기 모드로 열고 csv.writer 사용
    with open(file_path, 'w', newline='') as csvfile:
        writer = csv.writer(csvfile)
        for key, value in result_dic.items():
            sentence = key + ": " + str(value)
            writer.writerow([sentence])


@app.get("/")
async def root():
    return {"message": "Listening for Firebase signals"}