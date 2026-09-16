package jsesh.render.elements.cartouche;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import jsesh.model.Cartouche;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.render.style.CartoucheSizeHelper;
import jsesh.render.style.GeometrySpecification;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;

/**
 * Drawer for circular fences (town walls).
 */
class CircularFenceDrawer extends AbstractCartoucheDrawer {
    
    /**
     * Bezier control point offset approximating a quarter circle of radius 1
     * (4/3 * tan(pi/8)).
     */
    private static final double CIRCLE_BEZIER_FACTOR = 0.5522847498307936;

    /**
     * How large a bastion is, relative to the wall's line width. Change this
     * to make the bastions deeper/shallower.
     */
    private static final float BASTION_RADIUS_FACTOR = 1.5f;

    /**
     * The empty wall space left between two neighbouring bastions, in units
     * of the bastion's radius (1 means "roughly one radius of open wall
     * between two bastions"). Change this to make the bastions denser or
     * sparser.
     */
    private static final float BASTION_GAP_FACTOR = 1f;

    public CircularFenceDrawer(JSeshStyle jseshStyle, TextDirection currentTextDirection,
            TextOrientation currentTextOrientation, MDCView currentView, Graphics2D g) {
        super(jseshStyle, currentTextDirection, currentTextOrientation, currentView, g);
    }

    @Override
    public void drawHorizontal(Cartouche cartouche) {
        GeometrySpecification geometry = jseshStyle.geometry();
        Stroke stroke = geometry.cartoucheStroke();
        float w1, w2;
        // The kind of elements found left and right of the cartouche.

        int leftElement, rightElement;

        if (currentTextDirection.isLeftToRight()) {
            leftElement = cartouche.getStartPart();
            rightElement = cartouche.getEndPart();
        } else {
            leftElement = cartouche.getEndPart();
            rightElement = cartouche.getStartPart();
        }

        // Compute horizontal space before and after the cartouche'stroke body :
        w1 = CartoucheSizeHelper.computeCartouchePartLength(jseshStyle,
                cartouche.getType(), leftElement);
        w2 = CartoucheSizeHelper.computeCartouchePartLength(jseshStyle,
                cartouche.getType(), rightElement);

        // Half line width : allows to have a close bounding box.
        float dy = geometry.cartoucheLineWidth() / 2f;
        float dx = geometry.cartoucheLineWidth() / 2f;

        Point2D.Float p1, p2, p3, p4;

        p1 = new Point2D.Float(w1, dy);
        p2 = new Point2D.Float(w1, currentView.getHeight() - dy);
        p3 = new Point2D.Float(currentView.getWidth() - w2, +dy);
        p4 = new Point2D.Float(currentView.getWidth() - w2, currentView
                .getHeight()
                - dy);

        // The necessary skip to get a nice bezier curve for our cartouche
        // loops.
        float loopSkip = geometry.cartoucheLoopLength() / 3;

        // Reference point known to be "inside" the enclosure, used to tell
        // which way is outward when placing bastions along the round ends.
        Point2D.Float center = new Point2D.Float(currentView.getWidth() / 2f, currentView.getHeight() / 2f);

        Path2D.Float outline = new Path2D.Float();
        Point2D.Float leftC1 = null, leftC2 = null;
        // Start
        if (leftElement != 0) {

            float p0x = dx;
            leftC1 = new Point2D.Float(p0x - loopSkip, p1.y);
            leftC2 = new Point2D.Float(p0x - loopSkip, p2.y);
            outline.moveTo(p1.getX(), p1.getY());
            outline.curveTo(leftC1.x, leftC1.y, leftC2.x, leftC2.y, p2.getX(), p2.getY());
        }
        // Middle part.

        outline.moveTo(p1.getX(), p1.getY());
        outline.lineTo(p3.getX(), p3.getY());
        outline.moveTo(p2.getX(), p2.getY());
        outline.lineTo(p4.getX(), p4.getY());

        Point2D.Float rightC1 = null, rightC2 = null;
        // End
        if (rightElement != 0) {

            float p0x = -dx;
            rightC1 = new Point2D.Float(currentView.getWidth() + loopSkip + p0x, p3.y);
            rightC2 = new Point2D.Float(currentView.getWidth() + loopSkip + p0x, p4.y);
            outline.moveTo(p3.getX(), p3.getY());
            outline.curveTo(rightC1.x, rightC1.y, rightC2.x, rightC2.y, p4.getX(), p4.getY());
        }

        // The whole enclosure (wall outline + bastions) is built as a
        // single Area and filled once, so bastions merge seamlessly into
        // the wall instead of being drawn as separate overlapping shapes.
        Area enclosure = new Area(stroke.createStrokedShape(outline));
        drawBastionRow(enclosure, p1, p3, 0, -1); // top wall, bulging up
        drawBastionRow(enclosure, p2, p4, 0, 1);  // bottom wall, bulging down
        if (leftElement != 0) {
            drawBastionsAlongCubic(enclosure, p1, leftC1, leftC2, p2, center);
        }
        if (rightElement != 0) {
            drawBastionsAlongCubic(enclosure, p3, rightC1, rightC2, p4, center);
        }
        g.fill(enclosure);
    }
    // end

    /**
     * The radius (depth) of a bastion. Change {@link #BASTION_RADIUS_FACTOR}
     * to tune it.
     */
    private float bastionRadius() {
        return jseshStyle.geometry().cartoucheLineWidth() * BASTION_RADIUS_FACTOR;
    }

    /**
     * The distance we want between the centers of two neighbouring
     * bastions, given their radius. Change {@link #BASTION_GAP_FACTOR} to
     * tune how much open wall is left between two bastions.
     */
    private float bastionSpacing(float radius) {
        return radius * (2 + BASTION_GAP_FACTOR);
    }

    /**
     * Places a row of bastions evenly along the segment [start, end],
     * bulging towards the (unit) outward normal (nx, ny), and adds each of
     * them to {@code area}.
     */
    private void drawBastionRow(Area area, Point2D.Float start, Point2D.Float end, float nx, float ny) {
        float length = (float) start.distance(end);
        float radius = bastionRadius();
        float spacing = bastionSpacing(radius);
        int n = Math.round(length / spacing);
        if (n <= 0) {
            return;
        }
        float actualSpacing = length / n;
        float ux = (end.x - start.x) / length;
        float uy = (end.y - start.y) / length;
        for (int i = 0; i < n; i++) {
            float t = actualSpacing * (i + 0.5f);
            area.add(new Area(bastionShape(start.x + ux * t, start.y + uy * t, nx, ny)));
        }
    }

    /**
     * Places bastions evenly (by arc length) along the cubic bezier curve
     * (p0, c1, c2, p3), each bulging away from {@code outwardReference}
     * (some point known to be inside the enclosure), and adds each of them
     * to {@code area}.
     */
    private void drawBastionsAlongCubic(Area area, Point2D.Float p0, Point2D.Float c1, Point2D.Float c2,
            Point2D.Float p3, Point2D.Float outwardReference) {
        final int samples = 48;
        Point2D.Float[] pts = new Point2D.Float[samples + 1];
        float[] len = new float[samples + 1];
        for (int i = 0; i <= samples; i++) {
            pts[i] = cubicPoint(p0, c1, c2, p3, (float) i / samples);
            len[i] = (i == 0) ? 0 : len[i - 1] + (float) pts[i - 1].distance(pts[i]);
        }

        float totalLength = len[samples];
        float radius = bastionRadius();
        float spacing = bastionSpacing(radius);
        int n = Math.round(totalLength / spacing);
        if (n <= 0) {
            return;
        }
        float actualSpacing = totalLength / n;

        int segment = 0;
        for (int i = 0; i < n; i++) {
            float target = actualSpacing * (i + 0.5f);
            while (segment < samples - 1 && len[segment + 1] < target) {
                segment++;
            }
            Point2D.Float a = pts[segment];
            Point2D.Float b = pts[segment + 1];
            float segLength = len[segment + 1] - len[segment];
            float fraction = segLength <= 0 ? 0 : (target - len[segment]) / segLength;
            float x = a.x + (b.x - a.x) * fraction;
            float y = a.y + (b.y - a.y) * fraction;

            // The chord a->b approximates the local tangent closely enough
            // for bastion placement (48 samples over one loop).
            float tx = b.x - a.x, ty = b.y - a.y;
            float tLen = (float) Math.hypot(tx, ty);
            if (tLen == 0) {
                continue;
            }
            tx /= tLen;
            ty /= tLen;

            // Of the two perpendiculars to the tangent, keep the one
            // pointing away from the inside of the enclosure.
            float nx = -ty, ny = tx;
            if (nx * (x - outwardReference.x) + ny * (y - outwardReference.y) < 0) {
                nx = -nx;
                ny = -ny;
            }
            area.add(new Area(bastionShape(x, y, nx, ny)));
        }
    }

    private static Point2D.Float cubicPoint(Point2D.Float p0, Point2D.Float c1, Point2D.Float c2, Point2D.Float p3,
            float t) {
        float u = 1 - t;
        float x = u * u * u * p0.x + 3 * u * u * t * c1.x + 3 * u * t * t * c2.x + t * t * t * p3.x;
        float y = u * u * u * p0.y + 3 * u * u * t * c1.y + 3 * u * t * t * c2.y + t * t * t * p3.y;
        return new Point2D.Float(x, y);
    }

    /**
     * Builds the shape of a bastion (typically, added over the loop): a
     * half circle with a flat basis, sitting on a short rectangular stem so
     * its base overlaps the wall it's attached to (avoiding an awkward gap
     * at the seam).
     * We give it a center, and a unit vector pointing toward the curve.
     * @param x coordinate of the center of the bastion
     * @param y coordinate of the center of the bastion
     * @param vx x coordinate of the (unit) orthogonal vector to the basis
     * @param vy y coordinate of the (unit) orthogonal vector to the basis
     */
    private Shape bastionShape(float x, float y, float vx, float vy) {
        float radius = bastionRadius();
        float k = (float) (radius * CIRCLE_BEZIER_FACTOR);
        float stem = radius / 2f;

        // (nx, ny) is v rotated by 90°, i.e. the direction of the flat
        // basis. Every point of the shape is then just x,y plus some
        // combination of v (towards the bulge) and v' (along the basis) -
        // no matrix/AffineTransform needed.
        float nx = -vy;
        float ny = vx;

        float a0x = x - radius * nx, a0y = y - radius * ny; // basis start
        float a1x = x + radius * vx, a1y = y + radius * vy; // tip of the bulge
        float a2x = x + radius * nx, a2y = y + radius * ny; // basis end

        // a0/a2 pushed back by `stem` along -v: the bottom corners of the
        // stem, meant to be buried in the wall so there's no visible gap.
        float b0x = a0x - stem * vx, b0y = a0y - stem * vy;
        float b2x = a2x - stem * vx, b2y = a2y - stem * vy;

        float c1x = x + k * vx - radius * nx, c1y = y + k * vy - radius * ny;
        float c2x = x + radius * vx - k * nx, c2y = y + radius * vy - k * ny;
        float c3x = x + radius * vx + k * nx, c3y = y + radius * vy + k * ny;
        float c4x = x + k * vx + radius * nx, c4y = y + k * vy + radius * ny;

        // Stem (b0 -> a0), half disc (a0 -> a1 -> a2, approximated with two
        // cubic bezier curves rather than Arc2D, for portability), stem
        // (a2 -> b2), then the closing line (b2 -> b0).
        Path2D.Double bastion = new Path2D.Double();
        bastion.moveTo(b0x, b0y);
        bastion.lineTo(a0x, a0y);
        bastion.curveTo(c1x, c1y, c2x, c2y, a1x, a1y);
        bastion.curveTo(c3x, c3y, c4x, c4y, a2x, a2y);
        bastion.lineTo(b2x, b2y);
        bastion.closePath();

        return bastion;
    }

    @Override
    public void drawVertical(Cartouche cartouche) {

        GeometrySpecification geometry = jseshStyle.geometry();
        Stroke s = geometry.cartoucheStroke();
        /*
         * float w1 = drawingSpecifications.getCartoucheStartWidth(cartouche.getType(),
         * cartouche .getStartPart()); float w2 =
         * drawingSpecifications.getCartoucheEndWidth(cartouche.getType(), cartouche
         * .getEndPart());
         */
        float w1, w2;

        // Compute vertical space before and after the cartouche's body :
        w1 = CartoucheSizeHelper.computeCartouchePartLength(jseshStyle,
                cartouche.getType(), cartouche.getStartPart());
        w2 = CartoucheSizeHelper.computeCartouchePartLength(jseshStyle,
                cartouche.getType(), cartouche.getEndPart());

        // Half line width : allows to have a close bounding box.
        float dy = geometry.cartoucheLineWidth() / 2f;
        float dx = geometry.cartoucheLineWidth() / 2f;

        // The respective limits of this cartouche's parts.
        //
        // w1 |
        // |
        // p2 p1
        // ...
        // p4 p3
        // |
        // w2|
        Point2D.Float p1, p2, p3, p4;

        p1 = new Point2D.Float(currentView.getWidth() - dx, w1);
        p2 = new Point2D.Float(dx, w1);
        p3 = new Point2D.Float(currentView.getWidth() - dx, currentView
                .getHeight()
                - dy - w2);
        p4 = new Point2D.Float(dx, currentView.getHeight() - dy - w2);

        // The necessary skip to get a nice bezier curve for our cartouche
        // loops.
        float loopSkip = geometry.cartoucheLineWidth() / 3;

        // Reference point known to be "inside" the enclosure, used to tell
        // which way is outward when placing bastions along the round ends.
        Point2D.Float center = new Point2D.Float(currentView.getWidth() / 2f, currentView.getHeight() / 2f);

        Path2D.Float outline = new Path2D.Float();
        Point2D.Float topC1 = null, topC2 = null;
        // Start
        if (cartouche.getStartPart() != 0) {

            float p0x = dx;

            topC1 = new Point2D.Float(p1.x, p0x - loopSkip);
            topC2 = new Point2D.Float(p2.x, p0x - loopSkip);
            outline.moveTo(p1.getX(), p1.getY());
            outline.curveTo(topC1.x, topC1.y, topC2.x, topC2.y, p2.getX(), p2.getY());
        }

        // Middle part.
        outline.moveTo(p1.getX(), p1.getY());
        outline.lineTo(p3.getX(), p3.getY());
        outline.moveTo(p2.getX(), p2.getY());
        outline.lineTo(p4.getX(), p4.getY());

        Point2D.Float bottomC1 = null, bottomC2 = null;
        // End
        if (cartouche.getEndPart() != 0) {

            float p0x = -dx;

            bottomC1 = new Point2D.Float(p3.x, currentView.getHeight() + loopSkip + p0x);
            bottomC2 = new Point2D.Float(p4.x, currentView.getHeight() + loopSkip + p0x);
            outline.moveTo(p3.getX(), p3.getY());
            outline.curveTo(bottomC1.x, bottomC1.y, bottomC2.x, bottomC2.y, p4.getX(), p4.getY());
        }

        // The whole enclosure (wall outline + bastions) is built as a
        // single Area and filled once, so bastions merge seamlessly into
        // the wall instead of being drawn as separate overlapping shapes.
        Area enclosure = new Area(s.createStrokedShape(outline));
        drawBastionRow(enclosure, p1, p3, 1, 0);  // right wall, bulging right
        drawBastionRow(enclosure, p2, p4, -1, 0); // left wall, bulging left
        if (cartouche.getStartPart() != 0) {
            drawBastionsAlongCubic(enclosure, p1, topC1, topC2, p2, center);
        }
        if (cartouche.getEndPart() != 0) {
            drawBastionsAlongCubic(enclosure, p3, bottomC1, bottomC2, p4, center);
        }
        g.fill(enclosure);
    }

}
