# 介绍
主要通过本样例，了解基本的NIO运行原理，以及为什么选择selector?<br/>
![网络数据流示意图](./data_flow.png)
<br/>
如上，网络通信（服务端/客户都安）将会<font color="red">读取数据时阻塞</font>，只有当客户端（服务端）发送数据时，服务端（客户端）的read才会获取到数据从而继续往下执行后续逻辑。</br>
所以有一个selector监听读写事件是非常有必要的！！！<br/>
注意：在SimpleServer和SimpleClient代码，关于客户端SocketChannel都是未配置非阻塞的，即client.configureBlocking(false)。