package com.infernalsuite.isdownloadapi;

import com.infernalsuite.isdownloadapi.configuration.AppConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;

@EnableConfigurationProperties({
        AppConfiguration.class
})
@SpringBootApplication
@ServletComponentScan
public class ISDownloadApi {

    public static void main(String[] args) {
        SpringApplication.run(ISDownloadApi.class, args);
    }

}
