package concurrency.ch6;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.*;


public class SingleLinkedListTest {
	private SingleLinkedList<Integer> list;

	@Before
	public void setUp() throws Exception {
		list = new SingleLinkedList<Integer>();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void initialTest() {
		assertSizeIs(0);
		
	}
	
	@Test
	public void canAddAnElement(){
		list.add(1);
		assertSizeIs(1);
		assertThat(list.get(0), is(equalTo(1)));
		list.remove(0);
		assertSizeIs(0);
	}
	
	@Test
	public void canBuildListof2Elements(){
		list.add(1);
		list.add(2);
		assertSizeIs(2);
		assertThat(list.get(1), is(equalTo(2)));
		assertThat(list.get(0), is(equalTo(1)));
	}
	@Test
	public void canAdd5ElementsAndRemoveThem(){
		for(int i = 0; i < 5; i++){
			list.add(i);
		}
		assertSizeIs(5);
		assertThat(list.get(0), is(equalTo(0)));
		assertThat(list.get(4), is(equalTo(4)));
//		inefficient remove
		for(int i = 4; i >= 0; i--){
			list.remove(i);
			assertSizeIs(i);
		}
		
	}
	private void assertSizeIs(int i){
		assertThat(list.size(), is(equalTo(i)));
	}
}
