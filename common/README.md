# 1.TIP
        netty的运行需依赖该common包的构建，即通过"mvn clean package -Dcheckstyle.skip=true"命令打包。 将target下的编译文件
    classes/io/netty/util/collection/放入到resources同层级下，即可解决工程io.netty.util编译错误。

