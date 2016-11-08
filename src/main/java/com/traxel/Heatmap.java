package com.traxel;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.traxel.heatmap.MercatorHeatmap;
import com.traxel.heatmap.MercatorMap;

import org.gdal.ogr.Layer;

public class Heatmap {
  
  // -----------------------------------------------------
  // Class Definition
  // -----------------------------------------------------
  
  private static final class Args {
    public final String dataPath;
    public final String mapPath;
    public final double nLat;
    public final double wLong;
    public final double sLat;
    public final double eLong;
    public final String framePath;
    public Args(final String[] args) throws IOException {
      dataPath = args[0];
      mapPath = args[1];
      nLat = Double.parseDouble(args[2]);
      wLong = Double.parseDouble(args[3]);
      sLat = Double.parseDouble(args[4]);
      eLong = Double.parseDouble(args[5]);
      framePath = args[6];
      new File(framePath).mkdirs();
    }
  }

  private static final class Dataset {
    public final String fileName;
    public final String label;
    public final double latitude;
    public final double longitude;
    public final double width;
    public final double deviations;
    public final Color color;
    public Dataset(final String[] parts) {
      fileName = parts[0];
      label = parts[1];
      latitude = Double.parseDouble(parts[2]);
      longitude = Double.parseDouble(parts[3]);
      width = Double.parseDouble(parts[4]);
      deviations = Double.parseDouble(parts[5]);

      // this adjusts all the deviations, "- 2" is not magic, use
      // whatever makes sense for your dataset
      final double normDeviations = deviations - 2;
      final int colorIndex = normDeviations < 0 ? -1
        : normDeviations >= MercatorHeatmap.COLORS.length ? MercatorHeatmap.COLORS.length - 1
        : (int) normDeviations;
      color = colorIndex == -1 ? null : MercatorHeatmap.COLORS[ colorIndex ];
    }
  }

  // -----------------------------------------------------
  // Class Methods
  // -----------------------------------------------------
  
  public static void usage() {
    final StringBuilder buff = new StringBuilder();
    buff
      .append("\n")
      .append("Usage:\n")
      .append("------\n")
      .append("\n")
      .append("$ java -cp foo.jar com.traxel.Heatmap <data/path.csv> <map/path.png> <nlat> <wlong> <slat> <elong> <frame/path/>\n")
      .append("\n")
      .append("First arg is to a tab delimited CSV in the following form:\n")
      .append("fileName\tlabel\tlatitude\tlongitude\twidth\tstddeviations\n")
      .append("0001.png\t2016-10-01\t33\t-80\t0.5\t2.57\n")
      .append("0001.png\t2016-10-01\t33\t-80.5\t0.5\t1.6\n")
      .append("0002.png\t2016-10-02\t33\t-80\t0.5\t1.0\n")
      .append("...\n")
      .append("\n")
      .append("Use the fileName 'auto' if you want numbered files based on label natural sort")
      .append("\n")
      .append("Second arg is to a map graphic\n")
      .append("Third arg is the northmost latitude in the map graphic\n")
      .append("Fourth arg is the westmost longitude in the map graphic\n")
      .append("Fifth arg is the southmost latitude in the map graphic\n")
      .append("Sixth arg is the eastmost longitude in the map graphic\n")
      .append("\n")
      .append("Seventh arg is a path where you want the output files stored\n")
      .append("\n")
      .append("Use included map as follows:\n")
      .append("$ java -cp foo.jar com.traxel.Heatmap <data/path.csv> src/main/resources/img/mercator-us-open-2633x1385.png 49.0 -124.7 25.1 -66.9 <frame/path/>\n")
      .append("\n");
    System.out.println(buff.toString());
  }

  public static void main(final String[] argv) throws IOException {
    if (argv.length != 7) {
      usage();
      System.exit(1);
    }
    final Args args = new Args(argv);
    final Heatmap heatmap = new Heatmap(args);
    heatmap.makeFrames();
  }

  // -----------------------------------------------------
  // Instance Parameters
  // -----------------------------------------------------
  
  private final Args args;
  private final MercatorMap map;
  private final Map<String,List<Dataset>> dataMap = new HashMap<String,List<Dataset>>();
  private boolean autoNumberFiles = false;

  // -----------------------------------------------------
  // Instance Initialization
  // -----------------------------------------------------

  public Heatmap(final Args args) throws IOException {
    this.args = args;
    map = new MercatorMap(args.mapPath, args.nLat, args.wLong, args.sLat, args.eLong);
    loadData(args);
  }
  
  private void loadData(final Args args) throws IOException {
    final BufferedReader reader = new BufferedReader(new FileReader(new File(args.dataPath)));
    String line;
    int lineNum = 0;
    while ((line = reader.readLine()) != null) {
      final String[] parts = line.split("\t");
      if (lineNum == 0) {
        boolean bad = false;
        bad = bad || parts.length != 6;
        bad = bad || ! "filename".equalsIgnoreCase(parts[0]);
        bad = bad || ! "label".equalsIgnoreCase(parts[1]);
        bad = bad || ! "latitude".equalsIgnoreCase(parts[2]);
        bad = bad || ! "longitude".equalsIgnoreCase(parts[3]);
        bad = bad || ! "width".equalsIgnoreCase(parts[4]);
        bad = bad || ! "stddeviations".equalsIgnoreCase(parts[5]);
        if (bad) {
          System.out.println("Improper Header Row:");
          System.out.println(line);
          usage();
          System.exit(1);
        }
      } else {
        final Dataset dataset = new Dataset(parts);
        synchronized(dataMap) {
          String key = dataset.fileName;
          if ("auto".equals(key)) {
            key = dataset.label;
            autoNumberFiles = true;
          }
          List<Dataset> data = dataMap.get(key);
          if (data == null) {
            data = new ArrayList<Dataset>();
            dataMap.put(key, data);
          }
          data.add(dataset);
        }
      }
      lineNum++;
    }
  }

  // -----------------------------------------------------
  // Instance Methods
  // -----------------------------------------------------

  public void makeFrames() throws IOException {
    final List<String> keys = new ArrayList<>(dataMap.keySet());
    Collections.sort(keys);
    int imgSeqNum = 0;
    for (String key : keys) {
      final List<Dataset> data;
      synchronized(dataMap) {
        data = dataMap.get(key);
      }
      String imgFileName;
      if (autoNumberFiles) {
        imgFileName = imgSeqNum < 10 ? "000" + imgSeqNum
          : imgSeqNum < 100 ? "00" + imgSeqNum
          : imgSeqNum < 1000 ? "0" + imgSeqNum
          : "" + imgSeqNum;
        imgFileName += ".png";
      } else {
        imgFileName = data.get(0).fileName;
      }
      final String path = args.framePath + "/" + imgFileName;
      System.out.println( "image: " + imgSeqNum + " label: " + data.get(0).label );
      final MercatorHeatmap heatmap = map.makeHeatmap();
      if(data.get(0).label != null) {
        heatmap.addLabel(data.get(0).label);
      }
      for (Dataset dataset : data) {
        if (dataset.color != null) {
          heatmap.fillGrid(dataset.latitude, dataset.longitude, dataset.width, dataset.color);
        }
      }
      System.out.println( "Writing: " + path );
      heatmap.writePng(path);
      imgSeqNum++;
    }
  }
}
