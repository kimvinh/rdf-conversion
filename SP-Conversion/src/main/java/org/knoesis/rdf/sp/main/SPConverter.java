package org.knoesis.rdf.sp.main;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.knoesis.rdf.sp.parser.SPParser;
import org.knoesis.rdf.sp.parser.SPParserFactory;
import org.knoesis.rdf.sp.utils.Constants;
import org.knoesis.rdf.sp.utils.RDFReadUtils;


public class SPConverter {

	
	final static Logger logger = Logger.getLogger(SPConverter.class);


	protected String rep = null;
	protected String ext = null;
	protected String prefix = null;
	protected String fileIn = null;
	protected String metaProp = null;
	protected String metaObj =  null;
	protected String spProp = null;
	protected boolean zip = false;
	protected boolean infer = false;
	protected String ontoDir = null;
	protected String url = null;
	protected String dsName = null;
	protected String _uuidInitStr = null;
	protected long _uuidInitNum = 0;
	protected boolean shortenURI = true;
	protected int parallel = 1;
	protected int bufferSizeStream = Constants.BUFFER_SIZE_STREAM;
	protected int bufferSizeWriter = Constants.BUFFER_SIZE_WRITER;

	
	/**	cd /semweb1/datasets
	 * java -jar ~/rdf-context-converter-0.0.1-SNAPSHOT.jar -zip -parallel 4 -f bio2rdf_R3_data -ext TTL -rep NG -shortenURI -spInitNum 1 -spInitStr b2r_r3 > log/bio2rdf_noinfer_results.txt
	 * java -jar ~/rdf-context-converter-0.0.1-SNAPSHOT.jar -zip -parallel 4 -f bio2rdf_R3_data -infer onto -ext TTL -rep NG -shortenURI -spInitNum 1 -spInitStr b2r_r3 > log/bio2rdf_infer_results.txt
	 * 
	 * @param args
	 * -rep 		NQUAD/NANO/TRIPLE/REI as input file representation
	 * -ext 		TTL/NT as output file extension
	 * -f 			FILE/FOLDER to convert the representation
	 * -base		base URI
	 * -metaProp	meta property of the triple, which could the provenance property of the triple, default value is prov:wasDerivedFrom
	 * -metaObj 	meta object of the triple, which could be the source where the triple was created
	 * -spProp		by default, it is rdf:singletonPropertyOf
	 * -zip
	 * -infer 		Ontology file or directory
	 * -url
	 * -prefix		
	 * -shorternURI
	 */
	
	/** Examples for testing
	 * 
	 * 1)	-f resources/test-nq -ext TTL -rep NG -spInitNum 1 -spInitStr str1
	 * 
	 * 2)	-f resources/test-nq -ext NT -rep NG -spInitNum 1 -spInitStr str2
	 * 
	 * 3) 	-f resources/test-triple -ext TTL -rep Triple -spInitNum 1 -spInitStr str3
	 * 
	 * 4)	-f resources/test-triple -ext NT -rep Triple -spInitNum 1 -spInitStr str4
	 * 
	 * 5)	-f resources/test-default -ext TTL -spInitNum 1 -spInitStr str5
	 * 
	 * 6) 	-f resources/test-default -ext NT -spInitNum 1 -spInitStr str6
	 * 
	 * 7) 	-f resources/test-nano -ext TTL -rep Nano -spInitNum 1 -spInitStr str7
	 * 
	 * 8)	-f resources/test-nano -ext NT -rep Nano -spInitNum 1 -spInitStr str8
	 * 
	 * 9)	-f resources/test-rei -ext TTL -rep REI -spInitNum 1 -spInitStr str9
	 * 
	 * 10)	-f resources/test-rei -ext NT -rep REI -spInitNum 1 -spInitStr str10
	 * 
	 * 11)	-f resources/test-reing -ext TTL -rep ReificationNG -spInitNum 1 -spInitStr str11
	 * 
	 * 12)	-f resources/test-reing -ext NT -rep ReificationNG -spInitNum 1 -spInitStr str12
	 * 
	 * java -jar ~/rdf-context-converter-0.0.1-SNAPSHOT.jar -zip -shortenURI -f ncbo/nq -ext TTL -rep NG -spInitNum 1 -spInitStr ncbo_sp_092016 > ncbo_results.txt 
	 * java -jar ~/rdf-context-converter-0.0.1-SNAPSHOT.jar -zip -shortenURI -f mesh/nq -ext TTL -rep NG -spInitNum 1 -spInitStr mesh_sp_092016 > mesh_results.txt 
	 * java -jar ~/rdf-context-converter-0.0.1-SNAPSHOT.jar -zip -shortenURI -f goa/nq -ext TTL -rep NG -spInitNum 1 -spInitStr goa_sp_092016 > goa_results.txt 
	 * java -jar ~/rdf-context-converter-0.0.1-SNAPSHOT.jar -zip -shortenURI -f pharmkgb/nq -ext TTL -rep NG -spInitNum 1 -spInitStr pharmkgb_sp_092016 > pharmkgb_results.txt 
	 * java -jar ~/rdf-context-converter-0.0.1-SNAPSHOT.jar -zip -shortenURI -f ncbigenes/nq -ext TTL -rep NG -spInitNum 1 -spInitStr ncbigenes_sp_092016 > ncbigenes_results.txt
	 * java -jar ~/rdf-context-converter-0.0.1-SNAPSHOT.jar -zip -shortenURI -f ncbo/nq -ext TTL -rep NG -spInitNum 1 -spInitStr ncbo_sp_092016 > ncbo_results.txt && java -jar ~/rdf-conversion-0.0.1-SNAPSHOT.jar -f mesh/nq -ext TTL -rep NG -spInitNum 1 -spInitStr mesh_sp_092016 > mesh_results.txt && java -jar ~/rdf-conversion-0.0.1-SNAPSHOT.jar -f goa/nq -ext TTL -rep NG -spInitNum 1 -spInitStr goa_sp_092016 > goa_results.txt && java -jar ~/rdf-conversion-0.0.1-SNAPSHOT.jar -f pharmgkb/nq -ext TTL -rep NG -spInitNum 1 -spInitStr pharmgkb_sp_092016 > pharmgkb_results.txt && java -jar ~/rdf-conversion-0.0.1-SNAPSHOT.jar -f ncbigenes/nq -ext TTL -rep NG -spInitNum 1 -spInitStr ncbigenes_sp_092016 > ncbigenes_results.txt	 
	 * */
	
	public static void main(String[] args) {

		SPConverter conversion = new SPConverter();
		conversion.parseParameters(args);
		System.out.println(conversion.getExt() + "\t" + conversion.getFileIn() + "\t" + conversion.getRep());
		
		conversion.start();
	}
	
	public void start(){
		
		// Prepare the data from url
		if (this.getUrl() != null && this.getDsName() != null){
			RDFReadUtils.fetchLinks(url, dsName);
			return;
		}
		
		if (this.getRep() != null){

			SPParser parser = SPParserFactory.createParser(rep, this.get_uuidInitNum(), this.get_uuidInitStr());

			parser.setInfer(isInfer());
			parser.setZip(isZip());
			parser.setExt(this.getExt());
			parser.setDsName(this.getDsName());
			parser.setPrefix(this.getPrefix());
			parser.setShortenURI(this.isShortenURI());
			parser.setOntoDir(this.getOntoDir());
			parser.setParallel(this.getParallel());
			
			parser.init();
			
			parser.parse(this.getFileIn(), this.getExt(), this.getRep());
		}
		
	}
		
	protected void parseParameters(String[] args) {

		for (int i = 0; i < args.length; i++) {

			// Get input file
			if (args[i].toLowerCase().equals("-f")) {
//				System.out.println("File in: " + args[i + 1]);
				String filename = args[i + 1];
				if (!Files.exists(Paths.get(filename))){
					System.out.println("File " + filename + " does not exist.\n");
					return;
				}
				this.setFileIn(filename);
			}
			// Get zip para
			if (args[i].toLowerCase().equals("-zip")) {
//				System.out.println("File in: " + args[i + 1]);
				this.setZip(true);;
			}
			// Get infer para
			if (args[i].toLowerCase().equals("-infer")) {
//				System.out.println("File in: " + args[i + 1]);
				this.setInfer(true);
				this.setOntoDir(args[i+1]);
			}
			
			// Get infer para
			if (args[i].toLowerCase().equals("-dsname")) {
//				System.out.println("File in: " + args[i + 1]);
				this.setDsName(args[i+1]);;
			}
			// Get prefix para
			if (args[i].toLowerCase().equals("-prefix")) {
//				System.out.println("File in: " + args[i + 1]);
				String filename = args[i + 1];
				if (!Files.exists(Paths.get(filename))){
					System.out.println("File " + filename + " does not exist.\n");
					return;
				}
				this.setPrefix(filename);
			}
			// Get shortenURI para
			if (args[i].toLowerCase().equals("-shortenuri")) {
				this.setShortenURI(true);
//				System.out.println("Shorten URI commandline: " + this.isShortenURI());
			}
			
			// Get url to start with
			if (args[i].toLowerCase().equals("-url")){
				this.setUrl(args[i+1]);
			}

			// Get url to start with
			if (args[i].toLowerCase().equals("-parallel")){
				this.setParallel(Integer.parseInt(args[i+1]));
			}

			// Get url to start with
			if (args[i].toLowerCase().equals("-buffersize")){
				this.setBufferSizeStream(Integer.parseInt(args[i+1]));
				this.setBufferSizeWriter(Integer.parseInt(args[i+1]));
			}

			// Get input file extension
			if (args[i].toLowerCase().equals("-rep")) {
				switch (args[i + 1].toUpperCase()) {
					case Constants.REI_REP:
						this.rep = Constants.REI_REP;
						break;
					case Constants.NANO_REP:
						this.rep = Constants.NANO_REP;
						break;
					case Constants.NG_REP:
						this.rep = Constants.NG_REP;
						break;
					case Constants.TRIPLE_REP:
						this.rep = Constants.TRIPLE_REP;
						break;
					default:
						this.rep = Constants.NONE_REP;
						break;
				}
			}
			if (args[i].toLowerCase().equals("-ext")) {
				switch (args[i + 1].toLowerCase()) {
					case Constants.NTRIPLE_EXT:
						this.ext = Constants.NTRIPLE_EXT;
						break;
					case Constants.TURTLE_EXT:
						this.ext = Constants.TURTLE_EXT;
						break;
					default:
						this.ext = Constants.TURTLE_EXT;
						break;
				}
			}
						
			if (args[i].toLowerCase().equals("-metaprop")) {
				this.metaProp = args[i+1];
			}
			
			if (args[i].toLowerCase().equals("-metaobj")) {
				this.metaObj = args[i+1];
			}
			
			if (args[i].toLowerCase().equals("-spprop")) {
				this.spProp = args[i+1];
			}
			
			if (args[i].toLowerCase().equals("-spinitnum")) {
				this._uuidInitNum = Long.parseLong(args[i+1]);
			}
			if (args[i].toLowerCase().equals("-spinitstr")) {
				this._uuidInitStr = args[i+1];
			}

		}

		// Check if input file is provided
		if (this.getFileIn() == null && this.getUrl() == null) {
			System.out.println("Input file or folder must be provided.");
			return;
		}

		// Check if input file is provided
		if (this.getExt() == null && this.getUrl() == null) {
			System.out.println("Input file extension must be provided.");
			return;
		}
		
	}
	
	public void setRep(String rep) {
		this.rep = rep;
	}

	public String getRep() {
		return this.rep;
	}


	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

	public String getExt() {
		return this.ext;
	}

	public void setFileIn(String filename) {
		this.fileIn = filename;
	}

	public String getFileIn() {
		return this.fileIn;
	}

	public String getDsName() {
		return dsName;
	}

	public void setDsName(String dsName) {
		this.dsName = dsName;
	}

	public String get_uuidInitStr() {
		return _uuidInitStr;
	}

	public void set_uuidInitStr(String _uuidInitStr) {
		this._uuidInitStr = _uuidInitStr;
	}

	public long get_uuidInitNum() {
		return _uuidInitNum;
	}

	public void set_uuidInitNum(long _uuidInitNum) {
		this._uuidInitNum = _uuidInitNum;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getOntoDir() {
		return ontoDir;
	}

	public void setOntoDir(String ontoDir) {
		this.ontoDir = ontoDir;
	}

	public boolean isInfer() {
		return infer;
	}

	public void setInfer(boolean infer) {
		this.infer = infer;
	}

	public boolean isZip() {
		return zip;
	}

	public void setZip(boolean zip) {
		this.zip = zip;
	}

	public String getMetaProp() {
		return metaProp;
	}

	public void setMetaProp(String metaProp) {
		this.metaProp = metaProp;
	}

	public String getMetaObj() {
		return metaObj;
	}

	public void setMetaObj(String metaObj) {
		this.metaObj = metaObj;
	}

	public String getSpProp() {
		return spProp;
	}

	public void setSpProp(String spProp) {
		this.spProp = spProp;
	}

	public boolean isShortenURI() {
		return shortenURI;
	}

	public void setShortenURI(boolean shortenURI) {
		this.shortenURI = shortenURI;
	}

	public int getParallel() {
		return parallel;
	}

	public void setParallel(int parallel) {
		this.parallel = parallel;
	}

	public int getBufferSizeStream() {
		return bufferSizeStream;
	}

	public void setBufferSizeStream(int bufferSizeStream) {
		this.bufferSizeStream = bufferSizeStream;
	}

	public int getBufferSizeWriter() {
		return bufferSizeWriter;
	}

	public void setBufferSizeWriter(int bufferSizeWriter) {
		this.bufferSizeWriter = bufferSizeWriter;
	}


}
