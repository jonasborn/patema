# Patema
Patema automated tape encoding management algorithms

---
**UNDER DEVELOPMENT**

This project is currently under heavy development and not working at all.
Just give me some time!

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

## Storage System
Before writing data to the tapes, all information is compressed and encrypted.
This is done using LZMA2 and AES in CTR mode using a 256 bit key. As the ftp server
needs to read and write randomly, the data is chunked in single files.

## Used software
### MinimalFTP
The ftp server is based on the wonderful MinimalFTP. For development reasons, the source code
of MinimalFTP is currently included in this project. All changes on the original source
are documented and I'm going to publish the changes to the main repo afterwards.
The source of MinimalFTP is licensed using the Apache license in version 2 - see MINIMALFTP-LICENSE.


