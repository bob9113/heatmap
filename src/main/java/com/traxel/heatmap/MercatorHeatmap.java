package com.traxel.heatmap;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import static com.traxel.heatmap.MercatorProjection.Xy;

public class MercatorHeatmap {

  // ----------------------------------------------------
  // Class Parameters
  // ----------------------------------------------------

  public static final float ALPHA = 1.0f; // 0.75f;
  public static final Color[] COLORS = new Color[]{
    new Color( 0.0f, 0.0f, 1.0f, ALPHA ),
    new Color( 0.4f, 0.4f, 1.0f, ALPHA ),
    new Color( 0.8f, 0.8f, 0f, ALPHA ),
    new Color( 1.0f, 1.0f, 0f, ALPHA ),
    new Color( 1.0f, 0.8f, 0f, ALPHA ),
    new Color( 1.0f, 0.6f, 0f, ALPHA ),
    new Color( 1.0f, 0.4f, 0f, ALPHA ),
    new Color( 1.0f, 0.2f, 0f, ALPHA ),
    new Color( 1.0f, 0.0f, 0f, ALPHA )
  };

  // ----------------------------------------------------
  // Instance Parameters
  // ----------------------------------------------------

  private final BufferedImage map;
  private final Graphics2D graphics;
  private final MercatorProjection projection;

  // ----------------------------------------------------
  // Instance Initialization
  // ----------------------------------------------------

  /**
   * This mutates the map BufferedImage you pass in.
   * latitude and longitude are in degrees, extents of map image
   */
  public MercatorHeatmap(final BufferedImage map,
                         final MercatorProjection projection) {
    this.map = map;
    this.projection = projection;
    this.graphics = map.createGraphics();
  }

  // ----------------------------------------------------
  // Instance Methods
  // ----------------------------------------------------

  public BufferedImage getImage() {
    return map;
  }

  public void addLabel(final String label) {
    graphics.setPaint(Color.BLACK);
    graphics.setFont(new Font(graphics.getFont().getFontName(), Font.PLAIN, 50));
    graphics.drawString(label, 50, map.getHeight() - 50);
  }

  public void writePng(final String path) throws IOException {
    ImageIO.write(getImage(), "png", new File(path));
  }

  public void fillGrid(final double centerLatitude,
                       final double centerLongitude,
                       final double gridSizeDegrees,
                       final Color fill) {
    final Xy tl = projection.toXy(centerLatitude + (gridSizeDegrees/2),
                                  centerLongitude - (gridSizeDegrees/2));
    final Xy tr = projection.toXy(centerLatitude + (gridSizeDegrees/2),
                                  centerLongitude + (gridSizeDegrees/2));
    final Xy bl = projection.toXy(centerLatitude - (gridSizeDegrees/2),
                                  centerLongitude - (gridSizeDegrees/2));
    final Xy br = projection.toXy(centerLatitude - (gridSizeDegrees/2),
                                  centerLongitude + (gridSizeDegrees/2));
    System.out.println( "filling: { " + centerLatitude + ", " + centerLongitude + " } "
                        + tl.x + ", " + tl.y
                        + " to " + br.x + ", " + br.y );
    final int[] polyX = new int[]{(int) tl.x, (int) tr.x, (int) br.x, (int) bl.x};
    final int[] polyY = new int[]{(int) tl.y, (int) tr.y, (int) br.y, (int) bl.y};
    graphics.setPaint(fill);
    graphics.setColor(fill);
    graphics.fillPolygon(polyX, polyY, 4);
  }
}

                        
// image: 18 label: 2016-10-19 00:00:00.0
// filling: { 41.5, -87.0625 } 745.65625, 185.5709895577893 to 768.8854166666642, 216.58640071832633
// filling: { 41.5625, -87.25 } 675.96875, 154.52559846351505 to 699.1979166666642, 185.5709895577893
// Writing: 1d-prop-16th-frames//0018.png
