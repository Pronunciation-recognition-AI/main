# PROGMATISM Project

---

- 화면 구성

## **1. 홈 화면 (HomeActivity)**

### 구성
- 상단에 "스피티"라는 텍스트 로고가 위치하며, 그 아래에 두 개의 버튼이 화면 중앙에 가로로 배치됩니다:
    - **음성 인식 버튼**: 음성 인식 기능을 제공하는 화면으로 이동
    - **학습하기 버튼**: 학습 기능을 제공하는 화면으로 이동

### 기능 추가 예정
- **음성 인식 버튼**: 클릭 시 `SpeechActivity`로 이동
- **학습하기 버튼**: 클릭 시 `LearningActivity`로 이동

---

## **2. 음성 인식 화면 (SpeechActivity)**

### 구성
- **텍스트 인식 결과 표시 영역**: 음성 인식 결과를 보여줌
- **녹음 버튼**: "녹음 중..." 메시지를 출력하고, 3초 후 "인식 완료!"라는 텍스트가 표시됨
- **TTS 버튼**: 클릭 시 아직 구현되지 않은 TTS 기능에 대한 알림 메시지 출력

### 기능 추가 예정
- **음성 인식**: 녹음 버튼 클릭 시 음성 인식을 실행하고, 결과를 텍스트로 표시
- **TTS (Text-to-Speech)**: TTS 기능을 추가하여 인식된 텍스트를 음성으로 변환하는 기능 구현 예정

---

## **3. 학습하기 화면 (LearningActivity)**

### 구성
- 학습 콘텐츠를 제공하는 화면 (현재는 구성 중)

### 기능 추가 예정
- **학습 콘텐츠 제공**: 학습할 수 있는 콘텐츠를 화면에 표시하고, 사용자가 학습할 수 있는 기능 추가
- **학습 완료 피드백**: 학습이 완료되면 피드백 메시지 제공

---

## **4. 로그인 화면 (LoginActivity)**

### 구성
- **이메일 입력 필드**
- **비밀번호 입력 필드**
- **로그인 버튼**: 클릭 시 사용자 인증을 시도
- **회원가입 버튼**: 아직 계정이 없는 사용자는 회원가입 화면으로 이동

### 기능 추가 예정
- **Firebase 로그인**: Firebase Authentication을 사용해 사용자 인증 구현
- **회원가입 화면 이동**: 회원가입 버튼 클릭 시 `RegisterActivity`로 이동

---

## **5. 회원가입 화면 (RegisterActivity)**

### 구성
- **이메일, 비밀번호 입력 필드**
- **회원가입 버튼**: 입력된 정보로 Firebase에서 사용자 계정 생성

### 기능 추가 예정
- **Firebase 회원가입**: Firebase Authentication을 통해 계정 생성
- **프로필 이미지 업로드**: 사용자 프로필 이미지를 업로드하는 기능 추가 예정

---

## **6. Firebase 통합**

### 기능
- **Firebase Authentication**: 사용자 로그인 및 회원가입 기능 구현
- **Firebase Realtime Database**: 사용자 정보 및 학습 기록 저장
- **Firebase Storage**: 사용자 프로필 이미지 저장 (추가 예정)

---

## **프로젝트 구조**

```bash
/app
  /src
    /main
      /java/com/example/hackathon_project
        HomeActivity.kt
        SpeechActivity.kt
        LearningActivity.kt
        LoginActivity.kt
        RegisterActivity.kt
      /res
        /layout
          activity_home.xml
          activity_speech.xml
          activity_learning.xml
          activity_login.xml
          activity_register.xml
        /drawable
          logo.png