package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import constant.Constant;
import enums.CsvHeader;
import enums.GitHubEventType;
import enums.Metric;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FileUtil {
  /**
   * download File
   *
   * @param dateTimeString
   * @throws IOException
   * @throws MalformedURLException
   */
  public static void downloadAsJsonFile(final String dateTimeString)
      throws MalformedURLException, IOException {
    log.info(String.format("--Downloading: %s", String.format(Constant.BASE_URL, dateTimeString)));

    final String pathname = String.format(Constant.BASE_UNZIPPED_FILE_NAME, dateTimeString);
    final File downloadedFile = new File(pathname);

    final URLConnection conn =
        new URL(String.format(Constant.BASE_URL, dateTimeString)).openConnection();
    conn.setRequestProperty("User-Agent",
        "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
    conn.connect();

    // decompress gzip then saving to file
    FileUtils.copyInputStreamToFile(new GZIPInputStream(conn.getInputStream()), downloadedFile);

    log.info(String.format("-- Saved to: %s.json", dateTimeString));
  }

  /**
   * @param filePath
   * @param eventType
   * @return
   * @throws IOException
   */
  public static List<String> readLinesByEventType(final String filePath,
      final GitHubEventType eventType) throws IOException {
    List<String> lines = new ArrayList<>();
    final String eventTypeStr = String.format("\"type\":\"%s\"", eventType.value());
    try (LineIterator it = FileUtils.lineIterator(new File(filePath), "UTF-8")) {
      while (it.hasNext()) {
        String line = it.nextLine();
        // do something with line
        if (line.contains(eventTypeStr)) {
          lines.add(line);
        }
      }
    }

    return lines;
  }

  /**
   * list all Json Files
   *
   * @return Set<String>
   * @throws IOException
   */
  public static Set<String> listJsonFiles() throws IOException {
    try (Stream<Path> stream = Files.walk(Paths.get(""), 1)) {
      return stream.filter(file -> !Files.isDirectory(file)).map(Path::getFileName)
          .filter(fileName -> fileName.toString().endsWith(".json")).map(Path::toString)
          .collect(Collectors.toSet());
    }
  }

  /**
   * delete all Json Files
   *
   * @return Set<String>
   * @throws IOException
   */
  public static void deleteJsonFiles() throws IOException {
    try (Stream<Path> stream = Files.walk(Paths.get(""), 1)) {
      stream.filter(file -> !Files.isDirectory(file)).map(Path::getFileName)
          .filter(fileName -> fileName.toString().endsWith(".json")).map(Path::toFile)
          .forEach(File::delete);
    }
  }

  /**
   * create CSV File from data rows
   *
   * @param rows
   * @throws IOException
   */
  public static void createCSVFile(List<String[]> rows) throws IOException {
    FileWriter out = new FileWriter(Constant.OUTPUT_FILE_NAME);

    List<String> headers =
        Stream.of(CsvHeader.values()).map(CsvHeader::name).collect(Collectors.toList());
    headers.addAll(Stream.of(Metric.values()).map(Metric::name).collect(Collectors.toList()));

    try (CSVPrinter printer =
        new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[0])))) {
      // Stream.of(rows).parallel().forEach(printer::printRecord);
      for (String[] row : rows) {
        printer.printRecord(row);
      }
    }
  }


}
