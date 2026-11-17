package jsesh;

/**
 * Encapsulate the version number.
 * <p>We used to have the version as a public constant,
 * but the java compiler did too smart optimisations: it included 
 * the constant in files which used it, and the ant depend target
 * did not understand this, and did not recompile the relevant files
 * (mainly MDCDisplayerAppli).
 * @author rosmord
 */
public class Version {
	private static final String VERSION="@JSESHVERSION@";
	
	public static String getVersion() {
		return VERSION;
	}
}
