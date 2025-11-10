package com.nageoffer.onethread.dashboard.dev.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.nageoffer.onethread.dashboard.dev.server.common.Result;
import com.nageoffer.onethread.dashboard.dev.server.config.DashBoardConfigProperties;
import com.nageoffer.onethread.dashboard.dev.server.config.OneThreadProperties;
import com.nageoffer.onethread.dashboard.dev.server.dto.WebThreadPoolDetailRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.dto.WebThreadPoolListReqDTO;
import com.nageoffer.onethread.dashboard.dev.server.dto.WebThreadPoolStateRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.dto.WebThreadPoolUpdateReqDTO;
import com.nageoffer.onethread.dashboard.dev.server.remote.client.NacosProxyClient;
import com.nageoffer.onethread.dashboard.dev.server.remote.dto.NacosConfigDetailRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.remote.dto.NacosConfigRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.remote.dto.NacosServiceListRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.remote.dto.NacosServiceRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.service.WebThreadPoolManagerService;
import com.nageoffer.onethread.dashboard.dev.server.service.handler.YamlConfigParser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Web 线程池管理接口实现层
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebThreadPoolManagerServiceImpl implements WebThreadPoolManagerService {

    private final OneThreadProperties oneThreadProperties;
    private final NacosProxyClient nacosProxyClient;
    private final YamlConfigParser yamlConfigParser;

    @Override
    public List<WebThreadPoolDetailRespDTO> listThreadPool(WebThreadPoolListReqDTO requestParam) {
        List<WebThreadPoolDetailRespDTO> threadPools = new ArrayList<>();

        List<String> namespaces = new ArrayList<>(oneThreadProperties.getNamespaces());
        String requestedNamespace = requestParam.getNamespace();
        String requestedServiceName = requestParam.getServiceName();
        if (StrUtil.isNotBlank(requestedNamespace) && namespaces.contains(requestedNamespace)) {
            // 只保留匹配的 namespace
            namespaces.clear();
            namespaces.add(requestedNamespace);
        }

        namespaces.forEach(namespace -> {
            List<NacosConfigRespDTO> nacosConfigResponse = nacosProxyClient.listConfig(namespace);
            if (CollUtil.isNotEmpty(nacosConfigResponse)) {
                nacosConfigResponse
                        .stream()
                        .filter(each -> {
                            if (StrUtil.isBlank(each.getAppName())) {
                                return false;
                            }
                            return StrUtil.isBlank(requestedServiceName) || Objects.equals(each.getAppName(), requestedServiceName);
                        })
                        .forEach(config -> {
                            // 配置列表 API 不返回 content，需要调用详情 API 获取完整配置
                            NacosConfigDetailRespDTO configDetail;
                            try {
                                configDetail = nacosProxyClient.getConfig(namespace, config.getDataId(), config.getGroup());
                            } catch (Exception e) {
                                log.warn("Failed to get config detail. dataId: {}, group: {}, namespace: {}, error: {}",
                                        config.getDataId(), config.getGroup(), namespace, e.getMessage());
                                return;
                            }

                            // 此处应根据配置文件的类型进行判断，比如 YAML 或者 Properties，为了简化非核心流程，默认处理 YAML
                            Map<Object, Object> configInfoMap = yamlConfigParser.doParse(configDetail.getContent());

                            // 将 Map 值绑定到 DashBoardConfigProperties 类属性
                            ConfigurationPropertySource sources = new MapConfigurationPropertySource(configInfoMap);
                            Binder binder = new Binder(sources);

                            DashBoardConfigProperties refresherProperties;
                            try {
                                refresherProperties = binder
                                        .bind("onethread", Bindable.of(DashBoardConfigProperties.class))
                                        .orElseThrow(() -> new IllegalArgumentException("onethread config binding failed"));
                            } catch (Exception e) {
                                return;
                            }

                            NacosServiceListRespDTO service = nacosProxyClient.getService(namespace, config.getAppName());
                            DashBoardConfigProperties.WebThreadPoolExecutorConfig webThreadPoolConfig = refresherProperties.getWeb();
                            if (service == null || CollUtil.isEmpty(service.getServiceList()) || webThreadPoolConfig == null) {
                                return;
                            }

                            NacosServiceRespDTO nacosService = service.getServiceList().get(0);
                            String networkAddress = nacosService.getIp() + ":" + nacosService.getPort();

                            Result<WebThreadPoolStateRespDTO> result;
                            try {
                                String resultStr = HttpUtil.get("http://" + networkAddress + "/web/thread-pool", 1000);
                                result = JSON.parseObject(resultStr, new TypeReference<>() {
                                });
                            } catch (Exception e) {
                                return;
                            }
                            String webContainerName = result.getData().getWebContainerName();

                            WebThreadPoolDetailRespDTO webThreadPool = WebThreadPoolDetailRespDTO.builder()
                                    .webContainerName(webContainerName)
                                    .namespace(namespace)
                                    .serviceName(config.getAppName())
                                    .dataId(config.getDataId())
                                    .group(config.getGroup())
                                    .instanceCount(service.getCount())
                                    .corePoolSize(webThreadPoolConfig.getCorePoolSize())
                                    .maximumPoolSize(webThreadPoolConfig.getMaximumPoolSize())
                                    .keepAliveTime(webThreadPoolConfig.getKeepAliveTime())
                                    .notify(BeanUtil.toBean(webThreadPoolConfig.getNotify(), WebThreadPoolDetailRespDTO.NotifyConfig.class))
                                    .build();
                            threadPools.add(webThreadPool);
                        });
            }
        });

        return threadPools;
    }

    @SneakyThrows
    @Override
    public void updateGlobalThreadPool(WebThreadPoolUpdateReqDTO requestParam) {
        NacosConfigDetailRespDTO configDetail = nacosProxyClient.getConfig(requestParam.getNamespace(), requestParam.getDataId(), requestParam.getGroup());
        String originalContent = configDetail.getContent();

        Map<Object, Object> configInfoMap = yamlConfigParser.doParse(originalContent);
        ConfigurationPropertySource source = new MapConfigurationPropertySource(configInfoMap);

        Binder binder = new Binder(source);
        DashBoardConfigProperties onethread = binder.bind("onethread", Bindable.of(DashBoardConfigProperties.class))
                .orElseThrow(() -> new RuntimeException("binding failed"));

        onethread.setWeb(BeanUtil.toBean(requestParam, DashBoardConfigProperties.WebThreadPoolExecutorConfig.class));

        Map<String, Object> updatedMap = new LinkedHashMap<>();
        updatedMap.put("onethread", onethread);

        YAMLFactory factory = new YAMLFactory();
        factory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER); // 去除 Yaml 字符串开头 ---
        factory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);

        ObjectMapper objectMapper = new ObjectMapper(factory);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // 去除 null 字段
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE); // 驼峰命名转 -，比如 corePooSize 转 core-pool-size

        String yamlStr = objectMapper.writeValueAsString(Collections.singletonMap("onethread", onethread));
        nacosProxyClient.publishConfig(requestParam.getNamespace(), requestParam.getDataId(), requestParam.getGroup(), configDetail.getAppName(), configDetail.getId(), configDetail.getMd5(), yamlStr, "yaml");
    }
}
