# Clicker000
![Clicker000썸네일](https://github.com/samgashyeong/Clicker000/assets/66434787/1e3b7843-e66a-4b67-b61f-66ae2c66a35a)


## 설명
1. 세계 각지에서 요요 대회가 열리며, 선수들과 경기 관람자들이 경기 영상을 채점하는 문화가 자리 잡았지만, 이를 지원하는 플랫폼이 없어 **대부분의 사람들은 경기 영상을 틀어놓고 머리속으로 채점하거나 계수기를 사용하여 불편함을 겪고 있습니다.** 이에 따라 경기 영상을 간편하게 채점할 수 있는 앱을 제작하게 되었습니다.

2. clicker000의 목표는 **그 누구나 간단하고 쉽고 빠르게** 요요 경기 영상들을 채점하고 채점한 데이터들을 기반으로 분석을 할 수 있는 것을 목표로 하고 있습니다.

3. github-release에서 앱을 배포하고 있으며, 현재 인스타그램 [@clicker._.000](https://www.instagram.com/clicker._.000/)에서 사용자들에게 업데이트 내역을 알리고 있습니다.

## 업데이트 내역
- 베타테스트 1차(2024.02.12)
  - clicker000 베타테스트 버전 출시
- 베타테스트 2차(2024.04.30)
   -  가점/감점 이벤트 시 진동설정
   -  가로화면 채점 지원
   -  가점/감점 좌우반전 지원
   -  각종 버그 해결
- 랭킹모드 출시(2024.08.03)
    - 세계요요선수권대회 기념으로 대회를 관람하면서 채점하고 채점한 데이터를 기반으로 대략적인 순위를 알 수 있는 기능 출시
- 1.0.1 업데이트(2024.09.04)
    - 영상을 가져올 시 시작점 설정 유무를 설정에서 조작 가능
    - 외부저장소 백업 지원
    - 각종 버그 해결
- 1.0.2 업데이트(2024.09.30)
    - 실시간 차트 퍼포먼스 향상
    - 화면을 가로로 돌렸을 때 이전에 채점되던 영상이 나오는 버그 수정
- 1.0.3 업데이트(2025.08.03)
    - 메인화면 UI 리뉴얼
       - 모드 변경을 바로 할 수 있는 토글버튼 추가
       - 이제 동영상 저장없이 분석 화면 진입 가능
    - 분석 화면 리뉴얼
       - 플러스 마이너스 점수에 대한 애니메이션 추가
       - 툴바 UI 변경
## 기술스택
| Title | Content |
| ------------ | -------------------------- |
| Language | Kotlin |
| Architecture | MVVM  |
| AAC | Data binding, ViewModel, LiveData
| Dependency Injection | Hilt  |
| Network | Retrofit, OkHttp  |
| Local Storage | Room, DataStore |
| Asynchronous Processing | Coroutine(+ Flow)  |
| Third Party Library | Glide ,[Youtube Player](https://github.com/PierfrancescoSoffritti/android-youtube-player), [FilePicker](https://github.com/Atwa/filepicker), [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)  |
