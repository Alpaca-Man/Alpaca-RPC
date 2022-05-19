package io.gitee.birdsofprey.protocol;

import java.io.Serializable;

/**
 * 响应对象
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public class AlpacaResponse implements Serializable {
    /**
     * 响应编号
     */
    private Integer id;
    private Object returnValue;
    private String exceptionMessage;

    public AlpacaResponse() {
    }

    public AlpacaResponse(Integer id, Object returnValue, String exceptionMessage) {
        this.id = id;
        this.returnValue = returnValue;
        this.exceptionMessage = exceptionMessage;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }
}
