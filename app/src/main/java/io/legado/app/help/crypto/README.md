# 加解密工具

提供对称加密、非对称加密和数字签名功能，主要供书源规则中的 Rhino JS 脚本调用。

参考 PR: https://github.com/gedoor/legado/pull/2880

## 文件说明

### `AsymmetricCrypto.kt`
- **功能**: 非对称加密（RSA 等），支持公钥/私钥加密解密。非对称加密一般只能知道其中一个密钥，而 RhinoJS 调用 java 方法不能传入 null 和 KeyType，因此提供以下重载函数
- **使用页面**: 书源规则 JS 脚本中的加密操作（如 `java.crypto()`）

```kotlin
fun setPublicKey(key: ByteArray): T
fun setPublicKey(key: String): T
fun setPrivateKey(key: ByteArray): T
fun setPrivateKey(key: String): T

fun decrypt(data: Any, usePublicKey: Boolean? = true): ByteArray?
fun decryptStr(data: Any, usePublicKey: Boolean? = true): String?

fun encrypt(data: Any, usePublicKey: Boolean? = true): ByteArray?
fun encryptHex(data: Any, usePublicKey: Boolean? = true): String?
fun encryptBase64(data: Any, usePublicKey: Boolean? = true): String?
```

### `SymmetricCryptoAndroid.kt`
- **功能**: 对称加密（AES、DES、3DES 等），基于 Android 平台的 `javax.crypto` 实现。支持多种加密模式（ECB/CBC/CTR）和填充方式
- **使用页面**: 书源规则 JS 脚本中的对称加密操作、备份数据 AES 加密

### `Sign.kt`
- **功能**: 数字签名工具，支持 SHA256withRSA 等签名算法，用于验证数据完整性和来源
- **使用页面**: 书源规则 JS 脚本中的签名验证操作