package wxyz.dcmj.dicom;

public class DicomException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7755904404264861503L;
	
	public DicomException() {
		super();
	}

	public DicomException(String message) {
		super(message);
	}

	public DicomException(String message, Throwable cause) {
		super(message, cause);
	}

	public DicomException(Throwable cause) {
		super(cause);
	}

}
