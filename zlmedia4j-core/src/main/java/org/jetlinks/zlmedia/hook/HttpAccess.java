package org.jetlinks.zlmedia.hook;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpHeaders;

@Getter
@Setter
@ToString
public class HttpAccess extends HookEvent<HookResponse> {

    private HttpHeaders headers;

    private String id;

    private String ip;

    private String isDir;

    private String path;

    private int port;

    @JsonAnySetter
    private void setHeader(String key, String value) {
        if (key.startsWith("header.")) {
            if (headers == null) {
                headers = new HttpHeaders();
            }
            headers.add(key.substring(7), value);
        }

    }
}
