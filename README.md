# longio

protocol support JSON, MessagePack and Protobuf;

transport support Http, Websocket and Rawsocket.

## Server end

in spring boot:

```java
    @Bean
    BeanFactoryPostProcessor getLioBeanBeanFactoryPostProcessor() {
    	return new LongioBeanFactoryPostProcessor();
    }
    
    @Boot(port = 9000, pt = ProtocolType.JSONARRAY, tt = TransportType.HTTP)
    @Boot(port = 9002, pt = ProtocolType.JSON, tt = TransportType.HTTP)
    @Boot(port = 9001, pt = ProtocolType.MESSAGE_PACK, tt = TransportType.SOCKET)
    @Bean(name="longio.bootstrap")
    LioBootstrap getLioBootstrap() {
    	return new LioBootstrap();
    }

```

```java
@Lservice(path = "com.lehuihome")
public class TestService {
	@Lio(cmd = "getUser")
	@Unpack("com.zhucode.longio.example.message.UserMsg")
	public Map<String, Map<String, UserMsg>>getUser(@Key("user_id")long userId) {
		System.out.println("++++++++++++++++++++++++++++++++++++++");
		JSONObject ret = new JSONObject();
		ret.put("status", "success");
		Map<String, UserMsg> rm = new HashMap<>();
		UserMsg um = new UserMsg();
		um.user_id = 9999;
		rm.put("1234", um);
		
		Map<String, Map<String, UserMsg>> m = new HashMap<>();
		m.put("1234", rm);
		return m;
		//return new String[]{"status", "true", "dddd"};
	}
	
	@Lio(cmd = "getUser1")
	@Unpack("com.zhucode.longio.example.message.User$Data")
	public Res.Data getUser1(@Key("user_id")long userId) {
		System.out.println("++++++++++++++++++++++++++++++++++++++");
		return Res.Data.newBuilder().setStatus("success").build();
	}
	
	@Lio(cmd = "getVoid")
	public void testVoid() {
		System.out.println("++++++++++++test void+++++++++++++++");
	}
	
	@Lio(cmd = "getInt")
	public int testInt() {
		System.out.println("++++++++++++test int+++++++++++++++");
		return 98800;
	}
	
	@Lio(cmd = "getString")
	public String testString() {
		System.out.println("++++++++++++test string+++++++++++++++");
		return "dddddddddfvvvv";
	}

}

```

## Client end

```java
@LsAutowired(app = "com.lehuihome", path = "com.lehuihome", tt=TransportType.SOCKET, ip="127.0.0.1", port=9001, pt=ProtocolType.MESSAGE_PACK)
public interface ClientService {
	
	@Lio(cmd = "getUser")
	@Pack("com.zhucode.longio.example.message.UserMsg")
	public Map<String, Map<String, UserMsg>> getUser(@Key("user_id")int userId);

	@Lio(cmd = "getVoid")
	public void testVoid();
	
	@Lio(cmd = "getInt")
	public int testInt();
	
	@Lio(cmd = "getString")
	public String testString();
}
```
