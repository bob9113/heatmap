package com.traxel.heatmap;

public class MercatorProjection {

  // ----------------------------------------------------
  // Class Definition
  // ----------------------------------------------------

  public static final class Xy {
    public final double x;
    public final double y;
    public Xy(final double x, final double y) {
      this.x = x; this.y = y;
    }
  }

  // ----------------------------------------------------
  // Class Initialization
  // ----------------------------------------------------

  // ----------------------------------------------------
  // Class Methods
  // ----------------------------------------------------

  public static void main(final String[] args) {
    // System.out.println( "----------------------------------" );
    System.out.println( "---- Global Center ---------------" );
    // System.out.println( "----------------------------------" );
    testGlobalCenter();
    // System.out.println( "----------------------------------" );
    System.out.println( "---- US Center -------------------" );
    // System.out.println( "----------------------------------" );
    testUsCenter();
    System.out.println( "---- Chicago -------------------" );
    // System.out.println( "----------------------------------" );
    testChicago();
  }

  private static void testGlobalCenter() {
    // final MercatorProjection mp1 =
    //   // new MercatorProjection(1318.0, 693.0, 49.0, -124.7, 25.1, -66.9);
    //   new MercatorProjection(2047.0, 2042.0, 90.0, -180, -90, 180);
    // // final Xy center = mp1.toXy( (49.0 + 25.1)/2, (-124.7 + -66.9)/2 );
    // // System.out.println("------------------------------");
    // final Xy center = mp1.toXy( 0.0, 0.0 );
    // System.out.println( "Center: X: " + center.x + ", Y: " + center.y );
  }

  private static void testUsCenter() {
    // final MercatorProjection mp1 =
    //   new MercatorProjection(1318.0, 693.0, 49.0, -124.7, 25.1, -66.9);
    // // System.out.println("------------------------------");
    // final Xy center = mp1.toXy( (49.0 + 25.1)/2, (-124.7 + -66.9)/2 );
    // System.out.println( "Center: X: " + center.x + ", Y: " + center.y );
  }

  private static void testChicago() {
    final MercatorProjection mp1 =
      new MercatorProjection(1115.0, 829.0, 42.7, -89.1, 41.0, -86.1);
    // System.out.println("------------------------------");
    Xy target = mp1.toXy( 42.7, -89.1 );
    System.out.println( "target: X: " + target.x + ", Y: " + target.y );
    target = mp1.toXy( 41.5, -87.0625 );
    System.out.println( "target: X: " + target.x + ", Y: " + target.y );
    target = mp1.toXy( 41.0, -86.1 );
    System.out.println( "target: X: " + target.x + ", Y: " + target.y );
  }

  // ----------------------------------------------------
  // Instance Definition
  // ----------------------------------------------------

  private final double cropWidth;
  private final double cropHeight;
  private final double tlLat;
  private final double tlLong;
  private final double brLat;
  private final double brLong;
  private final double fullWidth;
  private final double fullHeight;
  private double xOffset;
  private double yOffset;

  // ----------------------------------------------------
  // Instance Initialization
  // ----------------------------------------------------

  public MercatorProjection(final double cropWidth, final double cropHeight,
                            final double tlLat, final double tlLong,
                            final double brLat, final double brLong) {
    this.cropWidth = cropWidth;
    this.cropHeight = cropHeight;
    double fullWidth = cropWidth;
    double fullHeight = cropHeight;
    double xOffset = 0;
    double yOffset = 0;
    if (tlLat < 81.9 || tlLong > -179.9 || brLat > -81.9 || brLong < 179.9) {
      final MercatorProjection scaler =
        new MercatorProjection(1.0, 1.0, 85.05, -180.0, -85.05, 180.0);
      final Xy tlXy = scaler.toXy(tlLat, tlLong);
      final Xy brXy = scaler.toXy(brLat, brLong);
      System.out.println( "scale tlX: " + tlXy.x + ", tlY: " + tlXy.y );
      System.out.println( "scale brX: " + brXy.x + ", brY: " + brXy.y );
      final double scaleWidth = brXy.x - tlXy.x;
      final double scaleHeight = brXy.y - tlXy.y;
      fullWidth = cropWidth / scaleWidth;
      fullHeight = cropHeight / scaleHeight;
      xOffset = tlXy.x * fullWidth;
      yOffset = tlXy.y * fullHeight;
      System.out.println( "scaleWidth: " + scaleWidth );
      System.out.println( "scaleHeight: " + scaleHeight );
      System.out.println( "fullwidth: " + fullWidth );
      System.out.println( "fullHeight: " + fullHeight );
      System.out.println( "xOffset: " + xOffset );
      System.out.println( "yOffset: " + yOffset );
    }
    this.fullWidth = fullWidth;
    this.fullHeight = fullHeight;
    this.tlLat = tlLat;
    this.tlLong = tlLong;
    this.brLat = brLat;
    this.brLong = brLong;
    this.xOffset = xOffset;
    this.yOffset = yOffset;

    final Xy topLeft = toXy( tlLat, tlLong );
    System.out.println( "top left xy: " + topLeft.x + ", " + topLeft.y );
    this.xOffset = this.xOffset - topLeft.x;
    this.yOffset = this.yOffset + topLeft.y;
  }

  // ----------------------------------------------------
  // Instance Methods
  // ----------------------------------------------------

  public Xy toXy(final double latitude, final double longitude) {
    final double x = (longitude + 180.0)*(fullWidth/360.0);

    // get y value
    final double latRad = latitude * Math.PI / 180.0;
    final double tanArg = (Math.PI/4.0) + (latRad/2.0);
    final double logArg = Math.tan(tanArg);
    final double mercatorN = Math.log(logArg);
    final double y = (fullHeight/2) - (fullWidth*mercatorN/(2*Math.PI));

    // System.out.println("latitude: " + latitude);
    // System.out.println("latRad: " + latRad);
    // System.out.println("tanArg: " + tanArg);
    // System.out.println("logArg: " + logArg);
    // System.out.println("mercatorN: " + mercatorN);
    // System.out.println("y: " + y);

    // 'x' => ($lng+180)*($width/360),
    // 'y' => ($height/2)-($width*log(tan((M_PI/4)+(($lat*M_PI/180)/2)))/(2*M_PI))
    
    return new Xy(x - xOffset, y - yOffset);
  }
}
