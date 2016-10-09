package org.knoesis.rdf.sp.runnable;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.sparql.core.Quad;
import org.knoesis.rdf.sp.concurrent.PipedSPTripleStream;
import org.knoesis.rdf.sp.model.SPModel;
import org.knoesis.rdf.sp.model.SPNode;
import org.knoesis.rdf.sp.model.SPTriple;
import org.knoesis.rdf.sp.utils.Constants;
import org.knoesis.rdf.sp.utils.Reporter;

public class CallableConverter<T> implements Callable<String>{

	SPProcessor processor;
    PipedRDFIterator<T> processorIter;
	PipedRDFStream<SPTriple> converterInputStream;
	Reporter reporter;
	
    public CallableConverter(SPProcessor processor,
			PipedRDFIterator<T> processorIter,
			PipedRDFStream<SPTriple> converterInputStream, Reporter reporter) {
		super();
		this.processor = processor;
		this.processorIter = processorIter;
		this.converterInputStream = converterInputStream;
		this.reporter = reporter;
	}

	@Override
    public String call() {
    	long start = System.currentTimeMillis();
		SPTriple sptriple = null;
		
		converterInputStream.start();
		
		try {
			while (processorIter.hasNext()){
				// Put the output to the writerInputStream
				Object obj = processorIter.next();
				if (obj instanceof Quad){
	    			sptriple = processor.process((Quad)obj);
				} else if (obj instanceof Triple){
	    			sptriple = processor.process((Triple)obj);
				}
				if (sptriple != null) ((PipedSPTripleStream)converterInputStream).sptriple(sptriple);
			}
			Iterator<Entry<String, Integer>> it = processor.getReasoner().getGenericPropertyMapPerFile().entrySet().iterator();
			while (it.hasNext()) {
			    Map.Entry<String,Integer> pair = (Map.Entry<String,Integer>)it.next();
			    ((PipedSPTripleStream)converterInputStream).sptriple(new SPTriple(new SPNode(pair.getKey()), SPModel.rdfType, SPModel.genericPropertyClass));
			}
		} finally {
			converterInputStream.finish();
			reporter.reportSystem(start, Constants.PROCESSING_STEP_CONVERT);
		}
		
        return reporter.getFilename();
    }

	public SPProcessor getProcessor() {
		return processor;
	}

	public void setProcessor(SPProcessor processor) {
		this.processor = processor;
	}

	public PipedRDFStream<SPTriple> getConverterInputStream() {
		return converterInputStream;
	}

	public void setConverterInputStream(
			PipedRDFStream<SPTriple> converterInputStream) {
		this.converterInputStream = converterInputStream;
	}

	public PipedRDFIterator<T> getProcessorIter() {
		return processorIter;
	}

	public void setProcessorIter(PipedRDFIterator<T> processorIter) {
		this.processorIter = processorIter;
	}

	public Reporter getReporter() {
		return reporter;
	}

	public void setReporter(Reporter reporter) {
		this.reporter = reporter;
	}

}
