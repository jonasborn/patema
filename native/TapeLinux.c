/* TapeLinux.c */

/**
 * @author dcgibbons
 * Modified header location
 * Added additional operations
 * @author Jonas Born
 */

#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/fcntl.h>
#include <sys/mtio.h>

#include <jni.h>
#include <jvmti.h>
#include <hwloc.h>

#include "lsscsi.c"

#include "../build/include/jtape_BasicTapeDevice.h"

#define TRUE 1
#define FALSE 0



/* field IDs for commonly used object fields */
static jfieldID td_fdID;
static jfieldID td_eofID;
static jfieldID td_eomID;
static jfieldID IO_fd_fdID;


/* forward reference for utility functions */
static int getFD(JNIEnv* env, jobject obj);
static void setFD(JNIEnv* env, jobject obj, int fd);
static void throw(JNIEnv* env, int err);


/*
 * Class:     BasicTapeDevice
 * Method:    initFields
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_jtape_BasicTapeDevice_initFields
  (JNIEnv *env, jclass cls) 
{
    /* retrieve field IDs for the fd, eof, and eom member variables */
    td_fdID = (*env)->GetFieldID(env, cls, "fd", "Ljava/io/FileDescriptor;");
    td_eofID = (*env)->GetFieldID(env, cls, "eof", "Z");
    td_eomID = (*env)->GetFieldID(env, cls, "eom", "Z");

    /* retrieve the field ID for the private fd member of FileDescriptor */
    cls = (*env)->FindClass(env, "java/io/FileDescriptor");
    IO_fd_fdID = (*env)->GetFieldID(env, cls, "fd", "I");
}


/*
 * Class:     BasicTapeDevice
 * Method:    tapeOpen
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_jtape_BasicTapeDevice_tapeOpen
  (JNIEnv *env, jobject this, jstring path)
{
    int fd;
    const char* p;

    p = (*env)->GetStringUTFChars(env, path, 0);
    fd = open(p, O_RDWR | O_NONBLOCK);
    (*env)->ReleaseStringUTFChars(env, path, p);
    printf("You entered: %d", fd);
    if (fd == -1) {
        throw(env, errno);
    }

    setFD(env, this, fd);
}


/*
 * Class:     BasicTapeDevice
 * Method:    tapeClose
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_jtape_BasicTapeDevice_tapeClose
  (JNIEnv *env, jobject this)
{
    int fd = getFD(env, this);
    if (close(fd) == -1) {
        throw(env, errno);
    }

    fd = -1;
    setFD(env, this, fd);
}


/*
 * Class:     BasicTapeDevice
 * Method:    tapeRead
 * Signature: ([BII)I
 */
JNIEXPORT jint JNICALL Java_jtape_BasicTapeDevice_tapeRead
  (JNIEnv *env, jobject this, jbyteArray buf, jint off, jint len)
{
    int n, fd;
    jbyte* bufp;

    fd = getFD(env, this);
    bufp = (*env)->GetByteArrayElements(env, buf, 0);
    n = read(fd, &bufp[off], len);
    (*env)->ReleaseByteArrayElements(env, buf, bufp, 0);

    if (n < 0) {
        throw(env, errno);
    } else if (n == 0) {
        (*env)->SetBooleanField(env, this, td_eofID, TRUE);
    }

    return n;
}


/*
 * Class:     BasicTapeDevice
 * Method:    tapeWrite
 * Signature: ([BII)I
 */
JNIEXPORT jint JNICALL Java_jtape_BasicTapeDevice_tapeWrite
  (JNIEnv *env, jobject this, jbyteArray buf, jint off, jint len)
{
    int n, fd;
    jbyte* bufp;

    fd = getFD(env, this);
    bufp = (*env)->GetByteArrayElements(env, buf, 0);
    n = write(fd, &bufp[off], len);
    (*env)->ReleaseByteArrayElements(env, buf, bufp, 0);

    if (n < 0) {
        throw(env, errno);
    } else if (n == 0) {
        (*env)->SetBooleanField(env, this, td_eomID, TRUE);
    }



    return n;
}


/*
 * Class:     BasicTapeDevice
 * Method:    tapeGetBlockSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_jtape_BasicTapeDevice_tapeGetBlockSize
  (JNIEnv *env, jobject this)
{
    int fd;
    struct mtget mtget;
    jint bs;

    fd = getFD(env, this);
    if (ioctl(fd, MTIOCGET, &mtget) == -1) {
        throw(env, errno);
        bs = -1;
    } else {
        bs = mtget.mt_dsreg & MT_ST_BLKSIZE_MASK;
    }

    return bs;
}

/*
 * Class:     BasicTapeDevice
 * Method:    tapeGetStatus
 * Signature: ()L
 */
JNIEXPORT jlong JNICALL Java_jtape_BasicTapeDevice_tapeGetStatus
        (JNIEnv *env, jobject this)
{
    int fd;
    struct mtget mtget;
    jint bs;

    fd = getFD(env, this);
    if (ioctl(fd, MTIOCGET, &mtget) == -1) {
        throw(env, errno);
        bs = -1;
    } else {
        bs = mtget.mt_gstat;
    }

    return bs;
}

/*
 * Class:     BasicTapeDevice
 * Method:    tapeSetBlockSize
 * Signature: ()I
 */
JNIEXPORT void JNICALL Java_jtape_BasicTapeDevice_tapeSetBlockSize
  (JNIEnv *env, jobject this, jint bs)
{
    int fd;
    struct mtop mtop;

    mtop.mt_op = MTSETBLK;
    mtop.mt_count = bs;
    fd = getFD(env, this);
    if (ioctl(fd, MTIOCTOP, &mtop) == -1) {
        throw(env, errno);
    }
}


/*
 * Class:     BasicTapeDevice
 * Method:    tapeRewind
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_jtape_BasicTapeDevice_tapeRewind
  (JNIEnv *env, jobject this)
{
    int fd;
    struct mtop mtop;

    mtop.mt_op = MTREW;
    mtop.mt_count = 1;
    fd = getFD(env, this);
    if (ioctl(fd, MTIOCTOP, &mtop) == -1) {
        throw(env, errno);
    }
}


/*
 * Class:     BasicTapeDevice
 * Method:    tapeMTEOM
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_jtape_BasicTapeDevice_tapeMTEOM
  (JNIEnv* env, jobject this)
{
    int fd;
    struct mtop mtop;

    mtop.mt_op = MTEOM;
    mtop.mt_count = 1;
    fd = getFD(env, this);
    if (ioctl(fd, MTIOCTOP, &mtop) == -1) {
        throw(env, errno);
    }
}

/*
 * Class:     BasicTapeDevice
 * Method:    tapeMTFSF
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_jtape_BasicTapeDevice_tapeMTFSF
  (JNIEnv* env, jobject this)
{
    int fd;
    struct mtop mtop;

    mtop.mt_op = MTFSF;
    mtop.mt_count = 1;
    fd = getFD(env, this);
    if (ioctl(fd, MTIOCTOP, &mtop) == -1) {
        throw(env, errno);
    }
}

/*
 * Class:     BasicTapeDevice
 * Method:    tapeMTWEOF
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_jtape_BasicTapeDevice_tapeMTWEOF
  (JNIEnv* env, jobject this)
{
    int fd;
    struct mtop mtop;

    mtop.mt_op = MTWEOF;
    mtop.mt_count = 1;
    fd = getFD(env, this);
    if (ioctl(fd, MTIOCTOP, &mtop) == -1) {
        throw(env, errno);
    }
}

/*
 * Class:     BasicTapeDevice
 * Method:    tapeMTFSFM
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_jtape_BasicTapeDevice_tapeMTFSFM
  (JNIEnv* env, jobject this)
{
    int fd;
    struct mtop mtop;

    mtop.mt_op = MTFSFM;
    mtop.mt_count = 1;
    fd = getFD(env, this);
    if (ioctl(fd, MTIOCTOP, &mtop) == -1) {
        throw(env, errno);
    }
}

/*
 * Class:     BasicTapeDevice
 * Method:    tapeMTBSF
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_jtape_BasicTapeDevice_tapeMTBSF
  (JNIEnv* env, jobject this)
{
    int fd;
    struct mtop mtop;

    mtop.mt_op = MTBSF;
    mtop.mt_count = 1;
    fd = getFD(env, this);
    if (ioctl(fd, MTIOCTOP, &mtop) == -1) {
        throw(env, errno);
    }
}

/*
 * Class:     BasicTapeDevice
 * Method:    tapeMTBSFM
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_jtape_BasicTapeDevice_tapeMTBSFM
  (JNIEnv* env, jobject this)
{
    int fd;
    struct mtop mtop;

    mtop.mt_op = MTBSFM;
    mtop.mt_count = 1;
    fd = getFD(env, this);
    if (ioctl(fd, MTIOCTOP, &mtop) == -1) {
        throw(env, errno);
    }
}

/*
 * Class:     BasicTapeDevice
 * Method:    tapeMTUNLOAD
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_jtape_BasicTapeDevice_tapetapeMTUNLOAD
  (JNIEnv* env, jobject this)
{
    int fd;
    struct mtop mtop;

    mtop.mt_op = MTUNLOAD;
    mtop.mt_count = 1;
    fd = getFD(env, this);
    if (ioctl(fd, MTIOCTOP, &mtop) == -1) {
        throw(env, errno);
    }
}


/*
 * Retrieves the internal file descriptor from the BasicTapeDevice object
 */
static int getFD(JNIEnv* env, jobject obj) {
    jobject fdobj;

    fdobj = (*env)->GetObjectField(env, obj, td_fdID);
    return (*env)->GetIntField(env, fdobj, IO_fd_fdID);
}


/*
 * Sets the internal file descriptor of the BasicTapeDevice object
 */
static void setFD(JNIEnv* env, jobject obj, int fd)
{
    jobject fdobj = (*env)->GetObjectField(env, obj, td_fdID);
    (*env)->SetIntField(env, fdobj, IO_fd_fdID, fd);
}


/*
 * Throws a new IOException
 */
static void throw(JNIEnv* env, int err)
{
    jclass cls = (*env)->FindClass(env, "java/io/IOException");
    if (cls != NULL) {
        (*env)->ThrowNew(env, cls, strerror(err));
    }
}
