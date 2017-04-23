# Chapter 05
Chapter04의 WordCount소스에 사용된 MapReduce소스에 대해서 분석해보는 챕터입니다.

# 차례 
 1. 맵퍼
 2. 맵리듀스의 기본 자료형
 3. 입력포맷
 4. 컴바이너
 5. 셔플링과 소팅
 6. 리듀스
 7. 출력포맷
 8. 카운터
 9. MRUnit과 메이븐
 10. 잡 트래커와 웹 인터페이스
  


# 맵 리듀스의 맵퍼

``` java
class Mapper{

    public void setup(Mapper.Context context);
    /*
    map 메소드에서 필요 리소스를 할당하는 역할을 합니다. map의 선행작업을 여기서 수행할 수 있습니다.
    분산캐시를 오픈하거나 파일을 미리오픈하거나 여기서 할 수 있습니다.
    */    

    public void cleanup(Mapper.Context context);
    /*
    setup클래스의 반대 역할입니다. 할당한 리소스를 해제하는 역할을 합니다. 함수의 호출이 완료되면, 마지막으로 한번 호출됩니다.
    예를들어 setup에서 할당한 자원을 cleanup에서 해제하는것이 일반적인 사용모델입니다.
    */

    public void run(Context context) throws IOException InterruptedException{
        setup(context);
        while(context.nextKeyValue()){
            map(context.getCurrentKey(), context.getCurrentValue(),context)
        }
        cleanup(context);
    }
    /*
    Mapper의 전체 구동함수에 해당하며 이 함수를 오버라이드 할 일은 거의 없을것이다.
    setup메서드의 작업이 끝나야지 run메서드가 수행된다.
    */


}
```
 
### 맵퍼의 입력 K1,V1
 맵의 입력에서 가장 중요한 부분은 어떤 입력 포맷을 사용하였는가이다.
 - 텍스트 라인 하나가 하나의 레코드에 해당
 - 해당 라인의 파일오프셋(파일 처음부터의 위치)가 키 값이 된다. (키 타입은 LongWritable)
 - 해당 라인의 전체가 Value가 됩니다. Value Type은 Text가 됩니다.
맵의 입력에 사용되는 입력포맷은  TextInputFormat 이외에도 [KeyValueTextInputFormat, SequenceFileInputFormat]등이 있습니다.

### 맵퍼의 출력 K3,V3
 맵의 출력 레코드들의 타입들이 전체 하둡 잡의 출력 타입들과 다르다면 Job클래스의 다음 두 메서드 호출을 통해서 프레임 워크에 알려야 합니다.

>   job.setMapOutputKeyClass
>   job.setMapOutputValueClass

워드카운트 에서는 맵의 출력타입과 리듀스의 출력타입이 일치했기 때문에 위의 두 메소드를 호출할 필요기 없었습니다.

간혹 맵 출력만 필요하고 리듀스는 아예 필요로 하지 않는다면 리듀스 태스크의 수를 0으로 지정하면 된다. 그러면 지정한 하둡잡의 출력 디렉토리에
맵의 출력물들이 바로 저장되게 된다. 

> 저장되는 파일 이름 형식은
> part-r-xxxx => part-m-xxxx가 된다.

### 아이덴티티 맵퍼,리듀서 (Identity Mapper & Reducer)
 작업중에는 맵이나 리듀스가 필요없는것들도 있다. 이럴 때 사용하는게 아이덴티티 맵퍼나 리듀서이다.
> 아이덴티티 매퍼와 아이덴티티 리듀서는 주어진 입력 레코드(키,벨류)를 그대로 출력레코드로 내보내는 단순한 맵 클래스와 리듀스 클래스.



# MapReduce의 기본 자료형 및 변수들

### Writable 인터페이스
 - 기본적으로 이 인터페이스는 직렬화/역직렬화(Serialization/Deserialization) 을 구현하는데 사용되는 메소드들을 갖고 있다.
 - 하둡 특성상 키/벨류 레코드가 디스크에 저장되거나 네트워크를 타고 전달되어야 하기 때문에 이런 인터페이스가 필요하다.
 - 하둡은 RPC(Remote Procedure Call)을 이용해서 클러스터 내의 노드들 간에 통신합니다. 이 경우에도 Writable인터페이스를 사용하여 통신합니다.


``` java 
public class ItemFreq implements Writable{
    private String item;
    private Long freq;

    @Override
    public void write(DataOutput out)throws IOException{
        //write는 직렬화(Serialization)에 사용되는 메서드 이다. 주어진 스트림 내부 데이터를 차례로 저장하는 일을 수행하며, 저장된 데이터들만 보고 원래 상태가 복구될 수 있게 필요한 모든 정보를 저장한다.
        WritableUtils.wrtieString(out, item);
        // 먼저out객체에 item을 작성하고
        out.writeLong(freq);
        // 그 다음 out 객체에 값을 작성한다.
    }
    @Override
    public void readFields(DataInput in) throws IOException{
        // readFields 는 역직렬화(Deserialization)에 사용되는 메서드 이다. write에서 저장했던 순서 ㄱ대로 차례로 저장했던 데이터를 꺼내어 객체의 마지막 상태로 원상 복구하는 역할을 수행한다.
        item = WritableUtils.readString(in);//in의 item문자열을 읽는다.
        freq = in.readLong(); // freq를 읽는다.
    }
    //... 생략

}
```

### WritableComparable 인터페이스
 - WritableComparable 은 Writable 에서 제공되는 메소드들에다가 객체들간의 비교를 가능하게 해주기 위해 Comparable인터페이스가 추가된 인터페이스 입니다.
 - 하둡에서 맵과 리듀스에서 사용되는 키들은 소팅이 가능해야 하기 때문에 이런 인터페이스가 필요합니다. 
 - Compareble인터페이스에는 compareTo라는 메소드 하나가 존재하며 이는 결국지금 객체와 인자로 들어온 객체들을 비교하여 둘 사이의 순서를 정해주는 역할을 합니다.

``` java
 // Writable인터페이스에서 compareTo 메서드가 추가된것 말고 다른게 없음. 

    @Override
    public int compareTo(ItemFreq o) {
        int result = item.compareTo(o.item);
        if(0 = result){
            result = (int) (freq-(o.freq));
        }
        return result;
    }

```

### MapReduce의 Wapping 자료형

 Wrapping 변수      | 원본              | 반환 방법 
 -------------------|-------------------|-------------
 Text               | String            | .toString()
 IntWritable        | int               | .get()  
 LongWritable       | Long, long        | .get() 
 FloatWrtiable      | Floa, float       | .get() 
 BooleanWritable    | Boolean, boolean  | .get() 
 ArrayWritable      | [] , ArrayList    |  x
 NullWritable       | null              |  x 

- 이외 제공되는 타입을 보려면 http://org.apache.hadoop.io를 참조하길...


# 입력 포맷의 역할
 - job.setInputFormatClass() 에 해당되는 내용.

### TextInputFormat
 - FileInutFormat을 상속함
 - 텍스트 파일 대상이며 .gz 로 압축된 것도 처리가능하다.
 - 라인 하나(\n, \r)가 하나의 입력 레코드에 해당됨.
 - 한 레코드(라인)에서 키는 라인의 파일 오프셋(파일 선부에서부터) 타입은LongWritable이다.
 - 한 레코드에서 라인 전체 스트링이 되며 타입은 text이다.
 
### KeyValueTextInputFormat
 - Keyvaluetextinputformat = TextInputFormat 기본적으로는 같다.
 - 차이점은 하나의 레코드를 해석할 때 키와 벨류사이에 TAB문자와 같은 문자열을 분리자로 인지 하는지 안하는지에 대한 차이가 있습니다.
 - TAB 문자열 = \r

### SequenceFileInputFormat
 - 하둡의 고유 파일 폼냇은 시퀀스파일
 - 이 때 사용되는 기본 클래스가 바로 이 클래스 
 - 해당 파일이 생성 될 때 사용된 키와 밸류 타입을 사용해야 한다.
 - 하지만 TextInputFormat 과 다른점은 LongWritable 과 Text로 고정된것이 아니기 때문에 어떤 Key와 Value로써 사용가능합니다.
 - 이 포맷은 맵 파일을 읽는데도 사용할 수 있습니다.
 - 디렉토리로 값을 줘도 읽을 수 있으며, 디렉토리로 값을 줄 때 맵 파일의 형태를 먼저 읽고 그 다음에 시퀀스 파일을 로드 합니다.

### MultipleInputs
 - 지금까지 입력포맷은 잡에 하나의 맵만 존재하는 형태였다. 하지만 입력포맷이 다른 경우에는 어떻게 되는가?
 - Multipleinputs클래스를 사용하여 공통된 키를 묶어서 조인을 수행할 수 있다.

``` java
    public static void addInputPath(
        JobConf conf,  // 별도의 job class파일을 이용해도 된다. 
        Path path, //hdfs상의 파일 위치
        Class<? extends InputFormat> inputFormatClass,
        Class<? extneds Mapper> mapperClass
    )

```

### 그 외 입력폼맷
  - CombineFileInputFormat
  - NLineInputFormat



### 맵 태스크 수의 결정 방식
 - 입력파일을 처리하기 위해 필요한 맵 태스크의 수는 프레임워크가 알아서 결정한다. 
 - 입력포맷이 주어진 입력 파일을 처리하는데 몇 개의 맵 태스크가 필요한지 결헝한다.
 - 입력포맷은 getSplits라는 메소드를 갖고 있는데 이는 주어진 모든 입력 파일들을 조각(InputSplit이라 한다.)으로 나눠서 그 조각들의 리스트를 리턴합니다.
 - 이 조각마다 맵 태스크가 하나씩 할당된다.
  
### getSplits의 파일 조각 나누는 방식

 > (맵 태스크 수) = (입력파일 수) \* (데이터블록) or (gzip 등 압축된 파일 수)

 1. 입력파일의 수
    - 기본적으로 입력 파일의 수가 중요한 요소가 된다.
    - 맵 태스크의 수는 이보다 더 작아질 수는 없다.

 2. 입력파일의 크기
    - 데이터 블록으로 구성될 탠대 맵 테스크는 하나의 블록마다 할당된다.
    - 결국 하나의 블록이 하나의 Input Split이 된다.
    
 3. 입력포맷의 지능
    - gzip 등으로 압축되어있으면 전체 파일을 블록수와 관계없이 하나의 맵태스크로 지정함

### 입력포맷의 역할
 - 입력파일을 InputSplit으로 나누기
 - 하나의 InputSplit내의 레코드들을 읽는 방법 제공

 
# 컴바이너 (Combiner)
 - 미니 리듀서 혹은 로컬 리듀서라고 부른다.
 - 맵 태스크의 출력에 리듀스 코드 먼저 적용해서 리듀스로 넘어가야 하는 데이터의 크기를 줄이는 역할 담당.
 - 컴바이너 적용 가능 모델 :잡의 성격마다 다르지만 작업의 순서를 달리해도 최종 결과물이 같은 잡일 경우에만.
 - 컴바이넌 적용 가능 모델이라면 리듀스 클래스를 그대로 컴바이너로 가져가는걸 추천한다.
 - 셔플링과 소팅부분에서는 컴바이너를 여러번 적용 시키는 모델이 존재한다.
 - WordCount의 경우 Combiner의 특성을 가지고 있기 때문에 main함수에서 리듀스 클래스를 그대로 컴바이너로 지정할 수 있습니다.

# 셔플링과 소팅(Shuffling and Sorting)
 맵 리듀스에서 자동으로 해주는 내부동작이다. 맵 태스크와 리듀스 태스크를 이해하기위해 이해하고 넘어가야 할 부분
 
### 파티셔너 to 레코드 어떻게 리듀스로 보내는가.
 1. 결과 레코드의 키값을 해싱한다.
 2. 해싱된 값을 리듀스 태스크의 수로 나눈다.
 3. 해싱된 레코드가 어느 태스크로 갈지 결졍된다.(같은 키를 갖는 레코드들은 같은 리듀스 태스크로 보내지게 된다.)
 4. 1번~3번 동작을 수행하는것이 바로 파티셔너(partitioner)라 부른다.
 - 맵 태스크가 새로운 Key/Value를 출력하면 이는 궁극적으로 특정 리듀스 태스크로 보내져야 한다.
 - 자신만의 파티셔너를 정의할 수 있지만, 기본적으로 사용되는 파티셔너의 클래스이름은 HashPartitioner 

``` java
// HashPartitioner<K,V>.class

public class HashPartitioner<K, V> extends Partitioner<K, V>{
    /** use {@link Object#hashCode()} to partition. **/ 
    public int getPartition(K key,V value, int numReduceTasks){
        return (key.hashcode() & Integer.MAX_VALUE) % numReduceTasks;//파티션 번호에 해당한다.
    }
}

```

 - 파티션 번호 : 위 코드를 보면 알 수 있듯이 주어진 키의 해시값을 얻어낸 다음에 그것을 리듀스 태스크의 수(Job 클래스의 numReduceTasks메소드로 지정한 값)로 나눈 나머지를 리턴하고 있다.
 - 파티셔너는 맵 테스크에서 나온 ㅊㄹ력레코드를 보고 어느 리듀스 태스크로 보낼지 결정해주는것이 파티셔너 입니다.

### 맵 출력 버퍼링 
 1. 맵에서 출력된 레코드 들은 파티셔너(Partitioner) 클래스를 통해 파티션 번호를 알아낸다.
 2. 리듀스로 바로 가지 않고 메모리 버퍼에 씌어졌다가 메모리 버퍼가 차면 디스크에 레코드가 써집니다.
 3. 맵 태스크가 종료될때까지 1번과 2번 과정을 반복합니다.
 4. 종료시에는 디스크로 존재하던 파일들을 모두 모아서 하나의 파일로 병합합니다.(4번의 과정은 모든 맵 태스크마다 이루어지게 됩니다.)
 5. 리듀스 태스크들은 병합된 결과 레코드 파일에서 각기 자신에게 해당하는 파티션의 데이터를 읽어 갑니다.
 6. M개의 맵퍼와 N개의 리듀서가 존재한다면 M\*N개의 네트워크 커넥션이 맺어지게 됩니다.
 7. M\*N개의 커넥션의 네트워크를 통해 리듀서 자신에게 해당되는 데이터를 복사해 갑니다.

 - M\*N개의 커넥션에서 리듀서 자신에게 해당되는 데이터를 복사해갈 때 데이터가 크다면 병목지점이 발생하게 된다.
 - 성능에 있어 랜덤쓰기와 순차쓰기의 차이때문에 맵의 결과를 임시적으로 메모리 버퍼에 저장하는 방식을 이용한다.  (잦은 파일 입출력 방지를 위해)

### 셔플링
 - 앞서 이야기한 




















### 패키지 소스 코드 구분
 -  레거시 코드 
   > org.apache.hadoop.mapred 

 -  최신 코드
  > org.apache.hadoop.mapreduce


