# JarJarPatcher

## What is JarJarPatcher

JarJarPatcher is a tool for creating a binary patch between two jar files. It uses [jbsdiff](https://github.com/malensek/jbsdiff) on in memory tar files with small patch files that shouldn't contain significant code from the original jar. This is intended to be used to create distributable Minecraft jar mods without causing legal issues.

## Dependencies

Requires Java 6 or later. Additionally, if you are trying to create a new patch file you need to install the original [bsdiff](http://www.daemonology.net/bsdiff/) and add it to your PATH.

## Usage
- Convert Jar/Zip into a tar file (reproducible):  
    >JarJarPatcher jartotar \[Input Jar/Zip\] \[Output Tar\]  
- Convert Tar into a Jar/Zip file (reproducible):  
    >JarJarPatcher tartojar \[Input Tar\] \[Output Jar/Zip\]  
- Patch A Jar/Zip into a New Jar/Zip:  
    >JarJarPatcher patchjar \[Input Jar/Zip\] \[Output Jar/Zip\] \[Patch File\]  
- Generate Patch File From 2 tars to be used with patchjar \(requires dsdiff in path\):  
    >JarJarPatcher genpatch \[Input Tar\] \[Modified Tar\] \[Patch File\]  
- Verify MD5 Hash (Not Cryptographicly Secure):  
    >JarJarPatcher verifymd5 \[File\] \[HASH\]  