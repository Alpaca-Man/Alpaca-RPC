package io.gitee.birdsofprey.protocol;

import java.io.Serializable;

/**
 * 请求对象
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public class AlpacaRequest implements Serializable {
    /**
     * 请求编号
     */
    private Integer id;
    private String interfaceName;
    private String methodName;
    private Class<?>[] parameterType;
    private Object[] args;

    public AlpacaRequest() {
    }

    public AlpacaRequest(Integer id, String interfaceName, String methodName, Class<?>[] parameterType, Object[] args) {
        this.id = id;
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.parameterType = parameterType;
        this.args = args;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterType() {
        return parameterType;
    }

    public void setParameterType(Class<?>[] parameterType) {
        this.parameterType = parameterType;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
