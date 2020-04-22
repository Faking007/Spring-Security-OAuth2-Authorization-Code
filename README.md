## Spring-Security-OAuth2-Authorization-Code
#### Noted:
**Before run application, you shuold config hosts file in system (win 10)**
1. Go to *C:\Windows\System32\drivers\etc*
2. Open file *hosts*
3. Append below string to file *hosts*
4. After that, you can run application, and access <http://client:8080> to use OAuth2 demo
```$java
127.0.0.1			auth-server
127.0.0.1			client
127.0.0.1			resource-server
```
