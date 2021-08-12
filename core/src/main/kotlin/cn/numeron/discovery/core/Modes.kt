package cn.numeron.discovery.core

import java.io.Serializable

enum class Modes : Serializable {

    /**
     * 默认的处理方式
     * 扫描全部的class来获取实现类，速度较慢
     */
    Scan,

    /**
     * 仅处理Implementation注解标记的类作为实现类，速度较快
     */
    Mark;

}