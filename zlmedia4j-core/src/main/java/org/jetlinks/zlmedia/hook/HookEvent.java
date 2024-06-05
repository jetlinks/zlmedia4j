package org.jetlinks.zlmedia.hook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URLDecoder;

@Getter
@Setter
public class HookEvent<R extends HookResponse> {

    private MultiValueMap<String, String> params;


    private void setParams(String str) {
        if (str == null || str.isEmpty()) {
            return;
        }
        if (str.startsWith("?")) {
            str = str.substring(1);
        }
        String[] arr = str.split("&");
        params = new LinkedMultiValueMap<>();
        for (String s : arr) {
            String[] kv = s.split("=");
            if (kv.length == 2) {
                params.add(decodeUrl(kv[0]), decodeUrl(kv[1]));
            }
        }

    }

    private String decodeUrl(String url) {
        try {
            return URLDecoder.decode(url, "UTF-8");
        } catch (Exception e) {
            return url;
        }

    }

}
