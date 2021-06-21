package co.copper.app;

import co.copper.app.entity.address.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AppTest {

	BlockExplorer client;

	@Before
	public void setUp() throws Exception {
		client = new BlockExplorer();
	}

	@Test
	public void test() throws Exception {
//		Address address = client.getAddress("1jH7K4RJrQBXijtLj1JpzqPRhR7MdFtaW", FilterType.All, null, null);
		Address address = client.getAddress("1jH7K4RJrQBXijtLj1JpzqPRhR7MdFtaW");
		assertEquals("1jH7K4RJrQBXijtLj1JpzqPRhR7MdFtaW", address.getAddress());
		assertEquals("07feead7f9fb7d16a0251421ac9fa090169cc169", address.getHash160());
		assertEquals(0, address.getFinalBalance());
		assertEquals(16, address.getTxCount());
		assertEquals(605204, address.getTotalReceived());
		assertEquals(605204, address.getTotalSent());
		assertEquals(16, address.getTransactions().size());
	}

}