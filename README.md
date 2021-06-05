# Patema
Patema automated tape encoding management algorithms

---
**UNDER DEVELOPMENT**

This project is currently under heavy development and not working at all.
Just give me some time!

---

---
**SECURITY INFORMATION**

Do not use a already used password for a second tape.
You never should use passwords multiple times - but here
it may corrupt the whole encryption system. Scroll down to "Crypto" for more infos. 

---

## Current status
### Encryption
Currently implementing an alternative encryption solution using Apache Commons Crypto.
The lib will use OpenSsl, therefore using AES-NI on Intel-Chips - could be a bit faster.
Currently 250 KB/s is a bit slow
Update:
Ha, looks like it worked. There are now two supported crypto systems supported:
PartedCTRCrypto, using a cipher pool to speed up or PartedRCBCrypto using a single
Cipher with a custom iv xor (my favorite).
Both of them are running at around 10 MB/s.
Just had to learn a "few" infos about AES and all it's specials ^^


## About
Patema is a set of tools and algorithms to access LTO-tapes using Java.
The project also contains a easy to use FTP-server, able to compress and encrypt with random
access using a block based storage system (called see SplinteredFile).

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

``` bash
bash build.sh
```

- Install the project using

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
Currently there are two ways to encrypt file parts when creating projects:
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


