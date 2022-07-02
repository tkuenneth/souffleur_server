# Welcome to the Souffleur server

The Souffleur server uses `com.sun.net.httpserver.HttpsServer` to listen to client requests. It uses a keystore that holds a self-signed certificate. To create a keystore you can use something like this:

```
keytool -genkeypair -keyalg RSA -alias selfsigned -keystore souffleur.jks -storepass password -validity 9999 -keysize 2048
```

So, the password would be `password`.