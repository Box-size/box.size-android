
![boxDotSizeLogo](https://github.com/Box-size/box.size-android/assets/59639035/2c3eae6f-78ad-41e8-a8ba-585b75f3714b)

CJ 대한통운에서 주관하는 [CJ 미래기술 챌린지 2023](https://docs.google.com/forms/d/e/1FAIpQLSeKMHsCu9V0t2odyPmptMsHXKA0agPF-ZRqJ2v7rSo7KIgWaA/viewform) 에 제출한 어플리케이션의 소스코드입니다.

**'스마트폰 활용한 상품 체적측정'** 이 주제였으며, 예선 통과 후 본선까지 진출하였습니다.

---

## ✨Overview

<p align="center">
  <img src="https://github.com/Box-size/box.size-android/assets/59639035/524137cc-6928-439f-a0ab-80698b12ce15" width=500px/>
</p>

- 2023.7.7. ~ 2023.9.4.
- Box.size는 스마트폰 자원만을 활용하여 박스 크기를 측정하는 안드로이드 어플리케이션입니다.
- 해당 어플리케이션은 두 가지 경우에서 박스 체적 측정을 지원합니다.
    1. 박스 하나를 단일 측정 (과제1)
    2. 영상을 통해 일정 속도로 이동하는 여러개의 박스 측정 (과제2)

<br>


### Skill 🛠️

![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84.svg?style=for-the-badge&logo=android-studio&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
<img src="https://img.shields.io/badge/RXJava-B7178C?style=for-the-badge&logo=ReactiveX&logoColor=white">


![Python](https://img.shields.io/badge/python-3670A0?style=for-the-badge&logo=python&logoColor=ffdd54)
![OpenCV](https://img.shields.io/badge/opencv-%23white.svg?style=for-the-badge&logo=opencv&logoColor=white)
![TensorFlow](https://img.shields.io/badge/TensorFlow-%23FF6F00.svg?style=for-the-badge&logo=TensorFlow&logoColor=white)
<img src="https://img.shields.io/badge/ONNX-005CED?style=for-the-badge&logo=ONNX&logoColor=white">


<br>

### How to use 👀

- 사용 전 반드시 '테스트'버튼을 눌러 카메라 캘리브래이션 과정을 진행해야합니다.
- 이후 필요에 따라 '사진으로 촬영' 혹은 '영상으로 촬영'을 눌러 카메라 체적 측정을 시작합니다.
- '사진으로 촬영'의 경우 한장의 사진 속에 포함된 하나의 박스 체적을 측정합니다.
- '영상으로 촬영'의 경우 지정한 ms 간격으로 사진을 촬영하여 각 촬영 마다 박스 하나의 체적을 측정합니다.
- 측정결과는 '측정 기록'을 통해 확인 할 수 있습니다.
  
---

<br>

## 📐체적 측정 알고리즘

- 아래 기본 원리를 기반으로 알고리즘을 설계하였습니다. $$카메라의 초점거리 : 실제 거리 = 사진 상 박스 크기 : 실제 박스크$$
- 카메라의 초점 거리 등 내부/외부 파라미터를 구하기 위해 **체커보드 패턴을 사용한 카메라 캘리브레이션**을 활용 하였습니다.
- OpenCV를 사용하여 배경제거, 윤곽선검출 등으로 전처리를 진행하고 박스의 꼭짓점을 계산하였습니다.
 <p align="center"><img src="https://github.com/Box-size/box.size-android/assets/59639035/6ddcf7f0-5a65-48e4-81f8-723bb9499f23" width=50%> <img src="https://github.com/Box-size/box.size-android/assets/59639035/7f26d075-d876-4a54-8613-ccb568be5e42" width=48%>  </p> 
 
- 실제 박스 최하단 꼭지점을 월드 좌표로 구하기 위해 아래 공식을 활용하였습니다. $$P=C _w +k(p _w −C_w​ )$$
- 이후 얻어진 결과를 활용하여 실제 박스의 체적을 도출하였습니다.

<br>

## 📱안드로이드 앱

|<img src="https://github.com/Box-size/box.size-android/assets/59639035/fbb1b4fc-5c0b-4f00-a7e6-2e4aad25b401" width=50%>| ![image](https://github.com/Box-size/box.size-android/assets/59639035/e365254e-812a-4d71-ab33-d4dc958c3318) |![task1](https://github.com/Box-size/box.size-android/assets/59639035/38075cc0-46e6-40aa-bf0d-7b60d38afcf1)|![task2](https://github.com/Box-size/box.size-android/assets/59639035/6c0da5f6-22bc-427b-8dd1-fea9fb4cbeae)|![results](https://github.com/Box-size/box.size-android/assets/59639035/d3b9441e-9fce-490d-97ca-823cc8093eb5)|
|:---:|:---:|:---:|:---:|:---:|
|홈|카메라 <br> 캘리브레이션|사진 촬영으로 분석|연속 촬영으로 분석|기록 확인|

- CameraX, Camera2 를 활용하여 카메라 하드웨어 기능을 제어하였습니다.
- RxJava를 활용하여 비동기 처리 및 데이터 플로우를 제어하였습니다.
- ml kit에 박스 이미지 학습 모델을 활용하여 박스 이미지만을 크롭하고 해당 이미지를 일부 전처리하였습니다.
- Room을 활용하여 내부 DB에 측정 결과(측정시간, 결과, 과제번호, 원본이미지 uri, 크롭된 이미지 uri)를 저장합니다. <br> 해당내용은 '측정 기록'에서 확인할 수 있습니다.

<img src="https://github.com/Box-size/box.size-android/assets/59639035/cdff25f4-7ded-45e4-86b6-9d24335e8c8c" width= "60%">


<br>

## Team 👥

|팀원|역할|
|:---:|---|
|<img src="https://github.com/ssigner.png" width="100"><br>[김종훈(팀장)](https://github.com/ssigner)|카메라 캘리브레이션 및 알고리즘 개발|
|<img src="https://github.com/jagaldol.png" width="100"><br>[안혜준(알고리즘)](https://github.com/jagaldol)|인공지능 및 알고리즘 개발|
|<img src="https://github.com/gogumac.png" width="100"><br>[김유빈(AOS)](https://github.com/gogumac)|ML kit 객체 인식 및 카메라, DB 등 안드로이드 어플리케이션 기능 개발|
|<img src="https://github.com/jihoon5916.png" width="100"><br>[김지훈(알고리즘)](https://github.com/jihoon5916)|알고리즘 개발|



<!-- ## 디렉토리 구조 및 파일

<img src="https://github.com/Box-size/box.size-android/assets/59639035/e2581dd7-756b-4040-984e-e67219b5b7a3" width = 300px/>

### /retrofit : 서버통신 관련 파일 ~> 앱으로 이전 이후 없앨 예정

### /room : DB관련 파일 

### /ui : UI관련 파일

### 나머지 중 연결부!

- TestInteractor.kt : 테스트 요청 및 결과 처리 (지금은 네트워크 통신 결과 처리) -> 분석 시점은 주석에써둠
- BoxAnalyzeInteractor.kt : 박스 분석 요청 및 결과 처리 (지금은 네트워크 통신 결과 처리) -> 분석 시점은 주석에써둠


## 데이터 플로우

프래그먼트(UI)-> ***Interactor.kt(분석 요청) -> 분석 파일

분석완료 -> ***Interactor.kt(결과 처리) -> 프래그먼트(UI) -->








