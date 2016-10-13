package name.vlsergey.jconf2016.browsercap;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile;
import org.ini4j.Profile.Section;

public abstract class AbstractBrowsersCapParser {

  protected static Map<String, BrowserInfo> db;

  private static final Log log = LogFactory
      .getLog(AbstractBrowsersCapParser.class);

  private static List<Section> buildChain(Ini ini,
      String key) {
    List<Section> result = new ArrayList<>();
    while (StringUtils.isNotBlank(key)) {
      Section entry = ini.get(key);
      if (entry == null) {
        break;
      }
      result.add(entry);
      key = unquote(entry.get("Parent"));
    }
    return result;
  }

  private static String deduplicate(
      Map<String, String> stringDeduplicationChache,
      String src) {
    if (src == null)
      return null;
    if (src.length() == 0)
      return StringUtils.EMPTY;

    String already = stringDeduplicationChache.get(src);
    if (already != null) {
      return already;
    } else {
      stringDeduplicationChache.put(src, src);
      return src;
    }
  }

  protected static void loadDatabase() throws IOException {
    if (AbstractBrowsersCapParser.db == null) {
      log.info("Loading db...");
      Config config = new Config();
      config.setEscape(true);
      config.setComment(true);

      Ini ini;
      try (InputStream stream = BrowsersCapParser_JavaPatterns.class
          .getResourceAsStream("full_php_browscap.ini")) {
        ini = new Ini();
        ini.setConfig(config);
        ini.load(stream);
      }

      log.info("Loading db... Done: " + ini.keySet().size()
          + " patterns. Building browser infos...");

      LinkedHashMap<String, BrowserInfo> database = new LinkedHashMap<String, BrowserInfo>(
          ini.size());
      Map<String, String> stringDeduplicationChache = new HashMap<String, String>(
          ini.size() * 5);
      for (Map.Entry<String, Profile.Section> entry : ini
          .entrySet()) {
        final String key = entry.getKey();
        if (key.equals("comments")
            && key.equals("GJK_Browscap_Version"))
          continue;

        database.put(
            key,
            toBrowserInfo(ini, stringDeduplicationChache,
                key));
      }
      AbstractBrowsersCapParser.db = Collections
          .unmodifiableMap(database);
      log.info("Building browser infos... Done.");
    }
  }

  private static BrowserInfo toBrowserInfo(Ini ini,
      Map<String, String> strCache, String resultKey) {
    List<Profile.Section> first = buildChain(ini, resultKey);
    Collections.reverse(first);
    Map<String, String> result = new TreeMap<>();
    for (Profile.Section section : first) {
      for (Map.Entry<String, String> entry : section
          .entrySet()) {
        result.put(entry.getKey(),
            unquote(entry.getValue()));
      }
    }

    final String browser = deduplicate(strCache,
        result.get("Browser"));
    final String browser_version = deduplicate(strCache,
        result.get("Version"));
    final String device_brand_name = deduplicate(strCache,
        result.get("Device_Brand_Name"));
    final String device_code_name = deduplicate(strCache,
        result.get("Device_Code_Name"));
    final String device_name = deduplicate(strCache,
        result.get("Device_Name"));
    final String device_type = deduplicate(strCache,
        result.get("Device_Type"));
    final boolean is_crawler = Boolean.valueOf(result
        .get("Crawler"));
    final boolean is_mobile = Boolean.valueOf(result
        .get("isMobileDevice"));
    final boolean is_tablet = Boolean.valueOf(result
        .get("isTablet"));
    final String platform = deduplicate(strCache,
        result.get("Platform"));
    final String platform_version = deduplicate(strCache,
        result.get("Platform_Version"));

    final BrowserInfo browserInfo = new BrowserInfo(
        browser, browser_version, device_brand_name,
        device_code_name, device_name, device_type,
        is_crawler, is_mobile, is_tablet, platform,
        platform_version);
    return browserInfo;
  }

  private static String unquote(String value) {
    if (value != null) {
      if (value.startsWith("\"") && value.endsWith("\"")) {
        value = value.substring(1, value.length() - 1);
      }
    }
    return value;
  }

  public abstract BrowserInfo getBrowserInfo(
      String userAgent) throws Exception;

  public abstract String getPattern(String userAgent)
      throws Exception;

  @PostConstruct
  public void init() throws Exception {
    AbstractBrowsersCapParser.loadDatabase();
  }
}
