package com.traxel.heatmap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * This class represents a map with known longitude and latitude boundaries,
 * and an associated MercatorProjection. It is used to generate MercatorHeatmap
 * instances without having to rebuild the MercatorProjection or to re-read the
 * map image file from disk.
 */
public class MercatorMap {

  public static BufferedImage clone(final BufferedImage original) {
    final ColorModel colorModel = original.getColorModel();
    final boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
    final WritableRaster raster = original.copyData(null);
    return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
  }

  private final BufferedImage map;
  private final MercatorProjection projection;

  public MercatorMap(final String pathToImageFile,
                     final double northLatitude,
                     final double westLongitude,
                     final double southLatitude,
                     final double eastLongitude)
    throws IOException
  {
    this.map = ImageIO.read(new File(pathToImageFile));
    this.projection = new MercatorProjection(map.getWidth(), map.getHeight(),
                                             northLatitude, westLongitude,
                                             southLatitude, eastLongitude);
  }

  public MercatorHeatmap makeHeatmap() {
    final BufferedImage clone = clone(map);
    return new MercatorHeatmap(clone, projection);
  }
}
