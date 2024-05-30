package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component//表示将此类标记为Spring容器中的一个Bean
@ConfigurationProperties(prefix = "sky.alioss")//用于获取配置文件中的属性定义并绑定到Java Bean或属性中
@Data
public class AliOssProperties {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

}
