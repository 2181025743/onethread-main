package com.nageoffer.onethread.dashboard.dev.server.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.nageoffer.onethread.dashboard.dev.server.config.OneThreadProperties;
import com.nageoffer.onethread.dashboard.dev.server.dto.ProjectInfoRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.remote.client.NacosProxyClient;
import com.nageoffer.onethread.dashboard.dev.server.remote.dto.NacosConfigDetailRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.remote.dto.NacosConfigRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.remote.dto.NacosServiceListRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 项目接口实现层
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-18
 */
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final NacosProxyClient nacosProxyClient;
    private final OneThreadProperties oneThreadProperties;

    @Override
    public List<ProjectInfoRespDTO> listProject() {
        List<ProjectInfoRespDTO> projects = new ArrayList<>();

        List<String> namespaces = oneThreadProperties.getNamespaces();
        namespaces.forEach(namespace -> {
            List<NacosConfigRespDTO> nacosConfigResponse = nacosProxyClient.listConfig(namespace);
            if (CollUtil.isEmpty(nacosConfigResponse)) {
                return;
            }
            nacosConfigResponse
                    .stream().filter(each -> StrUtil.isNotBlank(each.getAppName()))
                    .forEach(config -> {
                        // 此处应根据配置文件的类型进行判断，比如 YAML 或者 Properties，为了简化非核心流程，默认处理 YAML
                        Yaml yaml = new Yaml();
                        Map<String, Object> map;

                        try {
                            map = yaml.load(config.getContent());
                        } catch (Exception e) {
                            return;
                        }

                        Map<String, Object> onethread = (Map<String, Object>) map.get("onethread");
                        Object executorsObj = onethread.get("executors");

                        int executorCount = 0;
                        if (executorsObj instanceof List) {
                            executorCount = ((List<?>) executorsObj).size();
                        }

                        NacosConfigDetailRespDTO configDetail = nacosProxyClient.getConfig(namespace, config.getDataId(), config.getGroup());
                        NacosServiceListRespDTO serviceDetails = nacosProxyClient.getService(namespace, config.getAppName());
                        projects.add(
                                ProjectInfoRespDTO.builder()
                                        .namespace(namespace)
                                        .serviceName(config.getAppName())
                                        .instanceCount(serviceDetails.getCount())
                                        .threadPoolCount(executorCount)
                                        .hasWebThreadPool(onethread.containsKey("web"))
                                        .updateTime(DateUtil.formatDateTime(DateUtil.date(configDetail.getModifyTime())))
                                        .build()
                        );
                    });

        });

        return projects;
    }
}
