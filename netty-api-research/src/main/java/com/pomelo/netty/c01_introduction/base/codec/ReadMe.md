### 网络编解码工具
网络传输编解码，性能较好的三种工具：json、protobuf(多语言)、msgpack(多语言)  
本用例主要用于对netty提供的编解码工具原理的分析和识别。  
在netty中，主要涉及两个关键类：  
- MessageToByteEncoder
- MessageToMessageEncoder

在netty中，使用的序列化：  
- ObjectEncoder
- CompactObjectOutputStream

在netty中,没有提供JSON编码器,但是提供了解码器:  
- JsonObjectDecoder (与普通的json解码器的区别,普通的是将字节流或字符串转换为可用的POJO对象,而netty的json解码器是将持续传输的字节流切割成一个一个的json字节流)