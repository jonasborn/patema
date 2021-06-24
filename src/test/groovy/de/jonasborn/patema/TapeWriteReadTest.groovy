/*
 * Copyright 2021 Jonas Born
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.jonasborn.patema

import com.google.common.hash.Hashing
import com.google.common.io.Files
import de.jonasborn.patema.ios.endecode.Decoder
import de.jonasborn.patema.ios.endecode.Encoder
import de.jonasborn.patema.ios.endecode.implementation.AESECBOnLZMA2
import de.jonasborn.patema.register.Register
import de.jonasborn.patema.register.V1Register
import de.jonasborn.patema.register.V1RegisterEntry
import de.jonasborn.patema.tape.Tape
import de.jonasborn.patema.tape.TapeInputStream
import de.jonasborn.patema.tape.TapeReader
import de.jonasborn.patema.util.ByteUtils
import de.jonasborn.patema.util.LogUtils
import de.jonasborn.patema.util.PaddingUtils
import de.jonasborn.patema.util.RandomUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

import java.util.concurrent.ThreadLocalRandom

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class TapeWriteReadTest {

    static File tempDir = new File("test/tapetest")

    static Logger logger = LogManager.getLogger(TapeWriteReadTest.class)
    static Tape tape
    static String password


    static {
        tempDir.mkdirs()
    }


    @BeforeClass
    public static void initializeDevice() {
        LogUtils.setRootLevel("ALL")
        def prop = new Properties()
        prop.load(new FileInputStream("test.properties"))
        tape = new Tape("test", prop.get("device") as String)
        password = UUID.randomUUID().toString().substring(0, ThreadLocalRandom.current().nextInt(4, 37))
        tape.initialize(true)
    }

    @AfterClass
    public static void closeDevice() {
        tape.close()
    }

    @Test
    public void aWriteRegister() {

        V1Register register = new V1Register()
        register.addEntry(new V1RegisterEntry(
                "test", 0, new byte[16], 1234, [], 1234, "password"
        ))
        tape.writeRegister(register, password)

    }

    @Test
    public void bReadRegister() {
        def register = tape.readRegister(password)
        assert register.getVersion() == 1
        assert register.getEntry(0).name == "test"
    }


    private List<File> createRawFiles(File targetDir) {
        List<File> result = []
        for (i in 0..<RandomUtils.nextInt(5, 20)) {
            def target = new File(targetDir, i + ".raw")
            RandomUtils.fillFile(target, RandomUtils.nextInt(0, 1024 * 8))
            result.add(target)
        }
        return result
    }

    private List<File> createEncryptedFiles(Encoder encoder, Integer index, List<File> sourceFiles, File targetDir) {


        def pos = 0
        List<File> result = []
        sourceFiles.each {
            def encryptedFile = new File(targetDir, pos + ".encrypted")
            logger.info("Encrypting file {}, stored as {}", it, encryptedFile)
            byte[] enc = encoder.encode(index, it.getBytes())

            encryptedFile.setBytes(
                    enc
            )
            result.add(encryptedFile)
            pos++
        }

        return result;

    }

    static File rawDir
    static List<File> rawFiles

    static File encryptedDir
    static List<File> encryptedFiles

    static File loadedDir
    static File decryptedDir
    static List<File> decryptedFiles = []

    static V1Register register

    static AESECBOnLZMA2 aesecb

    @Test
    void cWrite() {

        register = new V1Register("test", password)
          aesecb = new AESECBOnLZMA2(password, register.getIv("test"), register.getSalt("test"))

        rawDir = new File(tempDir, RandomUtils.nextUUID() + "-raw")
        rawDir.mkdirs()
        logger.info("Creating raw files in {}", rawDir)
        rawFiles = createRawFiles(rawDir)


        encryptedDir = new File(tempDir, RandomUtils.nextUUID() + "-enc")
        encryptedDir.mkdirs()
        logger.info("Encrypting files to {}", encryptedDir)
        encryptedFiles = createEncryptedFiles(aesecb, 0, rawFiles, encryptedDir)

        logger.info("Collecting sizes")
        List<Long> encryptedSizes = encryptedFiles.collect { it.size() }
        Long encryptedSize = encryptedSizes.sum() as Long
        logger.info("Detected {} chunks with a total length of {}", encryptedSizes.size(), encryptedSize)

        register.addEntry(
                new V1RegisterEntry(
                        "test", 0, new byte[16], encryptedSize, encryptedSizes, 0, password
                )
        )

        logger.info("Writing register")
        tape.writeRegister(register, password)

        logger.info("Writing files")
        int pos = 0
        encryptedFiles.each {
            logger.debug("Writing chunk {} of file {}", pos, 0)
            tape.writeChunk(0, pos, new FileInputStream(it))
            pos++
        }
        logger.info("Successfully written {} files to tape", pos)

    }

    @Test
    void dRead() {


        def loadedDir = new File(tempDir, RandomUtils.nextUUID() + "-loa")
        loadedDir.mkdirs()
        List<File> loadedFiles = []
        logger.info("Attempting to load encrypted chunks from tape to {}", loadedDir)
        TapeReader reader = new TapeReader(tape, register, 0)

        for (int i = 0; i < encryptedFiles.size(); i++) {
            def file = new File(loadedDir, i + ".loaded")
            file.setBytes(reader.readChunk(i))
            loadedFiles.add(file)
        }

        logger.info("Comparing read chunks with encrypted ones")
        for (int i = 0; i < encryptedFiles.size(); i++) {
            logger.debug("Comparing chunk {}, file {} and {}", i, encryptedFiles[i], loadedFiles[i])

            println "vbindiff " + loadedFiles[i] + " " + encryptedFiles[i]
            def a = Files.hash(encryptedFiles[i], Hashing.md5())
            def b = Files.hash(loadedFiles[i], Hashing.md5())
            assert encryptedFiles[i].length() == loadedFiles[i].length()
            assert a == b

        }

        logger.info("Encrypted and read are equal")

        decryptedDir = new File(tempDir, RandomUtils.nextUUID() + "-dec")
        decryptedDir.mkdirs()

        logger.info("Decrypting files")
        int index = 0;
        encryptedFiles.each {
            def file = new File(decryptedDir, index + ".decrypted")
            logger.debug("Decrypting file {} to {}", it, file)
            file.setBytes(aesecb.decode(0, it.getBytes()))
            decryptedFiles.add(file)
            index++
        }

        logger.info("Comparing raw with decrypted ones")
        for (i in 0..<loadedFiles.size()) {
            println "vbindiff " + rawFiles[i] + " " + decryptedFiles[i]
            def a = Files.hash(decryptedFiles[i], Hashing.md5())
            def b = Files.hash(rawFiles[i], Hashing.md5())
            assert decryptedFiles[i].length() == rawFiles[i].length()
            assert a == b

        }
    }

}
