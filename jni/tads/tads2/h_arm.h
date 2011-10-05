/* $Header: d:/cvsroot/tads/TADS2/H_ARM.H,v 1.2 1999/05/17 02:52:12 MJRoberts Exp $ */

/* 
 *   Copyright (c) 1998, 2002 Michael J. Roberts.  All Rights Reserved.
 *   
 *   Please see the accompanying license file, LICENSE.TXT, for information
 *   on using and copying this software.  
 */
/*
Name
  h_arm.h - hardware definitions for ARM.
Function
  These definitions are for 32-bit ARM CPUs.  
Notes

Modified
  10/05/11 retrobits  - Creation
*/

#ifndef H_ARM_H
#define H_ARM_H

/* 
 *   Round a size up to worst-case alignment boundary.  
 */
#define osrndsz(s) (((s) + 3) & ~3)

/* 
 *   Round a pointer up to worst-case alignment boundary.  
 */
#define osrndpt(p) ((unsigned char*)((((unsigned long)(p)) + 3) & ~3))

/* 
 *   Service macros for osrp2 etc.  
 */
#define osc2u(p,i) ((unsigned short)(((unsigned char*)(p))[i]))
#define osc2l(p,i) ((unsigned long)(((unsigned char*)(p))[i]))
#define osc2s(p,i) ((short)(((signed char*)(p))[i]))
#define osc2sl(p,i) ((long)(((signed char*)(p))[i]))

/* 
 *   Read an unaligned portable unsigned 2-byte value, returning an int
 *   value.  
 */
#define osrp2(p) (osc2u(p,0) + (osc2u(p,1) << 8))

/* 
 *   Read an unaligned portable signed 2-byte value, returning int.  
 */
#define osrp2s(p) (((short)osc2u(p,0)) + (osc2s(p,1) << 8))

/* 
 *   Write int to unaligned portable 2-byte value.  
 */
#define oswp2(p,i) \
    ((((unsigned char*)(p))[1] = (i) >> 8), \
     (((unsigned char*)(p))[0] = (i) & 255))

/* 
 *   Read an unaligned portable 4-byte value, returning long.  
 */
#define osrp4(p) \
    (((long)osc2l(p,0)) \
     + ((long)(osc2l(p,1)) << 8) \
     + ((long)(osc2l(p,2) << 16)) \
     + (osc2sl(p,3) << 24))

/* 
 *   Write a long to an unaligned portable 4-byte value.  
 */
#define oswp4(p,i) \
    ((((unsigned char*)(p))[0] = (i)), \
     (((unsigned char*)(p))[1] = (i) >> 8), \
     (((unsigned char*)(p))[2] = (i) >> 16, \
     (((unsigned char*)(p))[3] = (i) >> 24)))

#endif /* H_ARM_H */
