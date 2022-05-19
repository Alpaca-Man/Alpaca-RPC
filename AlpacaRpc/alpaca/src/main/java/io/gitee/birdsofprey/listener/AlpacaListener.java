package io.gitee.birdsofprey.listener;

/**
 * 监听器, 持有provider/consumer所占用的资源
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public interface AlpacaListener {
    /**
     * 关闭provider/consumer持有的网络资源
     */
    void close();
}
