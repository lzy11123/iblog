package com.shanzhu.blog.web.core.config;

import com.shanzhu.blog.BackendApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


/**
 * web容器中进行部署
 *
 * @author: ShanZhu
 * @date: 2023-12-09
 */
public class WebServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(BackendApplication.class);
    }
}
