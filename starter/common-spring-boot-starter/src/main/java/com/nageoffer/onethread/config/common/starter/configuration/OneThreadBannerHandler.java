package com.nageoffer.onethread.config.common.starter.configuration;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;
import org.springframework.boot.info.BuildProperties;

/**
 * oneThread Banner 打印组件
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-13
 */
@Slf4j
public class OneThreadBannerHandler implements InitializingBean {

    private static final String DYNAMIC_THREAD_POOL = " :: Dynamic ThreadPool :: ";
    private static final String ONE_THREAD_DASHBOARD = "Git:    https://gitcode.net/nageoffer/onethread";
    private static final String ONE_THREAD_SITE = "Site:   https://nageoffer.com/onethread";
    private static final int STRAP_LINE_SIZE = 50;
    private final String version;

    public OneThreadBannerHandler(BuildProperties buildProperties) {
        this.version = buildProperties != null ? buildProperties.getVersion() : "";
    }

    @Override
    public void afterPropertiesSet() {
        String banner = """
                                    _______ __                        __
                .-----.-----.-----.|_     _|  |--.----.-----.---.-.--|  |
                |  _  |     |  -__|  |   | |     |   _|  -__|  _  |  _  |
                |_____|__|__|_____|  |___| |__|__|__| |_____|___._|_____|
                
                """;
        String bannerVersion = StrUtil.isNotEmpty(version) ? " (v" + version + ")" : "no version.";
        StringBuilder padding = new StringBuilder();
        while (padding.length() < STRAP_LINE_SIZE - (bannerVersion.length() + DYNAMIC_THREAD_POOL.length())) {
            padding.append(" ");
        }
        System.out.println(AnsiOutput.toString(banner, AnsiColor.GREEN, DYNAMIC_THREAD_POOL, AnsiColor.DEFAULT,
                padding.toString(), AnsiStyle.FAINT, bannerVersion, "\n\n", ONE_THREAD_DASHBOARD, "\n", ONE_THREAD_SITE, "\n"));
    }
}
