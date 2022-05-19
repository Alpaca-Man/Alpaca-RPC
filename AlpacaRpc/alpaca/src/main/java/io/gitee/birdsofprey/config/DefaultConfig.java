package io.gitee.birdsofprey.config;

/**
 * 默认配置
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public interface DefaultConfig {
    /****************************** LengthFieldBasedFrameDecoder参数配置 ******************************/
    int MAX_FRAME_LENGTH = 1 << 30;
    int LENGTH_FIELD_OFFSET = 0;
    int LENGTH_FIELD_LENGTH = 4;
    int LENGTH_ADJUSTMENT = 0;
    int INITIAL_BYTES_TO_STRIP = 4;

    /****************************** ProviderNetManager参数配置 ******************************/
    int PARENT_THREADS = 1;
    int CHILD_THREADS = 0;

    /****************************** ConsumerNetManager参数配置 ******************************/
    int GROUP_THREADS = 0;

    /**************************************** 身份 ****************************************/
    String PROVIDER = "provider";
    String CONSUMER = "consumer";
    int NODE_WEIGHT = 10;

    /**************************************** 注册中心 ****************************************/
    String ZOOKEEPER_REGISTER = "zookeeper";
    String REDIS_REGISTRY = "redis";

    /********************************** Zookeeper 注册中心配置 **********************************/
    String ROOT_PATH = "/alpaca";
    int CONNECTION_TIME_OUT = 10_000;

    /********************************** Redis 注册中心配置 **********************************/
    int TTL_TIME = 2;
    int CORE_POOL_SIZE = 2;
}
