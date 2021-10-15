/**
 * 
 */
package utils.populate;

/**
 * @author tnoble
 *
 */
@SuppressWarnings("serial")
public class ProcessorException extends Exception {
	protected final boolean fatal;
	
	/**
	 * @param msg
	 * @param fatal
	 */
	public ProcessorException(String msg, boolean fatal) {
		super(msg);
		this.fatal = fatal;
	}

	/**
	 * @return the fatal
	 */
	public boolean isFatal() {
		return fatal;
	}
	
}
