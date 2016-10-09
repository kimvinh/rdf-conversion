package org.knoesis.rdf.sp.converter;

import org.junit.Before;
import org.junit.Test;
import org.knoesis.rdf.sp.parser.SPParser;
import org.knoesis.rdf.sp.parser.SPParserFactory;
import org.knoesis.rdf.sp.utils.Constants;
import org.knoesis.rdf.sp.utils.ConstantsTest;

public class NanoPub2SPTest {

	SPParser con1 = null;
	SPParser con2 = null;
	String rep = Constants.NANO_REP;
	@Before
	public void setUp() throws Exception {
		con1 = SPParserFactory.createParser(rep);
//		con1.setZip(true);
		con1.setOntoDir(ConstantsTest.test_data_onto);
		con1.setShortenURI(true);
		con1.setInfer(false);
		con1.init();
		con2 = SPParserFactory.createParser(rep, 10, "str1");
//		con2.setZip(true);
		con2.setOntoDir(ConstantsTest.test_data_onto);
		con2.setInfer(true);
		con1.setShortenURI(true);
		con2.init();
	}

	@Test
	public void testTransform() {
	}

	@Test
	public void testConvert() {
		con1.parse(ConstantsTest.test_data_dir + "/" + ConstantsTest.test_nano, "nt", rep);
		con2.parse(ConstantsTest.test_data_dir + "/" + ConstantsTest.test_nano, "ttl", rep);
//		con1.parse("src/test/resources/test-file/test2_nano.nq", "nt", rep);
//		con2.parse("src/test/resources/test-file/test2_nano.nq", "ttl", rep);
	}

	@Test
	public void testConvertFile() {
	}

	@Test
	public void testGenFileOut() {
	}

}
