package io.gitee.birdsofprey.net;

import java.util.Objects;

/**
 * 对Provider url的封装
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public class AlpacaInvoker {
    /**
     * 远程地址
     */
    private final String url;
    /**
     * 节点权重
     */
    private final int weight;

    public AlpacaInvoker(String url, int weight) {
        this.url = url;
        this.weight = weight;
    }

    public String getUrl() {
        return url;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AlpacaInvoker)) {
            return false;
        }
        AlpacaInvoker that = (AlpacaInvoker) o;
        return weight == that.weight &&
                Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, weight);
    }
}
