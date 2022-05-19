package io.gitee.birdsofprey.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 服务配置类, 负责读取配置文件有关配置
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
@ConfigurationProperties(prefix = "alpaca")
public class AlpacaProperties {
    private String identity;
    private String registryType;
    private String registryUrl;
    private Integer netPort;
    private String serializer;
    private Integer weight;
    private String loadBalanceStrategy;

    public AlpacaProperties() {
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getRegistryType() {
        return registryType;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

    public String getRegistryUrl() {
        return registryUrl;
    }

    public void setRegistryUrl(String registryUrl) {
        this.registryUrl = registryUrl;
    }

    public Integer getNetPort() {
        return netPort;
    }

    public void setNetPort(Integer netPort) {
        this.netPort = netPort;
    }

    public String getSerializer() {
        return serializer;
    }

    public void setSerializer(String serializer) {
        this.serializer = serializer;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getLoadBalanceStrategy() {
        return loadBalanceStrategy;
    }

    public void setLoadBalanceStrategy(String loadBalanceStrategy) {
        this.loadBalanceStrategy = loadBalanceStrategy;
    }
}
