# Chapter 06 
 - ![위키피디아 가공 문서](http://download.freebase.com/wex/2012-07-01/freebase-wex-2012-07-0-articles.tsv.bz2)
 - ![프리베이스](http://download.freebase.com/wex/)

### 프리베이스
 - 프리베이스는 자신의 커뮤니티의 구성원들이 업로드한 데이터들로 구성된 대규모 지식 베이스(온라인에 최적화된 Data들)
 - 음악,TV, 영화, 인물, 회사,책 영문버전으로 존재함

### 트러블 슈팅 
 - hdfs의 파일을 /tmp에 저장했을 경우 리부팅을 할 경우에

### 구현할 프로그램

 예제프로그램 | 기능
 -------------|------
 WordCount2   | 기존 WordCount를 개량한것
 TopN         | 빈도수 기준으로 최대의 N개의 레코드들을 뽑는다.
 CountTrigram | 트라이그램별로 빈도수를 계산하고 바로 TopN을 실행하여 빈도수가 높은 트라이그램을 보여줍니다.
 CountCitation | 200만개의 영문 위키피디아 문서들에 대해서 각기 인용된 숫자를 보여준다. WordCount와 흡사하다.
 JoinIDTitle | CountCitation의 결과에 TopN을 적용하여 가장 많이 인용된 문서 10개의 문서를 알아낸 후에 JoinIDTitle을 이용해서 그 문서들의 타이틀을 알아낼 수 있다.


# WordCount2 
 - WordCount와 WordCount2는 3가지가 개선되었다.
 1. 데이터의 크기가 다르다.
 2. 문서형식은 문서 ID와 ID에 대한 텍스트가 들어가는 형태
 3. 키는 ID 밸류는 텍스트, 프로그램의 입력포맷은 KeyValueTextInputFormat 이다.
 4. WordCount 프로그램은 컴바이너의 특성을 이용한다.
 5. 전체단어를 카운트 해서 볼 수 있다.
 

# TopN
 - WordCount2에서는 나타나는 모든 단어의 빈도수가 계산되기 때문에 만일 빈도수가 가장 높은 10개를 알고싶다면 다시 한번 별도로 처리해야한다.
 1.  











