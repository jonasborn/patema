# Patema
Patema automated tape encoding management algorithms

---
**UNDER DEVELOPMENT**

This project is currently under heavy development and not working at all.
Just give me some time!

At the moment, the program is able to write the encrypted chunks as a whole to the tapes and
will show the current content of a tape in the ftp tree

---

---
**SECURITY INFORMATION**

I'm not a security expert and therefore can not guarantee anything.

---

## Current status
### Encryption
Currently, the project is using **rockaport/alice** for encrypting the registers.
The register contains the iv and salt used for all files and provides the encryption/decryption passwords.
The file encryption is a customized AES ECB solution, using an IV based on the chunk position and the initial iv from
the register. The key for each chunk is generated from a password and the salt from the register. 

## About
Patema is a set of tools and algorithms to access LTO-tapes using Java.
The project also contains a easy to use FTP-server, able to compress and encrypt with random
access using a block based storage system (called see SplinteredFile).

### Structure

The project includes a CMake project in the native folder. This project is used to create
the libpatema-native (JNI), used to directly control the tape device.
The main project is build using Java, including sources from JTape and MinimalFTP.

Some parts of this project are written using Java, other ones using Groovy. This is made because
plain Java is more speed effective than Groovy. Groovy in the other hand is more useful.

### Files
As ftp needs random access to files - mostly in order to resume uploads and downloads, the project
provides a custom solution for that.
Files are split in chunks and helt in the memory while working, therefore all encryption, decryption and the
compression/decompression stuff works on the fly.
Both, the write and read functions drove me crazy!


#### Write

```
 1111111111111333333333333300000000000000000000000 -------------------- Current file content
 |___________| -------------------------------------------------------- Area to skip
              |___________| ------------------------------------------- Will be overwritten
 |________________________| ------------------------------------------- Data from file
                           |_____________________| -------------------- Empty
 |_______________________________________________| -------------------- Max file size
              4444444444444444444444444444444444442222222222222222222
              |__________________________________| -------------------- Data to write for current file
                                                 |__________________| - Data for next file
              |_____________________________________________________| - Data total
```

#### Read
As the read function not only needs to wirk with fixed chunk sizes (for the encrypted ones) but also with dynamic sizes
(for reading encrypted content) the read function is a bit more complicated but very well commented. Have a look
at **de.jonasborn.patema.ios.parted.file.PartedFile**


## Installation

### Clone the project
``` bash
git clone https://github.com/jonasborn/patema.git
```


### Build the project
This will install all required tools to build the project. This will include:
- JDK 11
- Groovy 3.x
- gcc 7.5
- gradle 2.4
- CMake 3.10

``` bash
bash build.sh
```

### Install the project

This will install the required runtime tools. This will include
- tapeinfo
- sg_logs
- lsscsi

Most of the commands require sudo to operate,
therefore the project contains some wrappers in the **script** folder.
The installer will create rules in /etc/sudousers to allow running
the scripts without password. If you are using a system without sudo available,
remove the _prefix in the config and modify the scripts as needed. 

>I've chosen this solution because of security and easiness.
> If you want sth. else, go on and create it, I would really appreciate it

``` bash
bash install.sh
```

## Storage System
Before writing data to the tapes, all information is compressed and encrypted.
This is done using LZMA2 and AES. As the ftp server
needs to read and write randomly, the data is chunked in single files.

## Crypto
Currently, there are two ways to encrypt file parts when creating projects:
CTR and ECB.
Both of them use a special iv creation based on the password. This may not seem secure,
using a random read and write system, I just could not find a better solution. All files
are using a IV, created from the initial IV and the current index of the file.
The en/decryption is currently under development.

## Used software
### MinimalFTP
The ftp server is based on the wonderful MinimalFTP. For development reasons, the source code
of MinimalFTP is currently included in this project. All changes on the original source
are documented and I'm going to publish the changes to the main repo afterwards.
The source of MinimalFTP is licensed using the Apache license in version 2 - see MINIMALFTP-LICENSE.

### vbindiff
Thanks for helping me to debug the whole filesystem!
See https://www.cjmweb.net/vbindiff/ for a great out-of-the-box binary diff tool!

