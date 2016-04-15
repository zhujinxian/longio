# longio

protocol support JSON, MessagePack and Protobuf;

transport support Http, Websocket and Rawsocket.

## Server end

in spring boot:

```java
    @Bean(name="cmdLookup")
	CmdLookup getCmdLookup() {
		return new DefaultCmdLookup(); 
	}
	
	@Bean(name="appLookup")
	AppLookup getAppLookup(@Qualifier("environment") Environment env) {
		return new DefaultAppLookup(new EnvProperties(env)); 
	}
	
	@Bean
	BeanFactoryPostProcessor getLioBeanBeanFactoryPostProcessor(
			@Qualifier("appLookup") AppLookup appLookup, 
			@Qualifier("cmdLookup")CmdLookup cmdLookup) {
		return new LongioBeanFactoryPostProcessor(appLookup, cmdLookup);
	}

	@Boot(port = 5000, pt = ProtocolType.JSONARRAY, tt = TransportType.HTTP)
	@Boot(port = 5002, pt = ProtocolType.JSON, tt = TransportType.HTTP)
	@Boot(port = 5001, pt = ProtocolType.MESSAGE_PACK, tt = TransportType.SOCKET)
	@Bean(name = "longio.bootstrap")
	public LioBootstrap getLioBootstrap() {
		return new LioBootstrap();
	}

```

```java
@Lservice(path = "com.lehuihome")
public class ExeHelloService {
	@Lio(cmd = "getUser")
	@Unpack("com.zhucode.longio.example.message.UserMsg")
	public Map<String, Map<String, UserMsg>>getUser(@Key("user_id")long userId) {
		JSONObject ret = new JSONObject();
		ret.put("status", "success");
		Map<String, UserMsg> rm = new HashMap<String, UserMsg>();
		UserMsg um = new UserMsg();
		um.user_id = 9999;
		rm.put("1234", um);
		
		Map<String, Map<String, UserMsg>> m = new HashMap<String, Map<String, UserMsg>>();
		m.put("1234", rm);
		return m;
	}
	
	@Lio(cmd = "getUser1")
	@Unpack("com.zhucode.longio.example.message.User$Data")
	public Res.Data getUser1(@Key("user_id")long userId) {
		return Res.Data.newBuilder().setStatus("success").build();
	}
	
	@Lio(cmd = "getVoid")
	public void testVoid() {
		System.out.println("++++++++++++test void+++++++++++++++");
	}
	
	@Lio(cmd = "getInt", asy=false)
	public int testInt(@Key("int")int x) {
		return x;
	}
	
	@Lio(cmd = "getString")
	public String testString(@Key("str")String string) {
		return string;
	}
	
	@Lio(cmd = "getStringAsy", asy=false)
	public String testStringAsy(@Key("str")String string) {
		return string;
	}
	
	@Lio(cmd = "getException")
	public void testException() throws LongioException {
		throw new LongioException(1111, "test exception");
	}
	
	@Lio(cmd = "list_to_list")
	public List<String> testLiat(@Key("strs")List<String> strs)  {
		return strs;
	}
	
	@Lio(cmd = "set_to_set")
	public Set<String> testSet(@Key("strs")Set<String> strs) {
		return strs;
	}

}
```

## Client end

```java
@LsAutowired(app = "com.lehuihome", path = "com.lehuihome", tt=TransportType.HTTP, ip="127.0.0.1", port=5002, pt=ProtocolType.JSONARRAY)
public interface HelloService {
	
	@Lio(cmd = "getUser")
	@Pack("com.zhucode.longio.example.message.UserMsg")
	public Map<String, Map<String, UserMsg>> getUser(@Key("user_id")int userId);

	@Lio(cmd = "getVoid")
	public void testVoid();
	
	@Lio(cmd = "getInt")
	public int testInt(@Key("int")int x);
	
	@Lio(cmd = "getString")
	public String testString(@Key("str")String str);
	
	@Lio(cmd = "getException")
	public void testException() throws LongioException;
	
	@Lio(cmd = "list_to_list")
	public List<String> testLiat(@Key("strs")List<String> strs);
	
	@Lio(cmd = "set_to_set")
	public Set<String> testSet(@Key("strs")Set<String> strs);

}

```

try steps:

1. git clone https://github.com/zhujinxian/longio.git
2. cd longio
3. mvn install
4. cd longio-spring
5. mvn install
6. import maven project longio-example to eclipse

using local config enviroment for services hosts and commands:

application.properties

```
app.${app_name}.host = ip:port#weight ip:port#weight ip:port#weight 
```

cmd.properties
```
# name-cmd 

com.lehuihome.getUser = 101
com.lehuihome.getVoid = 102
com.lehuihome.getInt = 103
com.lehuihome.getString = 104
```




