package jsesh.io.importer.rtf;

public interface RTFTokenType {
	int EOF= -1;
	int COMMAND= -2;
	int NUMERIC_COMMAND= -3;
	int OPEN_GROUP = -4;
	int CLOSE_GROUP = -5;
	int CHAR= 0;

}
