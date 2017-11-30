SimplePreference
========

`SimplePreference` is a annotation processor for easy SharedPreferences usage.

### Example

```java
// getters
int intValue = ShowCase.getInstance(this).getSampleInt(); // 1
String stringValue = ShowCase.getInstance(this).getSampleString(); // "test"
long longValue = ShowCase.getInstance(this).getSampleLong(); // 1L
float floatValue = ShowCase.getInstance(this).getSampleFloat(); // 1f

// setter
ShowCase.getInstance(this).setSampleInt(2);
ShowCase.getInstance(this).setSampleString("new_test");
ShowCase.getInstance(this).setSampleLong(2L);
ShowCase.getInstance(this).setSampleFloat(2f)
```

Download
--------
### root gradle script
``` 
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }  // add this repository
    }
}
```

### module gradle script
```
compile 'com.github.genju83.simplepreference:simplepreference:x.y.z'
annotationProcessor 'com.github.genju83.simplepreference:simplepreference-compiler:x.y.z'
```


License
-------

    Copyright 2017 Jungwook Park

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
