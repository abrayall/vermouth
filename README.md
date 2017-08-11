<img width="150px" src="https://groceries.morrisons.com/productImages/217/217731011_0_640x640.jpg?identifier=7015836e8608de5c56e6a87c1f6613e6" />

# Vermouth
[Semantic Versioning](http://semver.org) Library for Java

## Quick Start
  1. Download latest version of [vermouth.jar](https://github.com/abrayall/vermouth/releases/download/v0.3.1/vermouth-0.3.1.jar) and add to your project and classpath
  2. Create version.properties in root directory of your project like below
  ```properties
  major=1
  minor=1
  patch=0
  ```
  3. Update build process to ensure that version.properties is packaged in your main jar file
  4. Use Version class in your code to load current version when needed like below
  ```java
    import vermouth.Version;
    ...
    
    System.out.println(Version.getVersion());  // prints out 1.1.0
  ```
  
## Schedule
  - [x] v0.1.0 - Support for versions with major, minor and patch numbers
  - [x] v0.2.0 - Support for version with pre-release and metadata information
  - [x] [v0.3.0](https://github.com/abrayall/vermouth/releases/download/v0.3.1/vermouth-0.3.1.jar) - Support for loading versions from properties files
  - [x] [v0.4.0](https://github.com/abrayall/vermouth/releases/download/v4.0.0/vermouth-4.0.0.jar) - Support for storing version in properties files
  - [ ] v0.5.0 - Maven support
  - [ ] v0.6.0 - Gradle support
  - [ ] v0.7.0 - Ant support
  - [ ] v0.8.0 - Sbt support
  - [ ] v1.0.0 - Support for generating Version.java for runtime support
  
