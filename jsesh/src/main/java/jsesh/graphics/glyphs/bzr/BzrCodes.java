package jsesh.graphics.glyphs.bzr;

/**
 * OPCodes used in BZR files.
 *
 *
 * Created: Sun Jun  2 13:10:09 2002
 *
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC</a>
 *
 */

public interface BzrCodes {

  /**
   * Beginning of char
   */

  int BOC='b';
  int BOC_ABBREV= 'B';

  /**
   * font ending ID 
   */
  int BZR_ID= 'K';
  
  /**
   * char location identifier.
   * Followed by char code and char position
   */
  
  int CHAR_LOC= 'c';
  int CHAR_LOC_ABBREV= 'C';

  /**
   * EOC : end of char 
   */
  int EOC= 'e';
  
  /**
   * line segment 
   */

  int LINE= 'l';
  int LINE_ABBREV= 'L';
  
  /**
   * do nothing code 
   */
  
  int NO_OP= 'x';
  
  /**
   * postamble code 
   */

  int POST= 'P';
  int POST_POST= 'Q';

  /**
   * sline segment 
   */

  int SPLINE= 's';
  int SPLINE_ABBREV= 'S';

  /**
   * new path definition 
   */

  int START_PATH= 'p';

  /**
   * Extensions for screen font and the like.
   * NOT PART OF THE ORIGINAL BZR SPECS.
   */

  /* 
   * non filling stroke.
   */

  int START_CURVE= 't';
  
  /*
   * non filling closed path
   */
  int START_NOFIL= 'O';
  
  /** 
   * future use : pen strike
   * line plus varying line width and pen orientation 
   */
  
  int START_STRIKE= 'g';
  
  /**
   * CIRCLE.
   */
  
  int CIRCLE= 'o';
}// BzrCodes
