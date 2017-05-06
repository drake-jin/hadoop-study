package trigram;

public class ItemFreq {
	private String item;
	private Long freq;
	
	public ItemFreq(){
		this.item = "";
		this.freq = 0L;
	}
	
	public ItemFreq(String item, Long freq){
		this.item = item;
		this.freq = freq;
	}
	
	public String getItem() {
		return item;
	}
	public void setItem(String item) {
		this.item = item;
	}
	public Long getFreq() {
		return freq;
	}
	public void setFreq(Long freq) {
		this.freq = freq;
	}
	
	
}
