package name.vlsergey.jconf2016.browsercap;

public class BrowserInfo {

    public static final String UNKNOWN = "unknown";

    public final String browser;

    public final String browser_version;

    public final String device_brand_name;

    public final String device_code_name;

    public final String device_name;

    public final String device_type;

    public final boolean is_crawler;

    public final boolean is_mobile;

    public final boolean is_tablet;

    public final String platform;

    public final String platform_version;

    public BrowserInfo(String browser, String browser_version, String device_brand_name, String device_code_name,
            String device_name, String device_type, boolean is_crawler, boolean is_mobile, boolean is_tablet,
            String platform, String platform_version) {
        super();
        this.browser = browser;
        this.browser_version = browser_version;
        this.device_brand_name = device_brand_name;
        this.device_code_name = device_code_name;
        this.device_name = device_name;
        this.device_type = device_type;
        this.is_crawler = is_crawler;
        this.is_mobile = is_mobile;
        this.is_tablet = is_tablet;
        this.platform = platform;
        this.platform_version = platform_version;
    }

    @Override
    public String toString() {
        return "BrowserInfo [" + browser + " " + browser_version + " @ " + platform + " " + platform_version + " @ "
                + device_brand_name + " " + device_name + " (" + device_code_name + "; " + device_type
                + (is_crawler ? "; crawler" : "") + (is_mobile ? "; mobile" : "") + (is_tablet ? "; tablet" : "")
                + ")]";
    }

}