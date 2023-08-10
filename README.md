# box.size - android
스마트폰 카메라를 이용해 박스 크기를 측정하는 안드로이드 어플리케이션

## 디렉토리 구조 및 파일

<img src="https://github.com/Box-size/box.size-android/assets/59639035/e2581dd7-756b-4040-984e-e67219b5b7a3" width = 300px/>

### /retrofit : 서버통신 관련 파일 ~> 앱으로 이전 이후 없앨 예정

### /room : DB관련 파일 

### /ui : UI관련 파일

### 나머지 중 연결부!

- TestInteractor.kt : 테스트 요청 및 결과 처리 (지금은 네트워크 통신 결과 처리) -> 분석 시점은 주석에써둠
- BoxAnalyzeInteractor.kt : 박스 분석 요청 및 결과 처리 (지금은 네트워크 통신 결과 처리) -> 분석 시점은 주석에써둠


## 데이터 플로우

프래그먼트(UI)-> ***Interactor.kt(분석 요청) -> 분석 파일

분석완료 -> ***Interactor.kt(결과 처리) -> 프래그먼트(UI)








