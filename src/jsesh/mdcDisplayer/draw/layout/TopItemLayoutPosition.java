/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 23 déc. 2004
 *
 */
package jsesh.mdcDisplayer.draw.layout;


/**
 * The current position for laying out a topitem content.
 * @author Serge Rosmorduc
 */
public class TopItemLayoutPosition {

/*
    *//**
     * The translation for the next view.
     *//*
    private RelativePosition nextItemPosition;
    
    *//**
     * The current position.
     *//*
    private Point2D referencePoint;
    
    private DrawingSpecifications drawingSpecifications;
    
    private TextDirection mainTextDirection;

    private TextOrientation currentTextOrientation;
    *//**
     * The box for the whole document.
     *//*
    private Rectangle2D documentBoundingBox;
    
    *//**
     * The box for the zone being built (a line or a column).
     *//*
    private Rectangle2D zoneBoundingBox;
    
    *//**
     * The anchor point for the current zone.
     *//*
    
    private RelativePosition zoneStart;
    
    *//**
     * @return Returns the zoneBoundingBox.
     *//*
    public Rectangle2D getZoneBoundingBox() {
        return zoneBoundingBox;
    }
  
    *//**
     * @return Returns the currentTextOrientation.
     *//*
    public TextOrientation getCurrentTextOrientation() {
        return currentTextOrientation;
    }
    *//**
     * @return Returns the documentBoundingBox.
     *//*
    public Rectangle2D getDocumentBoundingBox() {
        return documentBoundingBox;
    }

    *//**
     * @return Returns the drawingSpecifications.
     *//*
    public DrawingSpecifications getDrawingSpecifications() {
        return drawingSpecifications;
    }
    *//**
     * @return Returns the mainTextDirection.
     *//*
    public TextDirection getMainTextDirection() {
        return mainTextDirection;
    }
    
    *//**
     * @param currentTextOrientation The currentTextOrientation to set.
     *//*
    public void setCurrentTextOrientation(TextOrientation currentTextOrientation) {
        this.currentTextOrientation = currentTextOrientation;
    }

    *//**
     * @param mainTextDirection The mainTextDirection to set.
     *//*
    public void setMainTextDirection(TextDirection mainTextDirection) {
        this.mainTextDirection = mainTextDirection;
    }


    *//**
     * 
     *//*
    public void resetZoneBoundingBox() {
        zoneBoundingBox= new Rectangle2D.Double();
        
    }
    
    public void resetDocumentBoundingBox() {
        documentBoundingBox= new Rectangle2D.Double();
    }
    
    *//**
     * @return Returns the zoneStart.
     *//*
    public RelativePosition getZoneStart() {
        return zoneStart;
    }
    *//**
     * @param zoneStart The zoneStart to set.
     *//*
    public void setZoneStart(RelativePosition zoneStart) {
        this.zoneStart = zoneStart;
    }
    *//**
     * @param drawingSpecifications The drawingSpecifications to set.
     *//*
    public void setDrawingSpecifications(
            DrawingSpecifications drawingSpecifications) {
        this.drawingSpecifications = drawingSpecifications;
    }
    *//**
     * @return Returns the referencePoint.
     *//*
    public Point2D getReferencePoint() {
        return referencePoint;
    }
    *//**
     * @param referencePoint The referencePoint to set.
     *//*
    public void setReferencePoint(Point2D referencePoint) {
        this.referencePoint = referencePoint;
    }
    *//**
     * @return Returns the relativePosition.
     *//*
    public RelativePosition getNextItemPosition() {
        return nextItemPosition;
    }
    *//**
     * @param relativePosition The relativePosition to set.
     *//*
    public void setNextItemPosition(RelativePosition relativePosition) {
        this.nextItemPosition = relativePosition;
    }
*/}
