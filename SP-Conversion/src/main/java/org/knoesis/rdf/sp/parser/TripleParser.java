package org.knoesis.rdf.sp.parser;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.apache.log4j.Logger;
import org.knoesis.rdf.sp.runnable.SPProcessor;
import org.knoesis.rdf.sp.utils.Constants;

public class TripleParser extends SPParser {
	
	final static Logger logger = Logger.getLogger(TripleParser.class);

	public TripleParser() {
	}

	public TripleParser(long uuidInitNum, String _uuidInitStr) {
		super(uuidInitNum, _uuidInitStr);
	}

	@Override
	public void parseFile(String in, String extension, String rep, String dirOut) {
		// PipedRDFStream and PipedRDFIterator need to be on different threads
		PipedRDFIterator<org.apache.jena.graph.Triple> iter = new PipedRDFIterator<org.apache.jena.graph.Triple>(Constants.BUFFER_SIZE, true);
		final PipedRDFStream<org.apache.jena.graph.Triple> inputStream = new PipedTriplesStream(iter);

		// Create a runnable for our parser thread
		Runnable parser = new Runnable() {

			@Override
			public void run() {
				// Call the parsing process.
				RDFDataMgr.parse(inputStream, in, null);
			}
		};

		// Start the parser on another thread
		producerExecutor.submit(parser);
  
        AtomicInteger atomicInt = new AtomicInteger(0);
        final boolean isZip = this.isZip();
        final boolean isInfer = this.isInfer();
        final String conRep = rep;
        final String filein = in;
        final String dirout = dirOut;
        final String ext = extension;
        final String ds = this.getDsName();

		Runnable transformer = new Runnable(){
        	@Override
        	public void run(){
        		SPProcessor processor = new SPProcessor(conRep);
        		processor.setDirout(dirout);
        		processor.setExt(ext);
        		processor.setFilein(filein);
        		processor.setIsinfer(isInfer);
        		processor.setIszip(isZip);
        		processor.setThreadnum(atomicInt.updateAndGet(n -> n + 1));
        		processor.setDsName(ds);
        		
        		processor.start();

        		while (iter.hasNext()){
        			Triple triple = iter.next();
        			processor.process(triple);
        		}
        		
        		processor.finish();
        		processor.close();
        		iter.close();
        		inputStream.finish();
       	}
		};
		consumerExecutor.submit(transformer);
	}

}