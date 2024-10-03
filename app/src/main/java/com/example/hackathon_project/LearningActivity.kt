package com.example.hackathon_project

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chaquo.python.Python
import java.io.FileOutputStream
import java.io.IOException
import com.chaquo.python.android.AndroidPlatform
import java.io.File
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class LearningActivity : AppCompatActivity() {

    private lateinit var wordTextView: TextView
    private lateinit var fileCountTextView: TextView
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button
    private lateinit var btnRecord: Button
    private lateinit var btnLearn: Button

    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    // 낱말 리스트
    private val words = listOf("나는 바지를 입고 단추를 채웁니다.", "책상 위에 가방이 있습니다.", "가방에 사탕과 연필을 넣을 거예요.", "아빠와 자동차를 타고 동물원에 갑니다.", "잘 다녀와 하면서 엄마가 뽀뽀를 해줍니다.", "동물원에는 호랑이가 꼬리를 늘어뜨리고 있습니다.", "나는 코끼리에게 땅콩을 줍니다.", "코끼리는 귀가 아주 큽니다.", "나는 동물원 놀이터에서 그네를 탑니다.", "아빠가 토끼 풍선을 사왔습니다.", "로봇 그림을 구경합니다.", "그림은 못 두 개에 걸려 있습니다.", "로봇은 긴 눈썹 괴물과 싸움을 합니다.", "나무에는 참새 세 마리가 짹짹 거리고 나무 아래 풀밭에는 메뚜기가 있습니다.", "엄마에게 전화를 합니다.", "엄마 동물원 재미있어요.", "문 열어.", "문 열어?", "나무가 많아.", "나무가 많아?", "내일 만나.", "내일 만나?", "이제 내려.", "이제 내려?", "머리 말려.", "머리 말려?", "여기 놔요.", "여기 놔요?", "나비가 날아요.", "나비가 날아요?", "엄마가 노래해.", "엄마가 노래해?", "머리 말려요.", "머리 말려요?", "여기 놨어요.", "여기 놨어요?", "여기 놔.", "여기 놔?", "여기 놔요.", "여기 놔요?", "머리 말려.", "머리 말려?", "머리 말려요.", "머리 말려요?", "여기 놨어요.", "여기 놨어요?", "머리 말렸어요.", "머리 말렸어요?", "여기 놓을 거예요.", "여기 놓을 거예요?", "밥 먹었죠.", "밥 먹었죠?", "집에 아무도 없겠지.", "집에 아무도 없겠지?", "발이 얼었어요.", "발이 얼었어요?", "길이 많이 막혀요.", "길이 많이 막혀요?", "선생님이 하셨어요.", "선생님이 하셨어요?", "거기 갈래.", "거기 갈래?", "그렇게 될 리가 없지.", "그렇게 될 리가 없지?", "전화 왔어요.", "전화 왔어요?", "여기까지 걸어 왔어요.", "여기까지 걸어 왔어요?", "방이 추운 거 같애.", "방이 추운 거 같애?", "날씨가 추워요.", "날씨가 추워요?", "음악 켜.", "음악 켜?", "날씨가 더워요.", "날씨가 더워요?", "백화점에 가서 목걸이와 반지를 샀습니다.", "휴지를 버려 주세요.", "우체국은 병원 앞에 있어요.", "약을 하루에 두 번씩 드세요.", "감기에 걸리지 않도록 조심해라.", "내가 퇴근하는 시간은 항상 같다.", "물이 차다.", "오늘처럼 눈이 오는 날은 조심해서 운전해야 한다.", "당신이 만든 작품을 설명해 보세요.", "당근은 무슨 색입니까?", "커피를 마셔서 잠이 오지 않는다.", "여기에 기다리세요.", "전화번호가 어떻게 됩니까?", "오래된 음식을 먹지 마세요.", "눈이 내리는 겨울이 빨리 왔으면 좋겠습니다.", "아침에 안개가 껴서 앞을 보기가 힘들어요.", "수박이 달다.", "일을 마치고 갈 시간이 충분합니다.", "내가 읽은 책을 너한테 빌려줄게.", "음악을 들으며 운동해 보세요.", "청소를 하다가 내가 잃어버렸던 반지를 찾았다.", "해가 뜬다.", "주소를 알려 주세요.", "좋아하는 음식이 뭡니까?", "외투와 장갑을 벗고 들어와요.", "경찰이 이 곳으로 곧 온다고 했어요.", "길이 너무 막혀서 약속에 늦었습니다.", "편식을 하는 습관은 건강에 해로워요.", "한 줄로 똑바로 서 주십시오.", "냉면을 먹으러 식당에 갑니다.", "사진을 찍어 주세요.", "저녁에 무엇을 먹을까?", "배가 고프다.", "열쇠가 없어서 문을 열지 못했다.", "어두운 곳에서 책을 읽지 마세요.", "버스에 사람이 많아서 내가 앉을 자리가 없다.", "냉장고에서 두부와 야채를 꺼냅니다.", "매일 강아지를 데리고 산책을 간다.", "차가 내뿜는 연기 때문에 하늘이 뿌옇다.", "숫자가 적혀있는 카드를 골라보세요.", "화장실은 어디에 있습니까?", "빨간 불에 건너가지 마십시오.", "밥을 먹자.", "칭찬을 받는 것은 기분 좋은 일이다.", "우리 가족은 모두 여섯 명입니다.", "일을 빨리 끝내고 집으로 갈께요.", "창문은 열어 주세요.", "가을이 오면 나무에 단풍이 든다.", "비가 내린 길은 미끄러워서 매우 위험하다.", "우리가 이겼다는 소식을 듣고 감격했다.", "설명서를 읽어보고 사용하세요.", "신발을 벗어 주세요.", "회사 앞에서 일곱 시에 만납시다.", "주차장은 지하에 있습니다.", "옷이 크다.", "나는 고추가 들어간 음식은 맵지만 좋아한다.", "냉장고가 고장나서 수리를 맡겼습니다.", "우표 한 장은 얼마입니까?", "물건을 훔친 도둑을 경찰이 차에 태웠다.", "쓰레기를 분리하는 것을 잊지 마세요.", "아기 옷을 벗기고 입히면서 빗으로 머리도 빗겨 주었다.", "밝은 빛으로 곱하기 공부를 하다가 급하게 책 한 권을 읽었다.", "큰 빚을 져서 맛있는 찜닭도 못 먹고 밤낮으로 뛰어다녔다.", "사기그릇 가게로부터 사기를 당했다는 걸 알고 그들은 사기를 떨어졌다.", "서른여덟의 김유신은 권력을 이용해 불법으로 생산라인을 개조하였다.", "소주와 김밥과 통닭을 즐겨 먹은 까닭에 수일 내에 돼지가 될 것 같다.", "원룸에 사니까 절약할 필요가 없어서 방을 밝게 한다.", "일요일날에는 안암 일 동에서 이 동으로 이동하는 것과 은근히 일하다.", "저 병에 든 약을 마시면 병에 차도가 있을 것이다.", "음운론을 가르치시던 담임선생님은 흙에서 넓죽한 고구마를 캐셨다.", "서울역에서 김연아를 보고 그 찬란한 모습에 넋이 나갔다.", "산기슭에 있는 장미꽃으로 장식을 하려다가 가시의 끝을 만졌다.", "늦여름이나 가을날에 상견례를 하려고 온라인으로 식당을 예약했다.", "넓게 지어진 연륙교가 효과적으로 제 몫을 다 해내고 있다.", "이 모 씨의 이모가 마침내 고소 철차를 밟게 되었다.", "의견 작성 시 띄어쓰기의 원칙에 주의해 주십시오.", "아들과 병원에 가보니 하필 늑막염이었다.", "애달픈 개처럼 해 질 녘까지 엄마를 기다렸다.", "어제는 허리가 아파서 거동이 불편했다.", "에누리 없이 게와 고등어를 파는 사람들을 헤아려 보았다.", "오전에 호루라기를 챙겨서 고사장으로 갔다.", "우리는 후식으로 과자를 먹으며 구름을 보았다.", "으름장을 놓아서 그를 흐느끼게 만들었다.", "이동할 때를 기다리며 히히덕거렸다.", "야생화 향기를 맡아보더니 고개를 약간 갸웃뚱거렸다.", "얘는 얘기꾼이라 걔보다 얘기를 잘해.", "여러가지 현악기가 결이 곱다.", "예금 상품의 혜택을 계산해보았다.", "와전된 과거의 일이 화를 불러왔다.", "왜가리 괘씸하게도 횃대를 부러뜨렸다.", "외삼촌은 금융업 분야의 회식을 괴로워했다.", "요즘 교사들 사이에서 효도 관광이 유행이다.", "워낙 권력을 좋아해서 훨씬 더 회장 자리에 집착했다.", "웨딩드레스에 대해 궤변을 늘어놓으며 훼방을 놓았다.", "위대한 귀농인을 만나기 위해서 휘파람을 불며 옷 입고 나갔다.", "유도부가 휴가지에서 귤을 사 왔다.", "의사는 축의금과 함께 희소식을 전했다.", "가평의 고추밭에서 기적적으로 구출되었다.", "까치는 꾸물거리다가 구멍 사이에 끼었다.", "나무에 널린 니트들이 누구의 것인지 궁금했다.", "다리 아래 사는 두더지는 땅을 디딜 수 없었다.", "따가운 자외선에 뚜껑이 검은 빛을 띠었다.", "라면을 먹으면서 솜이불 위에서 루마니아 축구 리그를 보았다.", "마침내 무서운 미로에서 빠져나왔다.", "바다에서 부드러운 바람을 맞으며 비빔밥을 먹었다.", "빠르게 뛰었다가 뿌리에 걸려서 발목을 삐었다.", "사다리 위에서 수다를 떨다 보니 시간이 다 되었다.", "싸다고 하는 씨감자로 죽을 쑤다 말았다.", "자전거 가게에서 주민 회의가 지금 진행 중이다.", "짜게 끓인 찌개를 쭈그려 앉아서 먹었다.", "차선을 넘나들며 치열하게 추격전을 벌였다.", "카메라와 쿠키를 들고 있는 남자가 가장 키가 크다.", "타조는 투명한 유리에서 칼날과 티끌을 발견했다.", "파란 눈과 하얀 피부 덕분에 첫인상이 푸근해보였다.", "하늘이는 후미진 골목 끝에서 히죽거렸다.", "무지개 일곱 색깔은 빨 주 노 초 파 남 보입니다.", "일 주일은 월 화 수 목 금 토 일요일입니다.", "설날은 일 월 일 일입니다.", "크리스마스는* 십 이 월 이십 오 일입니다.", "지금 계절은 가을입니다.", "어제는 날씨가 더웠고 오늘 날씨는 선선했다.", "일 년은 삼백 육십 오 일이고 일 주일은 칠 일입니다.", "하루는 이십 사 시간이고 일 분은 육십 초입니다.", "미영이랑 나연이는 단짝입니다.", "미영이와 나연이는 노래하며 놉니다.", "마루 위에 나란히 누워 낭랑히 노래합니다.", "나연이는 노래를 매우 많이 압니다.", "노래도 잘해서 미영이에게 알려 줍니다.", "미영이는 음악에 어울리는 안무를 마련합니다.", "어느 날 나연이는 미영이를 놀립니다.", "자기보다 노래를 못한다고 놀립니다.", "미영이는 남몰래 노래를 연마합니다.", "미영이의 능력이 나날이 늘어납니다.", "그래서 나연이는 더 이상 미영이를 놀릴 수 없게 되었습니다.", "나연이는 사과했고 둘은 다시 사이가 좋아졌습니다.", "나는 라면을 매우 좋아한다.", "생라면도 컵라면도 좋지만 뭐니 뭐니 해도 끓인 라면이 제일 좋다.", "양은냄비를 꺼 내 가스레인지 위에 올리고 물이 끓을 때까지 조리법을 읽는다.", "물이 보글보글 끓기 시작하면 면과 스프를 넣고 끓인다.", "계란 노른자가 익는 모습을 보고 있을 때가 가장 즐거운 순간이다.", "얼른 먹고 싶어서 군침을 꿀떡꿀떡 삼키면서도 조리 시간을 지키는 것이 나의 철칙이다.", "열과 성을 다해 만든 라면을 한 젓가락 먹으면 절로 미소가 난다.", "입에서 김이 호호 나오고 땀이 뻘뻘 나지만 젓가락질을 멈출 수가 없다.", "그야말로 무아지경에 빠지고 마는 것이다.", "남일이네 야옹이는 멍멍이를 미워합니다.", "야옹이는 멍멍이의 마음을 모릅니다.", "그래서 멍멍이랑 놀아주지 않습니다.", "은행나무 위에는 야옹이만 올라옵니다.", "무모한 멍멍이는 나무 위로 날아오릅니다.", "그렇지만 너무 높아서 오르기가 어렵습니다.", "야옹이는 매일매일 나무 위에 머무릅니다.", "위에서 얄미운 울음만 웁니다.", "나무 아래 누워있는 멍멍이는 무료합니다.", "야옹이는 야밤에만 아래로 내려옵니다.", "우울한 멍멍이는 애먼 나를 원망합니다.", "우리나라의 가을은 참으로 아름답다.", "무엇보다도 산에 오를 땐 더욱 더 그 빼어난 아름다움이 느껴진다.", "쓰다듬어진 듯한 완만함과 깎아 놓은 듯한 뾰족함이 어우러진 산등성이를 따라 오르다 보면 절로 감탄을 금할 수가 없게 된다.", "붉은 색 푸른색 노란색 등의 여러 가지 색깔들이 어우러져 타는 듯한 감동을 주며 나아가 신비롭기까지 하다.", "숲 속에 누워서 하늘을 바라보라.", "쌍쌍이 짝지어져 있는 듯한 흰 구름 높고 파란 하늘을 쳐다보고 있노라면 과연 예부터 가을을 천고마비의 계절이라 일컫는 이유를 알게 될 것만 같다.", "가을에는 또한 오곡백과 등 먹거리가 풍성하기 때문에 결실의 계절이라고도 한다.", "햅쌀 밤 호두뿐만 아니라 대추 여러 가지 떡 크고 작은 과일들을 맛볼 수 있는데 가을의 대표적인 명절인 추석에 우리는 이것들을 쌓아놓고 조상님들께 차례를 지내기도 한다.", "또한 가을은 독서 계절이라고도 하여 책을 읽으며 시시때때로 명상에 잠기기도 하는데 독서는 우리에게 마음을 잘찌우고 아름답게 하는 힘을 주기 때문이다.", "어제 친구와 극장에 갔다.", "한 시 삼십 분쯤 극장에 도착했는데 일요일이라 그런지 사람들로 북적였다.", "두 시에 하는 영화는 모두 매진이어서 네 시까지 기다렸다가 영화를 보아야 했다.", "나는 영화를 보면서 내용이 너무 슬퍼서 계속 눈물이 났다.", "그러나 같이 간 친구는 전혀 눈물을 보이지 않았다.", "친한 친구들과 함께 한탄강에 래프팅을 갔다.", "날이 흐린 편이라 래프팅을 온 사람들은 많지 않았다.", "우리는 헬멧과 구명조끼를 착용하고 보트에 탔다.", "안전 요원의 구령에 맞춰 열심히 노를 저었다.", "물살이 빠른 곳으로 가자 보트가 빨라져 더 신이 났다.", "정신없이 놀다 보니 어느새 두 시간이 훌쩍 지났다.", "다음 여름에도 꼭 다시 오자고 돌아오는 길에 친구들과 약속했다.", "지하철은 어디에서 타요?", "여기 엘리베이터를 타고 지하 일 층으로 내려가세요.", "티머니 카드는 어디에 살 수 있어요?", "편의점이나 자동판매기에서 살 수 있어요.", "여기에서 몇동역까지 어떻게 가요?", "공항철도를 타고 서울역까지 가서 사 호선으로 갈아타시면 돼요.", "이 버스를 타려면 어디에 가야 돼요?", "저기 중앙차로에 있는 정류장에서 타면 돼요.", "이 버스 몇동호텔 앞에 가요?", "네 갑니다.", "택시 어디에서 타요?", "호텔 앞에 택시 정류장이 있습니다.", "기사님 인천공항으로 가 주세요.", "네 알겠습니다.", "공항까지 얼마나 걸려요?", "한 시간쯤 걸려요.", "어디에서 세워 드릴까요?", "저 십 번 게이트 앞에서 세워 주세요.", "뭐 드릴까요?", "불고기 주세요.", "주문 받을까요?", "네, 떡볶이 주세요.", "주문하시겠어요?", "비빔밥 하나 주세요.", "팥빙수 하나, 망고 빙수 하나 주세요.", "죄송합니다. 지금 빙수는 안 되는데요.", "여기요. 반찬 좀 더 주세요.", "저기요. 물 좀 더 주세요.", "물은 셀프예요.", "숟가락 하나 더 주시겠어요?", "네, 숟가락 여기 있습니다.", "물티슈 있나요?", "네, 있습니다. 가져다 드리겠습니다.", "따뜻한 아메리카노 한 잔 주세요.", "드시고 가실 거예요?", "아니요, 가져갈 거예요.", "빙수 포장 돼요?", "네, 됩니다.", "딸기 빙수 하나 포장해 주세요.", "네, 나오면 진동벨로 알려 드리겠습니다.", "숟가락 몇 개 넣어 드릴까요?", "두 개 주세요.", "토마토 주스 하나, 얼음 빼고 주세요.", "딸기 주스, 시럽 빼고 주세요.", "핫 초콜릿에 휘핑크림을 휘픙크림 올려 드릴까요?", "네, 휘핑크림 많이 주 올려 주세요.", "여기 비빔밥 하나 주시는데요. 고기는 빼고 주세요.", "비빔밥 하나, 고기 빼고요?", "네, 맞아요.", "찾으시는 거 있으세요?", "바지를 하나 사고 싶은데요.", "요즘 뭐가 제일 잘 나가요?", "이게 요즘 제일 인기가 많아요.", "이거 입어 봐요 봐도 돼요?", "네, 저쪽에서 입어 보세요.", "아주 잘 어울리시네요. 마음에 드세요?", "예쁜데 좀 큰 것 같아요. 작은 거 없어요?", "이 바지하고 티셔츠 주세요.", "네, 잠시만 기다려 주세요.", "이걸로 하시겠어요?", "네, 그런데 새 걸로 주세요.", "계산할게요.", "네, 사만 팔천 원입니다. 결제 어떻게 하시겠습니까?", "카드 되죠?", "여기 설 서명해 주세요. 영수증 드릴까요?", "네, 주세요.", "이거 어제 샀는데 환불 돼요?", "네, 결제하신 카드와 영수증 주세요.", "이건 세일 상품이라 환불은 안 되고 교환만 가능한데요.", "아, 그래요? 그럼 교환할게요.", "다른 색도 있는데 보여 드릴까요?", "네, 무슨 색 있어요?", "하얀색으로 교환해 주세요.", "네, 새 걸로 갖다 드릴게요.", "교환이나 환불 돼요?", "팩을 좀 사고 싶은데요. 세일하는 제품 있어요?", "이게 요즘 제일 잘 나가요.", "여기 수분 크림 있지요? 어디에 있어요?", "손님, 죄송하지만 그 제품은 다 나갔는데요.", "언제 다시 들어와요?", "이거 테스트해 봐요 봐도 돼요?", "네, 해 보세요.", "피부가 좀 건조하고 어두운 편인데요.", "비비크림 하나 추천해 주시겠어요?", "이게 잘 맞을 것 같+ 같은데 어떠세요?", "한국에 어떻게 오셨어요?", "여행 왔어요.", "며칠 동안 계실 거예요?", "일주일 있을 거예요.", "숙소는 어디세요?", "명동 호텔이에요.", "짐 찾는 곳이 어디예요?", "에스컬레이터를 타고 아래층으로 내려가시면 있어요.", "심 카드는 어디에+ 어디에서 살 수 있어요?", "저기 은행 옆에 심 카드 판매소가 있어요.", "여권을 좀 주시겠습니까?", "여기 있습니다.", "어느 자리로 드릴까요?", "창가 좌석으로 주세요.", "부치실+ 부치실 가방 있으십니까?", "네, 한 개 있어요.", "가방 안에 보조배터리가 들어 있는데 괜찮으실 괜찮을까요?", "아니요, 보조배터리는 가지고 타셔야 돼요.", "공항버스 매표소는 어디에 있어요?", "사 번 게이트 옆에 있습니다.", "강남고속터미널에 가려면 몇 번 버스를 타야 돼요?", "육천 일 번이나 육천 십 오번 버스를 타면 타시면 돼요.", "안녕하세요? 체크인하려고 하는데요.", "네, 여권 좀 보여 주세요.", "방은 금연실이지요?", "네, 금연실입니다.", "한강이 보이는 방으로 줄 수 있으세요?", "잠시만요. 네, 가능합니다.", "전망 좋은 방으로 주세요.", "죄송하지만 전망 좋은 방은 다 찼습니다.", "아침 식사는 어디에서 하나요?", "이 층 식당으로 가시면 됩니다.", "아침 식사 시간은 몇 시까지예요?", "일곱 시부터 열 시까지입니다.", "와이파이는 무료예요 무료예요?", "네, 무료로 이용하실 수 있습니다.", "와이파이 와이파이 비밀번호가 뭐예요?", "일 이 삼 사 오입니다.", "구백 이 호인데요. 헤어드라이기 헤어드라이어가 안 돼서요.", "세면대 물이 잘 안 내려가는데요.", "네, 곧 확인해 드리겠습니다.", "수건 좀 주시겠어요?", "네, 구백 이 호 맞으십니까? 가져다 가져다 드리겠습니다.", "지금 나간 나갔는데 방 청소 좀 해 주시겠어요?", "카드 키를 잃어버린 것 같아요.", "새로 만들어 드리겠습니다. 몇 호실이 몇 호실이세요?", "방에 키를 놓고 나왔는데요.", "다른 키를 드리겠습니다.", "사용하시고 반납 부탁드립니다.", "가방 좀 맡겨 맡겨도 될까요?", "네, 이쪽으로 갖다 가져다주시면 됩니다.", "제 짐을 찾아가고 싶습니다.", "네, 보관증을 보여 주십시오.", "이십 오 일하고 이십 육 일에 콘서트를 하는데 언제 볼까요?", "이십 오 일 저녁에는 다른 일정이 있으니까 이십 육 일이 좋겠어요.", "R석은 가격이 너무 비싸요.", "그러게요. 우리 S석으로 예매해요.", "안녕하세요, 인터넷으로 예매해서 티켓 찾으러 왔 왔는데요.", "네, 어디에서 예매하셨어요?", "예스파크에서요.", "저, 죄송한데 사진 좀 찍어 주시면 안 될까요?", "네, 찍어 드릴게요. 하나, 둘, 셋.", "여기가 한국 기획사가 있는 건물이 맞나요?", "네, 맞아요. 저기 보이는 입구로 들어가시면 돼요.", "들어가서 구경해도 되나요?", "네, 어서 들어오세요.", "다섯 시 표 두 장으로 표 두 장 주세요.", "다섯 시 영화는 지금 매진입니다.", "그다음 영화는 언제 해요?", "일곱 시 십 분 영화가 있습니다.", "그럼 그걸로 두 장 주세요.", "주문 안 하신 손님 이쪽으로 오세요.", "팝콘 어떤 맛으로 드릴까요?", "오리지널하고 캐러멜 맛 반반씩 주세요.", "이 아이돌 그룹 알지요?", "그럼요. 저도 팬이에요.", "직접 본 적 있어요?", "아니요, 사진이랑 동영상으로만 봤어요.", "직접 만나서 악수 한번 해 봤으면 좋겠어요.", "저도 우리나라에서 팬 미팅이나 콘서트를 하면 꼭 갈 갈 거예요.", "와, 부러워요. 저는 춤을 잘 못 춰요.", "여기가 드라마 세트장입니다.", "세트장이 굉장히 크네요.", "이곳은 아나운서 체험을 하는 곳입니다.", "저도 한번 해 보고 싶어요.", "이쪽에 와서 서 보세요. 아주 예뻐 보여요.", "조명을 받아서 그런가 봐요.", "이 스튜 스+ 스튜디오에서는 드라마 녹화 중입니다.", "저는 드라마 녹화하는 것 처음 봐요.", "여기에서 드라마 ‘마인’을 찍었어요.", "한번 꼭 와 보고 싶어요.", "여기가 마지막 장면을 찍은 곳이래요.", "그렇네요. 기억나요.", "저기 서 있는 사람이 배우 이유라지요?", "네, 정말 인형 같아요.", "지금 가서 사인 받아도 될까요?", "지금은 촬영 중이라서 안 될 것 같아요.", "이 사진 좀 봐요.", "정말 배우랑 같이 찍은 것처럼 나왔어요.", "이 배우는 정말 잘생겼네요.", "네, 실물도 잘생겼고 카메라도 잘 받은 받는 것 같아요.", "아나운서 목소리 정말 좋았+ 좋았지요?", "네, 그 목소리에 반했어요.", "뭐가 제일 재미있었어요?", "아나운서 체험요.", "저는 드라마 세트장이 제일 재미있었어요.", "한복 좀 빌려줘* 빌려 빌리려고 하는데요.", "네, 여기서 골라 보세요.", "해설 들으면서 구경하고 싶은데요.", "여기 안내문+ 안내문을 보시고 시간에 맞춰 출발 장소에서 기다리세요.", "음성 안내기를 빌릴 수 있을까요?", "네, 신분증이나 여권 주시겠어요?", "짐을 좀 맡기고 싶은데요.", "일 층과 삼 층에 물품 보관함이 있으+ 있으니까 그걸 이용하세요.", "저기 행사 포스터에 있는 문화 공연을 보고 싶은데요.", "아, 저 공연을 보시려면 홈페이지에서 미리 신청하셔서 신청하셔야 돼요.", "자유이용권 두 장 주세요.", "할인되는 카드 있으세요?", "이게 롤러코스터 줄이에요+ 줄이에요? 줄이 너무 기네요.", "맞아요. 이게 이 공원에서 제일 인기가 많아요.", "퍼레이드는 어디에서 시작해요?", "바이킹 옆에서 시작해요.", "봄꽃 축제 기간은 언제예요?", "사 월 칠 일부터 십 이 일까지예요.", "이 공연을 보고 싶은데요.", "공연장은 어느 쪽으로 가야 돼요?", "나가셔서 왼쪽으로 쭉 가시면 돼요.", "어디에서 사진 찍으면 제일 예뻐요?", "저기에서 찍으면 제일 잘 나와요.", "PC방에 처음 왔는데요. 어떻게 이용하는 거예요?", "이 기계에서 시간 선택하시시고 결제하시면 돼요.", "화면에 메뉴판이 있네요. 음식도 먹을 수 있나 봐요.", "네, 자리에서 주문하면 가져다준대요.", "이용 시간이 십 분 남았네요. 조금 더 연장할까요?", "네, 우리 삼십 분만 더 있다가 가요.", "여기는 강아지들이 많네요. 들어가도 돼요?", "네, 그럼요. 여기는 애견 카페예요.", "이 강아지는 이름이 뭐예요?", "쿠키예요. 두 살이에요.", "와, 정말 귀엽네요. 이 과자 줘도 돼요?", "네, 조금만 주세요.", "저기 야구 경기 규칙을 잘 모르는데 괜찮을까요?", "규칙을 몰라도 응원만 해도 재미있대요.", "저녁을 안 먹고 안 먹고 와서 배가 고프네요.", "제가 뭐 좀 사 올게요.", "우리 치맥 먹을까요? 매점에서 팔더라고요.", "찜질방 성인 두 명이요.", "야간은 만 원이고, 오전 다섯 시까지 이용할 수 있어요.", "어디로 들어가면 돼요?", "여자 탈의실은 이쪽이에요.", "식혜하고 맥반석 달걀하고 두 개씩 주세요.", "오천 원입니다.", "이 동네가 한국 대학생들이 많이 오는 곳이래요.", "예쁜 카페가 많아서 인기 있나 봐요.", "이 식당 사장이 제가 좋아하는 가수예요.", "팬들이 많이 오겠어요.", "저 식당은 줄 서서 기다리는 사람이 많네요.", "맛집인가 봐요.", "이 거리에서 이 식당이 제일 유명하대요.", "아, SNS에서 본 집이에요.", "동대문 시장에 가 볼까요?", "네, 좋아요. 밤에 가면 더 싸대요.", "고속버스터미널 지하에 가면 옷을 싸게 살 수 있대요.", "네, 저도 들었어요. 우리 오늘 가 봐요.", "화장품이 더 필요하다고 했지요? 오늘 사러 갈까요?", "지난번에는 명동에서 샀으니까 오늘은 신촌으로 가요.", "오늘 저녁에는 이태원에 가 볼까요?", "네, 좋아요! 꼭 한번 구경하고 싶었어요.", "와, 한강에는 자전거 타는 사람들이 많네요.", "우리도 자전거 빌려서 탈까요?", "유람선을 타고 싶어요.", "저 앞이 선창 선착장 것 같아요. 가 봐요.", "한강 다리에서 분수가 나오네요.", "네, 밤에는 조명을 켜서 더 예뻐요.", "오늘 구경 정말 많이 했+ 했어요.", "너무 많이 걸어서 다리가 좀 아파요.", "저도요. 우리 벤치에 앉아서 좀 쉬어요.", "이 동네에는 한오그릇* 한옥들이 많이 모여 있네요.", "그래서 한옥 마을이라고 부른대요.", "저기가 남대문이에요.", "우리 여기 서서 남대문이 다 나오게 사진 찍어요.", "인사동이에요. 저기 저는 여기 꼭 와 보고 싶었어요.", "저도요. 옛날 건물이 많아서 재미있어요.", "한국 전통찻집이 있네요.", "무슨 차가 있는지 들어가 봐요.", "네, 좋아요. 어떤 분+ 분위기인지 궁금하고", "한국 전통차도 마시고 싶어요.", "바다를 보려고 바다를 보려면 어디로 가야 돼요?", "해운대나 광안리 해수욕장에 가 보세요.", "그리고 달맞이길에 가면 바다가 보이는 예쁜 카페도 많아요.", "가 볼 만한 곳을 좀 추천해 주시겠어요?", "태종+ 태종대나 동백섬+ 동백섬도 좋아요.", "부산에서 유명한 음식이 뭐예요?", "밀면이 맛있는데요. 이 근처에 유명한 집이 있어요.", "맛있는 생선 횟집을 알려 주세요.", "네, 자갈치 시장에 가시면 돼요.", "이거 어떻게 먹는 거예요?", "계란을 콩나물국밥에 넣어서 먹어요+ 먹어요?", "국밥은 나중에 따로 드시는 거에 거고요.", "와! 반찬이 정말 많이 나오네요.", "이 길에서 파는 거 다 먹어 보고 싶어요.", "저도요. 다 맛있어 보이네요.", "옛날 사람들도 이렇게 화려한 귀걸이를 했네요.", "네, 금으로 이렇게 만들었다니 신기해요.", "저기에 올라와 올라가서 사람들이 하늘의 별을 봤대요.", "아, 그래요? 그런데 저 위에 어떻게 올라갔을+ 갔을까요?", "이 탑 어디에서 본 것 같지 않아요?", "한국 동전에서 본 것 같아요.", "우리 황남빵 두+ 두 박스 살까요?", "황남빵은 경주에서만 살 수 있대요. 한 박스 더 사요.", "성산일출봉 정상까지 올라가는 데 얼마나 걸려요?", "이십에서 삼십 분쯤 걸릴 거예요.", "오늘 한라산에 올라가면 백록+ 백록담을 볼 수 있을까요?", "날씨가 좋으니까 볼 수 있을 거예요.", "와! 바다색이 정말 예쁘네요.", "맞아요. 모래가 반짝거리는 게 너무 예뻐요.", "귤 맛 귤 많이 땄어요?", "생각보다 어려워서 많이 못 땄는데 재미있으 있네요.", "배고파요? 진짜?", "배고파요. 진짜.", "밥 먹었어?", "밥 먹었어.", "목 말라요?", "목 말라요.", "아이가 넘어졌어요.", "아이가 넘어졌어요?", "지금 가도 돼요?", "지금 가도 돼요.", "많이 아파요.", "많이 아파요?", "순두부 먹고 싶어요.", "순두부 먹고 싶어요?", "오늘이 수요일이에요+ 수요일이에요?", "오늘이 수요일이에요.", "문 잠겨 있어요.", "문이 잠겨 있어요?", "편의점에서 이거 사도 돼?", "편의+ 편의점에서 이거 사도 돼.", "백 십 오 호 방이 어+ 어두워요+ 어두워요.", "백 십 오 호 방이 어두워요?", "에어콘 때문에 추워요.", "에어콘 때문에 추워요?", "한 시간 늦을 것 같아요.", "한 시간 늦을 것 같아요?", "화장실 가고 싶어요.", "화장실 가고 싶어요?", "엄마가 아이에게 우유를 먹여요.", "엄마가 아이에게 우유를 먹어요?", "형이 동생을 밀어요.", "형이 동생을 밀어요?", "할머니가 무말랭이를 말려요.", "할머니가 무말랭이를 말려요?", "할아버지가 할아버지께서 신문을 읽어요?", "할아버지께서 신문을 읽어요.", "구부리면 허리가 아파요?", "구부리면 허리가 아파요.", "요한 씨, 오늘 오후 한 시에 뭐해요?", "식당에 가요. 점심을 먹어요.", "그러면 오후 세 시에는 뭐해+ 뭐해요?", "백화점에 가요. 그리고 쇼핑해요.", "여섯 시에는 뭐해요?", "커피숍에 가요. 그리고 커피를 마셔요.", "수안 씨, 오늘 오후 두 시에 뭐해요?", "친구와 커피숍에 가요.", "그러면+ 그러면 오후 네 시에는 뭐해요?", "공원에 가요. 그리고 산책해요.", "집에 가요. 그리고 숙제해요.", "육 월 팔 일 팔 일에 시험이 있어요.", "그래요? 무슨 요일이에요?", "금요일이에요.", "몇 시에 시험이 있+ 있어요?", "오전 아홉 시예요. 여덟 시 반까지 오세요.", "무엇을 준비해요?", "3시간 뒤 날씨 알려 줘.", "3월 14일은 무슨 날이에 날이야?", "4월 15일이 무슨 요일이야?", "4월에 쉬는 날 있어?", "5분 카운트다운 시+ 시작해 줘.", "5분 후에 약 먹기라고 알려 줘.", "5월에 쉬는 날 며칠 있어?", "700회 당첨자 수 알려 줘.", "700회 회로* 로또 번호 알려 줘.", "70년대 팝음악 들려 줘.", "내일 12시에 피부과 가기 일정 등록해 줘.", "내일 4시까지 일정 삭제해 줘.", "내일 서울 초미+ 초미세먼지 지수 알려 줘.", "내일 아침에 알람 몇 시야?", "내일 오후에 오후 3시 서울 날씨 알려 줘.", "내일 제주도 강수 확률 어때?", "내일 제주도 미세먼지 어때?", "내일 제주에 비 소식 있어?", "내일 모레 날짜 알려 줘.", "너 몇 살이야?", "오늘 3시 일정 캘린더에서 삭제해 줘.", "오늘 3시에 회어이* 있어?", "오늘 날씨 어때?", "오늘 날씨와 어울리는 노래 틀어 줘.", "오늘 뉴스 알려 줘.", "오늘 뉴욕 날씨 좋아?", "오늘 마+ 마스크 써야 해+ 해?", "오늘 며칠이야?", "오늘 별자리 운세 알려 줘.", "오늘 수요일이야?", "오늘 습도는 어때?", "오늘 엔화 환율 알려 줘.", "오늘 오후 3시 일정 등록해 줘.", "오늘 우산 필요해?", "오늘 일정 삭제해 줘.", "오늘 일정 알려 줘.", "오늘 장 봐야 한다고 나와의 채팅방에 메모해 줘.", "오늘 저녁에 약속 있어?", "오늘 주가 얼마야?", "이번 달 물병자리 운세 알려 줘.", "이번 주 날씨 어때?", "이번 주 돼지띠 운세 어 알려 줘.", "이번 주 로또 번호가 번호 알려 줘.", "이번 주 물병자리 운세 알려 줘.", "이번 주말에 눈 와?", "이+ 이번 주말에 부산 날씨 어때?", "이전 뉴스 틀어 줘.", "이전 채널로 바꿔.", "이전 팟+ 팟캐트 팟+ 팟캐스트 틀어 줘.")
    private var currentWordIndex = 0

    // AudioRecord 관련 변수
    private var isRecording = false
    private lateinit var audioRecord: AudioRecord
    private lateinit var outputFile: String
    private lateinit var recordingThread: Thread


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learning)

        // 권한 확인 및 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
        }

        // UI 요소 초기화
        wordTextView = findViewById(R.id.wordTextView)
        fileCountTextView = findViewById(R.id.fileCountTextView)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        btnRecord = findViewById(R.id.btnRecord)
        btnLearn = findViewById(R.id.btnLearn)

        // 현재 낱말 표시
        wordTextView.text = words[currentWordIndex]
        updateFileCount()  // 처음에 파일 개수 표시

        // 이전 버튼 클릭 이벤트
        btnPrevious.setOnClickListener {
            if (currentWordIndex > 0) {
                currentWordIndex--
                wordTextView.text = words[currentWordIndex]
                updateFileCount()  // 단어가 변경된 후 파일 개수 업데이트
            } else {
                Toast.makeText(this, "첫 번째 단어입니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 다음 버튼 클릭 이벤트
        btnNext.setOnClickListener {
            if (currentWordIndex < words.size - 1) {
                currentWordIndex++
                wordTextView.text = words[currentWordIndex]
                updateFileCount()  // 단어가 변경된 후 파일 개수 업데이트
            } else {
                Toast.makeText(this, "마지막 단어입니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 녹음 버튼 클릭 이벤트
        btnRecord.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                // 권한 확인 후 녹음 시작
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    startRecording()
                } else {
                    Toast.makeText(this, "녹음 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        //학습버튼
        btnLearn.setOnClickListener {
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("signals")
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_user"
            Log.d("UserID", "User ID: $userId")
            // 사용자 ID와 "study" 신호를 Firebase에 전송
            val signalData = mapOf(
                "userId" to userId,
                "action" to "study"
            )
            myRef.push().setValue(signalData)
        }
    }

    private fun startRecording() {
        val sampleRate = 44100  // 44.1kHz 샘플 레이트
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2

        // AudioRecord 객체 초기화
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize)

        val outputDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        if (outputDir != null && !outputDir.exists()) {
            outputDir.mkdirs()  // Music 폴더가 없으면 생성
        }
        outputFile = "${outputDir}/${words[currentWordIndex]}_${System.currentTimeMillis()}.wav"
        val outputStream = FileOutputStream(outputFile)

        val buffer = ByteArray(bufferSize)

        // 먼저 더미 헤더를 작성하고 파일에 기록
        val header = createWavFileHeader(0, 0, sampleRate, 1, 16)
        outputStream.write(header)

        // 녹음 시작
        audioRecord.startRecording()
        isRecording = true
        btnRecord.text = "녹음 중지"
        Toast.makeText(this@LearningActivity, "녹음이 시작되었습니다.", Toast.LENGTH_SHORT).show()

        // 녹음 스레드 실행
        recordingThread = Thread {
            try {
                var totalAudioLen: Long = 0
                val headerSize = 44  // WAV 헤더 크기

                while (isRecording) {
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        outputStream.write(buffer, 0, read)
                        totalAudioLen += read
                    }
                }

                // 총 데이터 길이와 파일 크기를 계산
                val totalDataLen = totalAudioLen + headerSize - 8
                updateWavHeader(outputFile, totalAudioLen, totalDataLen, sampleRate, 1, 16)
                outputStream.close()

                // 녹음이 끝난 후 Firebase에 업로드
                uploadFileToFirebase(outputFile)

                // 파일 개수 업데이트
                runOnUiThread {
                    updateFileCount()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        recordingThread.start()
    }

    // Firebase에 파일 업로드 함수
    private fun uploadFileToFirebase(outputFile: String) {
        // 녹음된 파일을 Uri로 가져오기
        val file = Uri.fromFile(File(outputFile))

        // Firebase Storage에 파일을 저장할 참조 설정
        val storageRef = FirebaseStorage.getInstance().reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_user"
        val fileRef = storageRef.child("audio/$userId/${words[currentWordIndex]}_${System.currentTimeMillis()}.wav")

        // 파일 업로드
        fileRef.putFile(file)
            .addOnSuccessListener {
                // 업로드 성공 시 처리
                Toast.makeText(this, "파일 업로드 완료", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // 업로드 실패 시 처리
                Toast.makeText(this, "파일 업로드 실패", Toast.LENGTH_SHORT).show()
            }
    }


    private fun stopRecording() {
        // 녹음 중지
        isRecording = false
        audioRecord.stop()
        audioRecord.release()

        // 녹음 스레드 종료 대기
        recordingThread.join()
        btnRecord.text = "녹음 시작"
    }

    // WAV 파일 헤더 생성
    private fun createWavFileHeader(totalAudioLen: Long, totalDataLen: Long, longSampleRate: Int, channels: Int, bitRate: Int): ByteArray {
        val header = ByteArray(44)
        val sampleRate = longSampleRate
        val byteRate = bitRate * sampleRate * channels / 8

        header[0] = 'R'.toByte() // RIFF/WAVE header
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.toByte() // WAVE
        header[9] = 'A'.toByte()
        header[10] = 'V'.toByte()
        header[11] = 'E'.toByte()
        header[12] = 'f'.toByte() // 'fmt ' chunk
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1 (PCM)
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = (sampleRate shr 8 and 0xff).toByte()
        header[26] = (sampleRate shr 16 and 0xff).toByte()
        header[27] = (sampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (channels * bitRate / 8).toByte() // block align
        header[33] = 0
        header[34] = bitRate.toByte() // bits per sample
        header[35] = 0
        header[36] = 'd'.toByte() // data chunk identifier
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()
        return header
    }

    // WAV 파일 헤더 업데이트
    private fun updateWavHeader(filePath: String, totalAudioLen: Long, totalDataLen: Long, longSampleRate: Int, channels: Int, bitRate: Int) {
        val header = createWavFileHeader(totalAudioLen, totalDataLen, longSampleRate, channels, bitRate)
        try {
            val raf = java.io.RandomAccessFile(filePath, "rw")
            raf.seek(0) // 파일의 처음으로 이동하여 헤더 작성
            raf.write(header)
            raf.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // 녹음된 현재 단어의 파일 개수를 업데이트하는 함수
    private fun updateFileCount() {
        val dataFolder = getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath ?: return

        // 현재 단어에 해당하는 파일 필터링
        val recordedFiles = File(dataFolder).listFiles { file ->
            file.extension == "wav" && file.name.contains(words[currentWordIndex])
        } ?: emptyArray()

        // 파일 개수 업데이트
        fileCountTextView.text = "현재 녹음된 파일: ${recordedFiles.size} / 100 개"
    }
}
