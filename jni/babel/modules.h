/* modules.h  Declaration of treaty modules for the babel program
 * (c) 2006 By L. Ross Raszewski
 *
 * This code is freely usable for all purposes.
 *
This program is released under the Perl Artistic License as specified below: 

 Preamble

The intent of this document is to state the conditions under which a
Package may be copied, such that the Copyright Holder maintains some
semblance of artistic control over the development of the package,
while giving the users of the package the right to use and distribute
the Package in a more-or-less customary fashion, plus the right to
make reasonable modifications.  Definitions

    "Package" refers to the collection of files distributed by the
    Copyright Holder, and derivatives of that collection of files
    created through textual modification.

    "Standard Version" refers to such a Package if it has not been
    modified, or has been modified in accordance with the wishes of
    the Copyright Holder as specified below.

    "Copyright Holder" is whoever is named in the copyright or
    copyrights for the package.

    "You" is you, if you're thinking about copying or distributing
    this Package.

    "Reasonable copying fee" is whatever you can justify on the basis
    of media cost, duplication charges, time of people involved, and
    so on. (You will not be required to justify it to the Copyright
    Holder, but only to the computing community at large as a market
    that must bear the fee.)

    "Freely Available" means that no fee is charged for the item
    itself, though there may be fees involved in handling the item. It
    also means that recipients of the item may redistribute it under
    the same conditions they received it.

   1. You may make and give away verbatim copies of the source form of
    the Standard Version of this Package without restriction,
    provided that you duplicate all of the original copyright
    notices and associated disclaimers.

   2. You may apply bug fixes, portability fixes and other
    modifications derived from the Public Domain or from the
    Copyright Holder. A Package modified in such a way shall still
    be considered the Standard Version.

   3. You may otherwise modify your copy of this Package in any way,
    provided that you insert a prominent notice in each changed file
    stating how and when you changed that file, and provided that
    you do at least ONE of the following:

         1. place your modifications in the Public Domain or otherwise
         make them Freely Available, such as by posting said
         modifications to Usenet or an equivalent medium, or placing
         the modifications on a major archive site such as
         uunet.uu.net, or by allowing the Copyright Holder to include
         your modifications in the Standard Version of the Package.
         2. use the modified Package only within your corporation or
         organization.  3. rename any non-standard executables so the
         names do not conflict with standard executables, which must
         also be provided, and provide a separate manual page for each
         non-standard executable that clearly documents how it differs
         from the Standard Version.
         4. make other distribution arrangements with the Copyright
         4. Holder.

   4. You may distribute the programs of this Package in object code
    or executable form, provided that you do at least ONE of the
    following:

         1. distribute a Standard Version of the executables and
         library files, together with instructions (in the manual page
         or equivalent) on where to get the Standard Version.
         2. accompany the distribution with the machine-readable
         source of the Package with your modifications.  3. give
         non-standard executables non-standard names, and clearly
         document the differences in manual pages (or equivalent),
         together with instructions on where to get the Standard
         Version.
         4. make other distribution arrangements with the Copyright
         4. Holder.

   5. You may charge a reasonable copying fee for any distribution of
    this Package. You may charge any fee you choose for support of
    this Package. You may not charge a fee for this Package
    itself. However, you may distribute this Package in aggregate
    with other (possibly commercial) programs as part of a larger
    (possibly commercial) software distribution provided that you do
    not advertise this Package as a product of your own. You may
    embed this Package's interpreter within an executable of yours
    (by linking); this shall be construed as a mere form of
    aggregation, provided that the complete Standard Version of the
    interpreter is so embedded.

   6. The scripts and library files supplied as input to or produced
    as output from the programs of this Package do not automatically
    fall under the copyright of this Package, but belong to whomever
    generated them, and may be sold commercially, and may be
    aggregated with this Package. If such scripts or library files
    are aggregated with this Package via the so-called "undump" or
    "unexec" methods of producing a binary executable image, then
    distribution of such an image shall neither be construed as a
    distribution of this Package nor shall it fall under the
    restrictions of Paragraphs 3 and 4, provided that you do not
    represent such an executable image as a Standard Version of this
    Package.

   7. C subroutines (or comparably compiled subroutines in other
    languages) supplied by you and linked into this Package in order
    to emulate subroutines and variables of the language defined by
    this Package shall not be considered part of this Package, but
    are the equivalent of input as in Paragraph 6, provided these
    subroutines do not change the language in any way that would
    cause it to fail the regression tests for the language.

   8. Aggregation of this Package with a commercial distribution is
    always permitted provided that the use of this Package is
    embedded; that is, when no overt attempt is made to make this
    Package's interfaces visible to the end user of the commercial
    distribution. Such use shall not be construed as a distribution
    of this Package.

   9. The name of the Copyright Holder may not be used to endorse or
    promote products derived from this software without specific
    prior written permission.

  10. THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR
   IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
   WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
   PURPOSE.

The End

 *
 * This file depends upon treaty.h and all the references treaty modules
 *
 * Persons wishing to add support for a new module to babel need only
 * add a line in the form below.  New modules should be positioned according
 * to their popularity.  If this file is being used in tandem with register.c
 * (as it is in babel), then being dishonest about the popularity of an added
 * system will make the program non-compliant with the treaty of Babel
 *
 * REGISTER_NAME is used as a placeholder for formats which are specified
 * as existing by the treaty but for which no handler yet exists.
 * remove the REGISTER_NAME for any format which has a registered treaty.
 */


#include "treaty.h"
#undef REGISTER_TREATY
#undef REGISTER_CONTAINER
#undef REGISTER_NAME
#ifdef TREATY_REGISTER
#ifdef CONTAINER_REGISTER
#ifdef FORMAT_REGISTER
#define REGISTER_TREATY(x)        #x,
#define REGISTER_NAME(x)          #x,
#define REGISTER_CONTAINER(x)
#else
#define REGISTER_TREATY(x)
#define REGISTER_CONTAINER(x)     x##_treaty,
#define REGISTER_NAME(x)
#endif
#else
#define REGISTER_TREATY(x)        x##_treaty,
#define REGISTER_CONTAINER(x)
#define REGISTER_NAME(x)
#endif
#else
#define REGISTER_TREATY(x)        int32 x##_treaty(int32, void *, int32, void *, int32);
#define REGISTER_CONTAINER(x)        int32 x##_treaty(int32, void *, int32, void *, int32);
#define REGISTER_NAME(x)
#endif


REGISTER_CONTAINER(blorb)
REGISTER_TREATY(zcode)
//REGISTER_TREATY(glulx)
REGISTER_TREATY(tads2)
//REGISTER_TREATY(tads3)
//REGISTER_TREATY(hugo)
//REGISTER_TREATY(alan)
//REGISTER_TREATY(adrift)
//REGISTER_TREATY(level9)
//REGISTER_TREATY(agt)
//REGISTER_TREATY(magscrolls)
//REGISTER_TREATY(advsys)
//REGISTER_TREATY(executable)



