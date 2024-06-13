package org.jetlinks.zlmedia.restful;

import lombok.Getter;
import lombok.Setter;
import org.jetlinks.zlmedia.ZLMediaOperations;

import java.io.File;
import java.util.*;

@Getter
@Setter
public class ZLMediaConfigs {
    private static final List<String> ALL_HOOKS =
        Arrays.asList(
            "on_flow_report",
            "on_http_access",
            "on_play",
            "on_publish",
            "on_record_mp4",
            "on_record_ts",
            "on_rtsp_auth",
            "on_rtsp_realm",
            "on_shell_login",
            "on_stream_changed",
            "on_stream_none_reader",
            "on_server_started",
            "on_server_exited",
            "on_server_keepalive",
            "on_send_rtp_stopped",
            "on_rtp_server_timeout"
        );

    /**
     * 数据存储路径,截图,录像等数据存储到此目录.
     */
    private String dataPath = "data/zlmedia";

    /**
     * hook 通知的根地址,如: <code>http://127.0.0.1:8080/zlmedia/hook/</code>
     *
     * @see ZLMediaOperations#opsForHook()
     * @see org.jetlinks.zlmedia.restful.RestfulHookOperations#fireEvent(String, String)
     */
    private String hookBasePath;

    /**
     * hook通知的事件类型,默认全部
     */
    private Set<String> hooks = new HashSet<>(ALL_HOOKS);

    /**
     * 服务ID,用于区分不同的服务,在集群通知时可根据参数中的serverId进行集群调用.
     */
    private String serverId;

    /**
     * 端口信息
     */
    private Ports ports = new Ports();

    /**
     * 其他自定义配置
     */
    private Map<String, String> others;

    /**
     * 对外提供服务的域名或者ip地址
     */
    private String publicHost;

    public Map<String, String> createConfigs() {
        Map<String, String> configs = new HashMap<>();

        if (others != null) {
            configs.putAll(others);
        }
        //服务ID
        if (serverId != null) {
            configs.put("general.mediaServerId", serverId);
        }
        //hook配置
        if (hookBasePath != null) {
            configs.put("hook.enable", "1");
            String prefix = hookBasePath;
            if (!prefix.endsWith("/")) {
                prefix = prefix + "/";
            }
            for (String hook : hooks) {
                configs.put("hook." + hook, prefix + hook);
            }
        }

        ports.applyConfig(configs);

        //存储相关配置
        if (dataPath != null) {
            File file = new File(dataPath);
            String path = file.getAbsolutePath();
            configs.put("api.downloadRoot", path);
            configs.put("http.rootPath", path);
            configs.put("api.snapRoot", path + "/snapshot/");
            configs.put("api.defaultSnap", path + "/snapshot.png");
            configs.put("protocol.mp4_save_path", path + "/mp4");
            configs.put("protocol.hls_save_path", path + "/hls");
        }

        if (publicHost != null) {
            configs.put("rtc.externIP", publicHost);
        }
        return configs;
    }

    @Getter
    @Setter
    public static class Ports {
        private int http = 8188;
        private int rtp = 10000;
        private int[] rtpRange = new int[]{30000, 35000};
        private int rtsp = 554;
        private int rtmp = 1935;
        private int rtc = 8000;
        private int srt = 9000;

        public void applyConfig(Map<String, String> config) {
            config.put("http.port", String.valueOf(http));

            config.put("rtp_proxy.port", String.valueOf(rtp));
            config.put("rtp_proxy.port_range", rtpRange[0] + "-" + rtpRange[1]);

            config.put("rtsp.port", String.valueOf(rtsp));
            config.put("rtmp.port", String.valueOf(rtmp));

            config.put("rtc.port", String.valueOf(rtc));
            config.put("rtc.tcpPort", String.valueOf(rtc));

            config.put("srt.port", String.valueOf(srt));
        }
    }

}
