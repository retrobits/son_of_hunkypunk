
/* A Bison parser, made by GNU Bison 2.4.1.  */

/* Skeleton implementation for Bison's Yacc-like parsers in C
   
      Copyright (C) 1984, 1989, 1990, 2000, 2001, 2002, 2003, 2004, 2005, 2006
   Free Software Foundation, Inc.
   
   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

/* As a special exception, you may create a larger work that contains
   part or all of the Bison parser skeleton and distribute that work
   under terms of your choice, so long as that work isn't itself a
   parser generator using the skeleton or a modified version thereof
   as a parser skeleton.  Alternatively, if you modify or redistribute
   the parser skeleton itself, you may (at your option) remove this
   special exception, which will cause the skeleton and the resulting
   Bison output files to be licensed under the GNU General Public
   License without this special exception.
   
   This special exception was added by the Free Software Foundation in
   version 2.2 of Bison.  */

/* C LALR(1) parser skeleton written by Richard Stallman, by
   simplifying the original so-called "semantic" parser.  */

/* All symbols defined below should begin with yy or YY, to avoid
   infringing on user name space.  This should be done even for local
   variables, as they might otherwise be expanded by user macros.
   There are some unavoidable exceptions within include files to
   define necessary library symbols; they are noted "INFRINGES ON
   USER NAME SPACE" below.  */

/* Identify Bison output.  */
#define YYBISON 1

/* Bison version.  */
#define YYBISON_VERSION "2.4.1"

/* Skeleton name.  */
#define YYSKELETON_NAME "yacc.c"

/* Pure parsers.  */
#define YYPURE 0

/* Push parsers.  */
#define YYPUSH 0

/* Pull parsers.  */
#define YYPULL 1

/* Using locations.  */
#define YYLSP_NEEDED 0



/* Copy the first part of user declarations.  */

/* Line 189 of yacc.c  */
#line 1 "inform.y"

/*  Nitfol - z-machine interpreter using Glk for output.
    Copyright (C) 1999  Evin Robertson

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111, USA.

    The author can be reached at ecr+@andrew.cmu.edu
*/

#include "nitfol.h"
#include <ctype.h>

/* bison uses str* functions; make it use n_str* instead... */
#ifndef n_strcat
#define strcat(d, s) n_strcat(d, s)
#endif
#ifndef n_strlen
#define strlen(s) n_strlen(s)
#endif
#ifndef n_strcpy
#define strcpy(d, s) n_strcpy(d, s)
#endif
  
  
#ifdef DEBUGGING
  
  typedef struct zword_list zword_list;
  struct zword_list {
    zword_list *next;
    zword item;
  };

  typedef struct cond_list cond_list;
  struct cond_list {
    cond_list *next;
    zword val;
    BOOL (*condfunc)(zword a, zword b);
    BOOL opposite;
  };

  cond_list *condlist;
  
  static z_typed z_t(z_typed a, z_typed b, zword v);
  
  static const char *lex_expression;
  static int lex_offset;

  static const char *lex_tail(void) {
    const char *t = lex_expression + lex_offset;
    while(*t == ' ')
      t++;
    lex_offset = n_strlen(lex_expression);
    return t;
  }
  
  static z_typed inform_result;
  
  static int yylex(void);
  static void yyerror(const char *s);
  static void inform_help(void);
  
  int ignoreeffects;

#define YYERROR_VERBOSE
  
/*
#define YYDEBUG 1
*/



/* Line 189 of yacc.c  */
#line 157 "y.tab.c"

/* Enabling traces.  */
#ifndef YYDEBUG
# define YYDEBUG 0
#endif

/* Enabling verbose error messages.  */
#ifdef YYERROR_VERBOSE
# undef YYERROR_VERBOSE
# define YYERROR_VERBOSE 1
#else
# define YYERROR_VERBOSE 0
#endif

/* Enabling the token table.  */
#ifndef YYTOKEN_TABLE
# define YYTOKEN_TABLE 0
#endif


/* Tokens.  */
#ifndef YYTOKENTYPE
# define YYTOKENTYPE
   /* Put the tokens into the symbol table, so that GDB and other debuggers
      know about them.  */
   enum yytokentype {
     NUM = 258,
     DFILE = 259,
     CONDITION = 260,
     ALIAS = 261,
     RALIAS = 262,
     UNALIAS = 263,
     DUMPMEM = 264,
     AUTOMAP = 265,
     HELP = 266,
     UNDO = 267,
     REDO = 268,
     LANGUAGE = 269,
     INFOSOURCE = 270,
     INFOSOURCES = 271,
     COPYING = 272,
     WARRANTY = 273,
     PRINT = 274,
     SET = 275,
     MOVE = 276,
     TO = 277,
     GIVE = 278,
     REMOVE = 279,
     JUMP = 280,
     CONT = 281,
     STEP = 282,
     NEXT = 283,
     UNTIL = 284,
     STEPI = 285,
     NEXTI = 286,
     FINISH = 287,
     BREAK = 288,
     DELETE = 289,
     IF = 290,
     COND = 291,
     IGNORE = 292,
     BREAKPOINTS = 293,
     RESTORE = 294,
     RESTART = 295,
     QUIT = 296,
     RECORDON = 297,
     RECORDOFF = 298,
     REPLAY = 299,
     REPLAYOFF = 300,
     SYMBOL_FILE = 301,
     FRAME = 302,
     SELECT_FRAME = 303,
     BACKTRACE = 304,
     UP_FRAME = 305,
     DOWN_FRAME = 306,
     UP_SILENTLY = 307,
     DOWN_SILENTLY = 308,
     DISPLAY = 309,
     UNDISPLAY = 310,
     DISABLE_DISPLAY = 311,
     ENABLE_DISPLAY = 312,
     DISABLE_BREAK = 313,
     ENABLE_BREAK = 314,
     OBJECT_TREE = 315,
     FIND = 316,
     LIST_GLOBALS = 317,
     BTRUE = 318,
     BFALSE = 319,
     NOTHING = 320,
     PARENT = 321,
     CHILD = 322,
     SIBLING = 323,
     CHILDREN = 324,
     RANDOM = 325,
     NOTNOT = 326,
     OROR = 327,
     ANDAND = 328,
     OR = 329,
     WORDARRAY = 330,
     BYTEARRAY = 331,
     precNEG = 332,
     LOCAL = 333,
     GLOBAL = 334,
     STRING = 335,
     ROUTINE = 336,
     OBJECT = 337,
     NUMBER = 338,
     DECREMENT = 339,
     INCREMENT = 340,
     PROPLENGTH = 341,
     PROPADDR = 342,
     SUPERCLASS = 343
   };
#endif
/* Tokens.  */
#define NUM 258
#define DFILE 259
#define CONDITION 260
#define ALIAS 261
#define RALIAS 262
#define UNALIAS 263
#define DUMPMEM 264
#define AUTOMAP 265
#define HELP 266
#define UNDO 267
#define REDO 268
#define LANGUAGE 269
#define INFOSOURCE 270
#define INFOSOURCES 271
#define COPYING 272
#define WARRANTY 273
#define PRINT 274
#define SET 275
#define MOVE 276
#define TO 277
#define GIVE 278
#define REMOVE 279
#define JUMP 280
#define CONT 281
#define STEP 282
#define NEXT 283
#define UNTIL 284
#define STEPI 285
#define NEXTI 286
#define FINISH 287
#define BREAK 288
#define DELETE 289
#define IF 290
#define COND 291
#define IGNORE 292
#define BREAKPOINTS 293
#define RESTORE 294
#define RESTART 295
#define QUIT 296
#define RECORDON 297
#define RECORDOFF 298
#define REPLAY 299
#define REPLAYOFF 300
#define SYMBOL_FILE 301
#define FRAME 302
#define SELECT_FRAME 303
#define BACKTRACE 304
#define UP_FRAME 305
#define DOWN_FRAME 306
#define UP_SILENTLY 307
#define DOWN_SILENTLY 308
#define DISPLAY 309
#define UNDISPLAY 310
#define DISABLE_DISPLAY 311
#define ENABLE_DISPLAY 312
#define DISABLE_BREAK 313
#define ENABLE_BREAK 314
#define OBJECT_TREE 315
#define FIND 316
#define LIST_GLOBALS 317
#define BTRUE 318
#define BFALSE 319
#define NOTHING 320
#define PARENT 321
#define CHILD 322
#define SIBLING 323
#define CHILDREN 324
#define RANDOM 325
#define NOTNOT 326
#define OROR 327
#define ANDAND 328
#define OR 329
#define WORDARRAY 330
#define BYTEARRAY 331
#define precNEG 332
#define LOCAL 333
#define GLOBAL 334
#define STRING 335
#define ROUTINE 336
#define OBJECT 337
#define NUMBER 338
#define DECREMENT 339
#define INCREMENT 340
#define PROPLENGTH 341
#define PROPADDR 342
#define SUPERCLASS 343




#if ! defined YYSTYPE && ! defined YYSTYPE_IS_DECLARED
typedef union YYSTYPE
{

/* Line 214 of yacc.c  */
#line 84 "inform.y"

  glui32 pcoffset;
  infix_file *filenum;
  z_typed val;
  
  zword_list *zlist;

  struct {
    BOOL (*condfunc)(zword a, zword b);
    BOOL opposite;
  } cond;

  BOOL flag;



/* Line 214 of yacc.c  */
#line 386 "y.tab.c"
} YYSTYPE;
# define YYSTYPE_IS_TRIVIAL 1
# define yystype YYSTYPE /* obsolescent; will be withdrawn */
# define YYSTYPE_IS_DECLARED 1
#endif


/* Copy the second part of user declarations.  */


/* Line 264 of yacc.c  */
#line 398 "y.tab.c"

#ifdef short
# undef short
#endif

#ifdef YYTYPE_UINT8
typedef YYTYPE_UINT8 yytype_uint8;
#else
typedef unsigned char yytype_uint8;
#endif

#ifdef YYTYPE_INT8
typedef YYTYPE_INT8 yytype_int8;
#elif (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
typedef signed char yytype_int8;
#else
typedef short int yytype_int8;
#endif

#ifdef YYTYPE_UINT16
typedef YYTYPE_UINT16 yytype_uint16;
#else
typedef unsigned short int yytype_uint16;
#endif

#ifdef YYTYPE_INT16
typedef YYTYPE_INT16 yytype_int16;
#else
typedef short int yytype_int16;
#endif

#ifndef YYSIZE_T
# ifdef __SIZE_TYPE__
#  define YYSIZE_T __SIZE_TYPE__
# elif defined size_t
#  define YYSIZE_T size_t
# elif ! defined YYSIZE_T && (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
#  include <stddef.h> /* INFRINGES ON USER NAME SPACE */
#  define YYSIZE_T size_t
# else
#  define YYSIZE_T unsigned int
# endif
#endif

#define YYSIZE_MAXIMUM ((YYSIZE_T) -1)

#ifndef YY_
# if YYENABLE_NLS
#  if ENABLE_NLS
#   include <libintl.h> /* INFRINGES ON USER NAME SPACE */
#   define YY_(msgid) dgettext ("bison-runtime", msgid)
#  endif
# endif
# ifndef YY_
#  define YY_(msgid) msgid
# endif
#endif

/* Suppress unused-variable warnings by "using" E.  */
#if ! defined lint || defined __GNUC__
# define YYUSE(e) ((void) (e))
#else
# define YYUSE(e) /* empty */
#endif

/* Identity function, used to suppress warnings about constant conditions.  */
#ifndef lint
# define YYID(n) (n)
#else
#if (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
static int
YYID (int yyi)
#else
static int
YYID (yyi)
    int yyi;
#endif
{
  return yyi;
}
#endif

#if ! defined yyoverflow || YYERROR_VERBOSE

/* The parser invokes alloca or malloc; define the necessary symbols.  */

# ifdef YYSTACK_USE_ALLOCA
#  if YYSTACK_USE_ALLOCA
#   ifdef __GNUC__
#    define YYSTACK_ALLOC __builtin_alloca
#   elif defined __BUILTIN_VA_ARG_INCR
#    include <alloca.h> /* INFRINGES ON USER NAME SPACE */
#   elif defined _AIX
#    define YYSTACK_ALLOC __alloca
#   elif defined _MSC_VER
#    include <malloc.h> /* INFRINGES ON USER NAME SPACE */
#    define alloca _alloca
#   else
#    define YYSTACK_ALLOC alloca
#    if ! defined _ALLOCA_H && ! defined _STDLIB_H && (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
#     include <stdlib.h> /* INFRINGES ON USER NAME SPACE */
#     ifndef _STDLIB_H
#      define _STDLIB_H 1
#     endif
#    endif
#   endif
#  endif
# endif

# ifdef YYSTACK_ALLOC
   /* Pacify GCC's `empty if-body' warning.  */
#  define YYSTACK_FREE(Ptr) do { /* empty */; } while (YYID (0))
#  ifndef YYSTACK_ALLOC_MAXIMUM
    /* The OS might guarantee only one guard page at the bottom of the stack,
       and a page size can be as small as 4096 bytes.  So we cannot safely
       invoke alloca (N) if N exceeds 4096.  Use a slightly smaller number
       to allow for a few compiler-allocated temporary stack slots.  */
#   define YYSTACK_ALLOC_MAXIMUM 4032 /* reasonable circa 2006 */
#  endif
# else
#  define YYSTACK_ALLOC YYMALLOC
#  define YYSTACK_FREE YYFREE
#  ifndef YYSTACK_ALLOC_MAXIMUM
#   define YYSTACK_ALLOC_MAXIMUM YYSIZE_MAXIMUM
#  endif
#  if (defined __cplusplus && ! defined _STDLIB_H \
       && ! ((defined YYMALLOC || defined malloc) \
	     && (defined YYFREE || defined free)))
#   include <stdlib.h> /* INFRINGES ON USER NAME SPACE */
#   ifndef _STDLIB_H
#    define _STDLIB_H 1
#   endif
#  endif
#  ifndef YYMALLOC
#   define YYMALLOC malloc
#   if ! defined malloc && ! defined _STDLIB_H && (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
void *malloc (YYSIZE_T); /* INFRINGES ON USER NAME SPACE */
#   endif
#  endif
#  ifndef YYFREE
#   define YYFREE free
#   if ! defined free && ! defined _STDLIB_H && (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
void free (void *); /* INFRINGES ON USER NAME SPACE */
#   endif
#  endif
# endif
#endif /* ! defined yyoverflow || YYERROR_VERBOSE */


#if (! defined yyoverflow \
     && (! defined __cplusplus \
	 || (defined YYSTYPE_IS_TRIVIAL && YYSTYPE_IS_TRIVIAL)))

/* A type that is properly aligned for any stack member.  */
union yyalloc
{
  yytype_int16 yyss_alloc;
  YYSTYPE yyvs_alloc;
};

/* The size of the maximum gap between one aligned stack and the next.  */
# define YYSTACK_GAP_MAXIMUM (sizeof (union yyalloc) - 1)

/* The size of an array large to enough to hold all stacks, each with
   N elements.  */
# define YYSTACK_BYTES(N) \
     ((N) * (sizeof (yytype_int16) + sizeof (YYSTYPE)) \
      + YYSTACK_GAP_MAXIMUM)

/* Copy COUNT objects from FROM to TO.  The source and destination do
   not overlap.  */
# ifndef YYCOPY
#  if defined __GNUC__ && 1 < __GNUC__
#   define YYCOPY(To, From, Count) \
      __builtin_memcpy (To, From, (Count) * sizeof (*(From)))
#  else
#   define YYCOPY(To, From, Count)		\
      do					\
	{					\
	  YYSIZE_T yyi;				\
	  for (yyi = 0; yyi < (Count); yyi++)	\
	    (To)[yyi] = (From)[yyi];		\
	}					\
      while (YYID (0))
#  endif
# endif

/* Relocate STACK from its old location to the new one.  The
   local variables YYSIZE and YYSTACKSIZE give the old and new number of
   elements in the stack, and YYPTR gives the new location of the
   stack.  Advance YYPTR to a properly aligned location for the next
   stack.  */
# define YYSTACK_RELOCATE(Stack_alloc, Stack)				\
    do									\
      {									\
	YYSIZE_T yynewbytes;						\
	YYCOPY (&yyptr->Stack_alloc, Stack, yysize);			\
	Stack = &yyptr->Stack_alloc;					\
	yynewbytes = yystacksize * sizeof (*Stack) + YYSTACK_GAP_MAXIMUM; \
	yyptr += yynewbytes / sizeof (*yyptr);				\
      }									\
    while (YYID (0))

#endif

/* YYFINAL -- State number of the termination state.  */
#define YYFINAL  117
/* YYLAST -- Last index in YYTABLE.  */
#define YYLAST   351

/* YYNTOKENS -- Number of terminals.  */
#define YYNTOKENS  104
/* YYNNTS -- Number of nonterminals.  */
#define YYNNTS  11
/* YYNRULES -- Number of rules.  */
#define YYNRULES  131
/* YYNRULES -- Number of states.  */
#define YYNSTATES  208

/* YYTRANSLATE(YYLEX) -- Bison symbol number corresponding to YYLEX.  */
#define YYUNDEFTOK  2
#define YYMAXUTOK   343

#define YYTRANSLATE(YYX)						\
  ((unsigned int) (YYX) <= YYMAXUTOK ? yytranslate[YYX] : YYUNDEFTOK)

/* YYTRANSLATE[YYLEX] -- Bison symbol number corresponding to YYLEX.  */
static const yytype_uint8 yytranslate[] =
{
       0,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,   101,     2,    81,    82,     2,
      98,   103,    79,    77,    71,    78,    99,    80,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,   102,     2,
       2,    72,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,    83,     2,    84,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     1,     2,     3,     4,
       5,     6,     7,     8,     9,    10,    11,    12,    13,    14,
      15,    16,    17,    18,    19,    20,    21,    22,    23,    24,
      25,    26,    27,    28,    29,    30,    31,    32,    33,    34,
      35,    36,    37,    38,    39,    40,    41,    42,    43,    44,
      45,    46,    47,    48,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    58,    59,    60,    61,    62,    63,    64,
      65,    66,    67,    68,    69,    70,    73,    74,    75,    76,
      85,    86,    87,    88,    89,    90,    91,    92,    93,    94,
      95,    96,    97,   100
};

#if YYDEBUG
/* YYPRHS[YYN] -- Index of the first RHS symbol of rule number YYN in
   YYRHS.  */
static const yytype_uint16 yyprhs[] =
{
       0,     0,     3,     4,     6,     8,    10,    12,    14,    16,
      18,    20,    22,    24,    26,    28,    30,    32,    34,    36,
      38,    41,    44,    46,    49,    52,    55,    60,    62,    65,
      67,    69,    72,    76,    81,    84,    87,    89,    92,    94,
      97,    99,   102,   104,   106,   109,   111,   114,   116,   119,
     123,   126,   130,   133,   135,   138,   141,   144,   146,   148,
     150,   152,   154,   156,   159,   162,   164,   167,   169,   172,
     174,   177,   179,   182,   184,   187,   191,   193,   196,   199,
     203,   206,   208,   212,   213,   217,   219,   221,   225,   229,
     230,   235,   237,   239,   241,   243,   247,   252,   257,   262,
     267,   272,   277,   278,   283,   284,   289,   292,   296,   300,
     304,   308,   312,   316,   320,   323,   327,   331,   334,   337,
     340,   343,   346,   350,   354,   358,   361,   364,   367,   370,
     373,   376
};

/* YYRHS -- A `-1'-separated list of the rules' RHS.  */
static const yytype_int8 yyrhs[] =
{
     105,     0,    -1,    -1,   101,    -1,     9,    -1,     6,    -1,
       7,    -1,     8,    -1,    10,    -1,    11,    -1,    40,    -1,
      39,    -1,    42,    -1,    43,    -1,    44,    -1,    45,    -1,
      41,    -1,    12,    -1,    13,    -1,    46,    -1,    19,   109,
      -1,    20,   109,    -1,    54,    -1,    55,     3,    -1,    56,
       3,    -1,    57,     3,    -1,    21,   109,    22,   109,    -1,
      60,    -1,    60,   109,    -1,    61,    -1,    62,    -1,    62,
     109,    -1,    23,   109,     3,    -1,    23,   109,    84,     3,
      -1,    24,   109,    -1,    25,   106,    -1,    26,    -1,    26,
       3,    -1,    27,    -1,    27,     3,    -1,    28,    -1,    28,
       3,    -1,    29,    -1,    30,    -1,    30,     3,    -1,    31,
      -1,    31,     3,    -1,    32,    -1,    33,   106,    -1,    33,
     106,    35,    -1,    36,     3,    -1,    37,     3,     3,    -1,
      34,     3,    -1,    38,    -1,    38,     3,    -1,    58,     3,
      -1,    59,     3,    -1,    14,    -1,    15,    -1,    16,    -1,
      17,    -1,    18,    -1,    47,    -1,    47,     3,    -1,    48,
       3,    -1,    50,    -1,    50,     3,    -1,    52,    -1,    52,
       3,    -1,    51,    -1,    51,     3,    -1,    53,    -1,    53,
       3,    -1,    49,    -1,    49,     3,    -1,    49,    78,     3,
      -1,     3,    -1,    77,     3,    -1,    78,     3,    -1,     4,
     102,     3,    -1,    79,     3,    -1,   112,    -1,   107,    76,
     112,    -1,    -1,   112,    71,   108,    -1,   112,    -1,   110,
      -1,   109,    71,   112,    -1,   109,    71,   110,    -1,    -1,
     112,     5,   111,   107,    -1,     3,    -1,    64,    -1,    63,
      -1,    65,    -1,   112,    72,   112,    -1,    66,    98,   109,
     103,    -1,    67,    98,   109,   103,    -1,    68,    98,   109,
     103,    -1,    69,    98,   109,   103,    -1,    70,    98,   109,
     103,    -1,   112,    98,   108,   103,    -1,    -1,   112,    75,
     113,   112,    -1,    -1,   112,    74,   114,   112,    -1,    73,
     112,    -1,   112,    77,   112,    -1,   112,    78,   112,    -1,
     112,    79,   112,    -1,   112,    80,   112,    -1,   112,    81,
     112,    -1,   112,    82,   112,    -1,   112,    83,   112,    -1,
      84,   112,    -1,   112,    86,   112,    -1,   112,    85,   112,
      -1,    78,   112,    -1,    95,   112,    -1,   112,    95,    -1,
      94,   112,    -1,   112,    94,    -1,   112,    97,   112,    -1,
     112,    96,   112,    -1,   112,    99,   112,    -1,    93,   112,
      -1,    92,   112,    -1,    91,   112,    -1,    90,   112,    -1,
      89,   112,    -1,    88,   112,    -1,    98,   109,   103,    -1
};

/* YYRLINE[YYN] -- source line where rule number YYN was defined.  */
static const yytype_uint16 yyrline[] =
{
       0,   130,   130,   132,   134,   142,   144,   146,   148,   150,
     152,   154,   165,   167,   169,   171,   173,   175,   182,   189,
     198,   200,   202,   204,   206,   208,   210,   212,   214,   216,
     221,   239,   255,   257,   259,   261,   263,   265,   267,   269,
     271,   273,   275,   277,   279,   281,   283,   285,   287,   289,
     291,   293,   295,   297,   299,   301,   303,   305,   307,   309,
     311,   313,   315,   317,   319,   321,   323,   325,   327,   329,
     331,   333,   335,   337,   339,   341,   349,   350,   351,   352,
     353,   357,   364,   377,   378,   382,   383,   384,   385,   390,
     390,   394,   396,   398,   400,   403,   406,   408,   410,   412,
     415,   425,   443,   443,   445,   445,   447,   450,   452,   454,
     456,   458,   460,   462,   464,   467,   469,   472,   475,   477,
     479,   481,   484,   486,   489,   497,   499,   501,   503,   505,
     507,   509
};
#endif

#if YYDEBUG || YYERROR_VERBOSE || YYTOKEN_TABLE
/* YYTNAME[SYMBOL-NUM] -- String name of the symbol SYMBOL-NUM.
   First, the terminals, then, starting at YYNTOKENS, nonterminals.  */
static const char *const yytname[] =
{
  "$end", "error", "$undefined", "NUM", "DFILE", "CONDITION", "ALIAS",
  "RALIAS", "UNALIAS", "DUMPMEM", "AUTOMAP", "HELP", "UNDO", "REDO",
  "LANGUAGE", "INFOSOURCE", "INFOSOURCES", "COPYING", "WARRANTY", "PRINT",
  "SET", "MOVE", "TO", "GIVE", "REMOVE", "JUMP", "CONT", "STEP", "NEXT",
  "UNTIL", "STEPI", "NEXTI", "FINISH", "BREAK", "DELETE", "IF", "COND",
  "IGNORE", "BREAKPOINTS", "RESTORE", "RESTART", "QUIT", "RECORDON",
  "RECORDOFF", "REPLAY", "REPLAYOFF", "SYMBOL_FILE", "FRAME",
  "SELECT_FRAME", "BACKTRACE", "UP_FRAME", "DOWN_FRAME", "UP_SILENTLY",
  "DOWN_SILENTLY", "DISPLAY", "UNDISPLAY", "DISABLE_DISPLAY",
  "ENABLE_DISPLAY", "DISABLE_BREAK", "ENABLE_BREAK", "OBJECT_TREE", "FIND",
  "LIST_GLOBALS", "BTRUE", "BFALSE", "NOTHING", "PARENT", "CHILD",
  "SIBLING", "CHILDREN", "RANDOM", "','", "'='", "NOTNOT", "OROR",
  "ANDAND", "OR", "'+'", "'-'", "'*'", "'/'", "'%'", "'&'", "'|'", "'~'",
  "WORDARRAY", "BYTEARRAY", "precNEG", "LOCAL", "GLOBAL", "STRING",
  "ROUTINE", "OBJECT", "NUMBER", "DECREMENT", "INCREMENT", "PROPLENGTH",
  "PROPADDR", "'('", "'.'", "SUPERCLASS", "'#'", "':'", "')'", "$accept",
  "input", "linespec", "orlist", "arglist", "commaexp", "condexp", "$@1",
  "exp", "$@2", "$@3", 0
};
#endif

# ifdef YYPRINT
/* YYTOKNUM[YYLEX-NUM] -- Internal token number corresponding to
   token YYLEX-NUM.  */
static const yytype_uint16 yytoknum[] =
{
       0,   256,   257,   258,   259,   260,   261,   262,   263,   264,
     265,   266,   267,   268,   269,   270,   271,   272,   273,   274,
     275,   276,   277,   278,   279,   280,   281,   282,   283,   284,
     285,   286,   287,   288,   289,   290,   291,   292,   293,   294,
     295,   296,   297,   298,   299,   300,   301,   302,   303,   304,
     305,   306,   307,   308,   309,   310,   311,   312,   313,   314,
     315,   316,   317,   318,   319,   320,   321,   322,   323,   324,
     325,    44,    61,   326,   327,   328,   329,    43,    45,    42,
      47,    37,    38,   124,   126,   330,   331,   332,   333,   334,
     335,   336,   337,   338,   339,   340,   341,   342,    40,    46,
     343,    35,    58,    41
};
# endif

/* YYR1[YYN] -- Symbol number of symbol that rule YYN derives.  */
static const yytype_uint8 yyr1[] =
{
       0,   104,   105,   105,   105,   105,   105,   105,   105,   105,
     105,   105,   105,   105,   105,   105,   105,   105,   105,   105,
     105,   105,   105,   105,   105,   105,   105,   105,   105,   105,
     105,   105,   105,   105,   105,   105,   105,   105,   105,   105,
     105,   105,   105,   105,   105,   105,   105,   105,   105,   105,
     105,   105,   105,   105,   105,   105,   105,   105,   105,   105,
     105,   105,   105,   105,   105,   105,   105,   105,   105,   105,
     105,   105,   105,   105,   105,   105,   106,   106,   106,   106,
     106,   107,   107,   108,   108,   109,   109,   109,   109,   111,
     110,   112,   112,   112,   112,   112,   112,   112,   112,   112,
     112,   112,   113,   112,   114,   112,   112,   112,   112,   112,
     112,   112,   112,   112,   112,   112,   112,   112,   112,   112,
     112,   112,   112,   112,   112,   112,   112,   112,   112,   112,
     112,   112
};

/* YYR2[YYN] -- Number of symbols composing right hand side of rule YYN.  */
static const yytype_uint8 yyr2[] =
{
       0,     2,     0,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       2,     2,     1,     2,     2,     2,     4,     1,     2,     1,
       1,     2,     3,     4,     2,     2,     1,     2,     1,     2,
       1,     2,     1,     1,     2,     1,     2,     1,     2,     3,
       2,     3,     2,     1,     2,     2,     2,     1,     1,     1,
       1,     1,     1,     2,     2,     1,     2,     1,     2,     1,
       2,     1,     2,     1,     2,     3,     1,     2,     2,     3,
       2,     1,     3,     0,     3,     1,     1,     3,     3,     0,
       4,     1,     1,     1,     1,     3,     4,     4,     4,     4,
       4,     4,     0,     4,     0,     4,     2,     3,     3,     3,
       3,     3,     3,     3,     2,     3,     3,     2,     2,     2,
       2,     2,     3,     3,     3,     2,     2,     2,     2,     2,
       2,     3
};

/* YYDEFACT[STATE-NAME] -- Default rule to reduce with in state
   STATE-NUM when YYTABLE doesn't specify something else to do.  Zero
   means the default is an error.  */
static const yytype_uint8 yydefact[] =
{
       2,     5,     6,     7,     4,     8,     9,    17,    18,    57,
      58,    59,    60,    61,     0,     0,     0,     0,     0,     0,
      36,    38,    40,    42,    43,    45,    47,     0,     0,     0,
       0,    53,    11,    10,    16,    12,    13,    14,    15,    19,
      62,     0,    73,    65,    69,    67,    71,    22,     0,     0,
       0,     0,     0,    27,    29,    30,     3,     0,    91,    93,
      92,    94,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    20,
      86,    85,    21,     0,     0,    34,    76,     0,     0,     0,
       0,    35,    37,    39,    41,    44,    46,    48,    52,    50,
       0,    54,    63,    64,    74,     0,    66,    70,    68,    72,
      23,    24,    25,    55,    56,    28,    31,     1,     0,     0,
       0,     0,     0,   106,   117,   114,   130,   129,   128,   127,
     126,   125,   120,   118,     0,     0,    89,     0,   104,   102,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   121,
     119,     0,     0,    83,     0,     0,    32,     0,     0,    77,
      78,    80,    49,    51,    75,     0,     0,     0,     0,     0,
     131,    88,    87,     0,    95,     0,     0,   107,   108,   109,
     110,   111,   112,   113,   116,   115,   123,   122,     0,     0,
     124,    26,    33,    79,    96,    97,    98,    99,   100,    90,
      81,   105,   103,   101,    83,     0,    84,    82
};

/* YYDEFGOTO[NTERM-NUM].  */
static const yytype_int16 yydefgoto[] =
{
      -1,    57,    91,   199,   188,    79,    80,   173,    81,   176,
     175
};

/* YYPACT[STATE-NUM] -- Index in YYTABLE of the portion describing
   STATE-NUM.  */
#define YYPACT_NINF -65
static const yytype_int16 yypact[] =
{
     137,   -65,   -65,   -65,   -65,   -65,   -65,   -65,   -65,   -65,
     -65,   -65,   -65,   -65,   156,   156,   156,   156,   156,    16,
      28,    33,    34,   -65,    36,    44,   -65,    16,    46,    47,
      48,    49,   -65,   -65,   -65,   -65,   -65,   -65,   -65,   -65,
      55,    56,    14,    57,    58,    59,    68,   -65,    70,    72,
      73,    84,    93,   156,   -65,   156,   -65,    97,   -65,   -65,
     -65,   -65,     0,     2,     3,     4,    10,   156,   156,   156,
     156,   156,   156,   156,   156,   156,   156,   156,   156,    38,
     -65,   195,    38,    -7,    15,    38,   -65,     9,   118,   127,
     134,   -65,   -65,   -65,   -65,   -65,   -65,   103,   -65,   -65,
     136,   -65,   -65,   -65,   -65,   169,   -65,   -65,   -65,   -65,
     -65,   -65,   -65,   -65,   -65,    38,    38,   -65,   156,   156,
     156,   156,   156,    37,   -29,   116,   -29,   -29,   -29,   -29,
     -29,   -29,    30,    30,   -55,   156,   -65,   156,   -65,   -65,
     156,   156,   156,   156,   156,   156,   156,   156,   156,   -65,
     -65,   156,   156,   156,   156,   156,   -65,   200,   201,   -65,
     -65,   -65,   -65,   -65,   -65,   -50,   -49,   -48,   -47,   -46,
     -65,   -65,   195,   156,   252,   156,   156,   -53,   -53,   116,
     116,   116,   116,   116,   -29,   -29,   -64,   -64,   102,   224,
     -65,    38,   -65,   -65,   -65,   -65,   -65,   -65,   -65,   130,
     252,    37,    37,   -65,   156,   156,   -65,   252
};

/* YYPGOTO[NTERM-NUM].  */
static const yytype_int16 yypgoto[] =
{
     -65,   -65,   180,   -65,     5,   -15,    81,   -65,   -63,   -65,
     -65
};

/* YYTABLE[YYPACT[STATE-NUM]].  What to do in state STATE-NUM.  If
   positive, shift that token.  If negative, reduce the rule which
   number is the opposite.  If zero, do what YYDEFACT says.
   If YYTABLE_NINF, syntax error.  */
#define YYTABLE_NINF -1
static const yytype_int16 yytable[] =
{
      82,    83,    84,    85,   123,   124,   125,   126,   127,   128,
     129,   130,   131,   132,   133,   155,   135,   104,   156,    86,
      87,   135,   135,   135,   135,   135,   142,   143,   144,   145,
     146,    92,   147,   148,   153,   154,    93,    94,   115,    95,
     116,   149,   150,   151,   152,   153,   154,    96,   170,    98,
      99,   100,   101,   194,   195,   196,   197,   198,   102,   103,
     106,   107,   108,   134,   135,   149,   150,   151,   152,   153,
     154,   109,   172,   110,   174,   111,   112,   177,   178,   179,
     180,   181,   182,   183,   184,   185,   135,   113,   186,   187,
     189,   190,   105,    88,    89,    90,   114,   117,   118,   157,
     119,   120,   121,   165,   166,   167,   168,   169,   122,   135,
     200,   158,   201,   202,   140,   141,   142,   143,   144,   145,
     146,   159,   147,   148,    -1,    -1,   151,   152,   153,   154,
     160,   149,   150,   151,   152,   153,   154,   161,   162,   163,
     191,   189,   207,     1,     2,     3,     4,     5,     6,     7,
       8,     9,    10,    11,    12,    13,    14,    15,    16,    58,
      17,    18,    19,    20,    21,    22,    23,    24,    25,    26,
      27,    28,   164,    29,    30,    31,    32,    33,    34,    35,
      36,    37,    38,    39,    40,    41,    42,    43,    44,    45,
      46,    47,    48,    49,    50,    51,    52,    53,    54,    55,
     136,   147,   148,   192,   193,   203,   205,    97,     0,   206,
     149,   150,   151,   152,   153,   154,   171,     0,     0,    59,
      60,    61,    62,    63,    64,    65,    66,     0,     0,    67,
       0,     0,     0,     0,    68,     0,     0,     0,    56,     0,
      69,     0,     0,     0,    70,    71,    72,    73,    74,    75,
      76,    77,     0,     0,    78,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   137,     0,   138,
     139,     0,   140,   141,   142,   143,   144,   145,   146,     0,
     147,   148,     0,     0,     0,     0,     0,     0,     0,   149,
     150,   151,   152,   153,   154,   204,   137,     0,   138,   139,
       0,   140,   141,   142,   143,   144,   145,   146,     0,   147,
     148,     0,     0,     0,     0,     0,     0,     0,   149,   150,
     151,   152,   153,   154,   137,     0,   138,   139,     0,   140,
     141,   142,   143,   144,   145,   146,     0,   147,   148,     0,
       0,     0,     0,     0,     0,     0,   149,   150,   151,   152,
     153,   154
};

static const yytype_int16 yycheck[] =
{
      15,    16,    17,    18,    67,    68,    69,    70,    71,    72,
      73,    74,    75,    76,    77,    22,    71,     3,     3,     3,
       4,    71,    71,    71,    71,    71,    79,    80,    81,    82,
      83,     3,    85,    86,    98,    99,     3,     3,    53,     3,
      55,    94,    95,    96,    97,    98,    99,     3,   103,     3,
       3,     3,     3,   103,   103,   103,   103,   103,     3,     3,
       3,     3,     3,    78,    71,    94,    95,    96,    97,    98,
      99,     3,   135,     3,   137,     3,     3,   140,   141,   142,
     143,   144,   145,   146,   147,   148,    71,     3,   151,   152,
     153,   154,    78,    77,    78,    79,     3,     0,    98,    84,
      98,    98,    98,   118,   119,   120,   121,   122,    98,    71,
     173,   102,   175,   176,    77,    78,    79,    80,    81,    82,
      83,     3,    85,    86,    94,    95,    96,    97,    98,    99,
       3,    94,    95,    96,    97,    98,    99,     3,    35,     3,
     155,   204,   205,     6,     7,     8,     9,    10,    11,    12,
      13,    14,    15,    16,    17,    18,    19,    20,    21,     3,
      23,    24,    25,    26,    27,    28,    29,    30,    31,    32,
      33,    34,     3,    36,    37,    38,    39,    40,    41,    42,
      43,    44,    45,    46,    47,    48,    49,    50,    51,    52,
      53,    54,    55,    56,    57,    58,    59,    60,    61,    62,
       5,    85,    86,     3,     3,   103,    76,    27,    -1,   204,
      94,    95,    96,    97,    98,    99,   135,    -1,    -1,    63,
      64,    65,    66,    67,    68,    69,    70,    -1,    -1,    73,
      -1,    -1,    -1,    -1,    78,    -1,    -1,    -1,   101,    -1,
      84,    -1,    -1,    -1,    88,    89,    90,    91,    92,    93,
      94,    95,    -1,    -1,    98,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    72,    -1,    74,
      75,    -1,    77,    78,    79,    80,    81,    82,    83,    -1,
      85,    86,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    94,
      95,    96,    97,    98,    99,    71,    72,    -1,    74,    75,
      -1,    77,    78,    79,    80,    81,    82,    83,    -1,    85,
      86,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    94,    95,
      96,    97,    98,    99,    72,    -1,    74,    75,    -1,    77,
      78,    79,    80,    81,    82,    83,    -1,    85,    86,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    94,    95,    96,    97,
      98,    99
};

/* YYSTOS[STATE-NUM] -- The (internal number of the) accessing
   symbol of state STATE-NUM.  */
static const yytype_uint8 yystos[] =
{
       0,     6,     7,     8,     9,    10,    11,    12,    13,    14,
      15,    16,    17,    18,    19,    20,    21,    23,    24,    25,
      26,    27,    28,    29,    30,    31,    32,    33,    34,    36,
      37,    38,    39,    40,    41,    42,    43,    44,    45,    46,
      47,    48,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    58,    59,    60,    61,    62,   101,   105,     3,    63,
      64,    65,    66,    67,    68,    69,    70,    73,    78,    84,
      88,    89,    90,    91,    92,    93,    94,    95,    98,   109,
     110,   112,   109,   109,   109,   109,     3,     4,    77,    78,
      79,   106,     3,     3,     3,     3,     3,   106,     3,     3,
       3,     3,     3,     3,     3,    78,     3,     3,     3,     3,
       3,     3,     3,     3,     3,   109,   109,     0,    98,    98,
      98,    98,    98,   112,   112,   112,   112,   112,   112,   112,
     112,   112,   112,   112,   109,    71,     5,    72,    74,    75,
      77,    78,    79,    80,    81,    82,    83,    85,    86,    94,
      95,    96,    97,    98,    99,    22,     3,    84,   102,     3,
       3,     3,    35,     3,     3,   109,   109,   109,   109,   109,
     103,   110,   112,   111,   112,   114,   113,   112,   112,   112,
     112,   112,   112,   112,   112,   112,   112,   112,   108,   112,
     112,   109,     3,     3,   103,   103,   103,   103,   103,   107,
     112,   112,   112,   103,    71,    76,   108,   112
};

#define yyerrok		(yyerrstatus = 0)
#define yyclearin	(yychar = YYEMPTY)
#define YYEMPTY		(-2)
#define YYEOF		0

#define YYACCEPT	goto yyacceptlab
#define YYABORT		goto yyabortlab
#define YYERROR		goto yyerrorlab


/* Like YYERROR except do call yyerror.  This remains here temporarily
   to ease the transition to the new meaning of YYERROR, for GCC.
   Once GCC version 2 has supplanted version 1, this can go.  */

#define YYFAIL		goto yyerrlab

#define YYRECOVERING()  (!!yyerrstatus)

#define YYBACKUP(Token, Value)					\
do								\
  if (yychar == YYEMPTY && yylen == 1)				\
    {								\
      yychar = (Token);						\
      yylval = (Value);						\
      yytoken = YYTRANSLATE (yychar);				\
      YYPOPSTACK (1);						\
      goto yybackup;						\
    }								\
  else								\
    {								\
      yyerror (YY_("syntax error: cannot back up")); \
      YYERROR;							\
    }								\
while (YYID (0))


#define YYTERROR	1
#define YYERRCODE	256


/* YYLLOC_DEFAULT -- Set CURRENT to span from RHS[1] to RHS[N].
   If N is 0, then set CURRENT to the empty location which ends
   the previous symbol: RHS[0] (always defined).  */

#define YYRHSLOC(Rhs, K) ((Rhs)[K])
#ifndef YYLLOC_DEFAULT
# define YYLLOC_DEFAULT(Current, Rhs, N)				\
    do									\
      if (YYID (N))                                                    \
	{								\
	  (Current).first_line   = YYRHSLOC (Rhs, 1).first_line;	\
	  (Current).first_column = YYRHSLOC (Rhs, 1).first_column;	\
	  (Current).last_line    = YYRHSLOC (Rhs, N).last_line;		\
	  (Current).last_column  = YYRHSLOC (Rhs, N).last_column;	\
	}								\
      else								\
	{								\
	  (Current).first_line   = (Current).last_line   =		\
	    YYRHSLOC (Rhs, 0).last_line;				\
	  (Current).first_column = (Current).last_column =		\
	    YYRHSLOC (Rhs, 0).last_column;				\
	}								\
    while (YYID (0))
#endif


/* YY_LOCATION_PRINT -- Print the location on the stream.
   This macro was not mandated originally: define only if we know
   we won't break user code: when these are the locations we know.  */

#ifndef YY_LOCATION_PRINT
# if YYLTYPE_IS_TRIVIAL
#  define YY_LOCATION_PRINT(File, Loc)			\
     fprintf (File, "%d.%d-%d.%d",			\
	      (Loc).first_line, (Loc).first_column,	\
	      (Loc).last_line,  (Loc).last_column)
# else
#  define YY_LOCATION_PRINT(File, Loc) ((void) 0)
# endif
#endif


/* YYLEX -- calling `yylex' with the right arguments.  */

#ifdef YYLEX_PARAM
# define YYLEX yylex (YYLEX_PARAM)
#else
# define YYLEX yylex ()
#endif

/* Enable debugging if requested.  */
#if YYDEBUG

# ifndef YYFPRINTF
#  include <stdio.h> /* INFRINGES ON USER NAME SPACE */
#  define YYFPRINTF fprintf
# endif

# define YYDPRINTF(Args)			\
do {						\
  if (yydebug)					\
    YYFPRINTF Args;				\
} while (YYID (0))

# define YY_SYMBOL_PRINT(Title, Type, Value, Location)			  \
do {									  \
  if (yydebug)								  \
    {									  \
      YYFPRINTF (stderr, "%s ", Title);					  \
      yy_symbol_print (stderr,						  \
		  Type, Value); \
      YYFPRINTF (stderr, "\n");						  \
    }									  \
} while (YYID (0))


/*--------------------------------.
| Print this symbol on YYOUTPUT.  |
`--------------------------------*/

/*ARGSUSED*/
#if (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
static void
yy_symbol_value_print (FILE *yyoutput, int yytype, YYSTYPE const * const yyvaluep)
#else
static void
yy_symbol_value_print (yyoutput, yytype, yyvaluep)
    FILE *yyoutput;
    int yytype;
    YYSTYPE const * const yyvaluep;
#endif
{
  if (!yyvaluep)
    return;
# ifdef YYPRINT
  if (yytype < YYNTOKENS)
    YYPRINT (yyoutput, yytoknum[yytype], *yyvaluep);
# else
  YYUSE (yyoutput);
# endif
  switch (yytype)
    {
      default:
	break;
    }
}


/*--------------------------------.
| Print this symbol on YYOUTPUT.  |
`--------------------------------*/

#if (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
static void
yy_symbol_print (FILE *yyoutput, int yytype, YYSTYPE const * const yyvaluep)
#else
static void
yy_symbol_print (yyoutput, yytype, yyvaluep)
    FILE *yyoutput;
    int yytype;
    YYSTYPE const * const yyvaluep;
#endif
{
  if (yytype < YYNTOKENS)
    YYFPRINTF (yyoutput, "token %s (", yytname[yytype]);
  else
    YYFPRINTF (yyoutput, "nterm %s (", yytname[yytype]);

  yy_symbol_value_print (yyoutput, yytype, yyvaluep);
  YYFPRINTF (yyoutput, ")");
}

/*------------------------------------------------------------------.
| yy_stack_print -- Print the state stack from its BOTTOM up to its |
| TOP (included).                                                   |
`------------------------------------------------------------------*/

#if (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
static void
yy_stack_print (yytype_int16 *yybottom, yytype_int16 *yytop)
#else
static void
yy_stack_print (yybottom, yytop)
    yytype_int16 *yybottom;
    yytype_int16 *yytop;
#endif
{
  YYFPRINTF (stderr, "Stack now");
  for (; yybottom <= yytop; yybottom++)
    {
      int yybot = *yybottom;
      YYFPRINTF (stderr, " %d", yybot);
    }
  YYFPRINTF (stderr, "\n");
}

# define YY_STACK_PRINT(Bottom, Top)				\
do {								\
  if (yydebug)							\
    yy_stack_print ((Bottom), (Top));				\
} while (YYID (0))


/*------------------------------------------------.
| Report that the YYRULE is going to be reduced.  |
`------------------------------------------------*/

#if (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
static void
yy_reduce_print (YYSTYPE *yyvsp, int yyrule)
#else
static void
yy_reduce_print (yyvsp, yyrule)
    YYSTYPE *yyvsp;
    int yyrule;
#endif
{
  int yynrhs = yyr2[yyrule];
  int yyi;
  unsigned long int yylno = yyrline[yyrule];
  YYFPRINTF (stderr, "Reducing stack by rule %d (line %lu):\n",
	     yyrule - 1, yylno);
  /* The symbols being reduced.  */
  for (yyi = 0; yyi < yynrhs; yyi++)
    {
      YYFPRINTF (stderr, "   $%d = ", yyi + 1);
      yy_symbol_print (stderr, yyrhs[yyprhs[yyrule] + yyi],
		       &(yyvsp[(yyi + 1) - (yynrhs)])
		       		       );
      YYFPRINTF (stderr, "\n");
    }
}

# define YY_REDUCE_PRINT(Rule)		\
do {					\
  if (yydebug)				\
    yy_reduce_print (yyvsp, Rule); \
} while (YYID (0))

/* Nonzero means print parse trace.  It is left uninitialized so that
   multiple parsers can coexist.  */
int yydebug;
#else /* !YYDEBUG */
# define YYDPRINTF(Args)
# define YY_SYMBOL_PRINT(Title, Type, Value, Location)
# define YY_STACK_PRINT(Bottom, Top)
# define YY_REDUCE_PRINT(Rule)
#endif /* !YYDEBUG */


/* YYINITDEPTH -- initial size of the parser's stacks.  */
#ifndef	YYINITDEPTH
# define YYINITDEPTH 200
#endif

/* YYMAXDEPTH -- maximum size the stacks can grow to (effective only
   if the built-in stack extension method is used).

   Do not make this value too large; the results are undefined if
   YYSTACK_ALLOC_MAXIMUM < YYSTACK_BYTES (YYMAXDEPTH)
   evaluated with infinite-precision integer arithmetic.  */

#ifndef YYMAXDEPTH
# define YYMAXDEPTH 10000
#endif



#if YYERROR_VERBOSE

# ifndef yystrlen
#  if defined __GLIBC__ && defined _STRING_H
#   define yystrlen strlen
#  else
/* Return the length of YYSTR.  */
#if (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
static YYSIZE_T
yystrlen (const char *yystr)
#else
static YYSIZE_T
yystrlen (yystr)
    const char *yystr;
#endif
{
  YYSIZE_T yylen;
  for (yylen = 0; yystr[yylen]; yylen++)
    continue;
  return yylen;
}
#  endif
# endif

# ifndef yystpcpy
#  if defined __GLIBC__ && defined _STRING_H && defined _GNU_SOURCE
#   define yystpcpy stpcpy
#  else
/* Copy YYSRC to YYDEST, returning the address of the terminating '\0' in
   YYDEST.  */
#if (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
static char *
yystpcpy (char *yydest, const char *yysrc)
#else
static char *
yystpcpy (yydest, yysrc)
    char *yydest;
    const char *yysrc;
#endif
{
  char *yyd = yydest;
  const char *yys = yysrc;

  while ((*yyd++ = *yys++) != '\0')
    continue;

  return yyd - 1;
}
#  endif
# endif

# ifndef yytnamerr
/* Copy to YYRES the contents of YYSTR after stripping away unnecessary
   quotes and backslashes, so that it's suitable for yyerror.  The
   heuristic is that double-quoting is unnecessary unless the string
   contains an apostrophe, a comma, or backslash (other than
   backslash-backslash).  YYSTR is taken from yytname.  If YYRES is
   null, do not copy; instead, return the length of what the result
   would have been.  */
static YYSIZE_T
yytnamerr (char *yyres, const char *yystr)
{
  if (*yystr == '"')
    {
      YYSIZE_T yyn = 0;
      char const *yyp = yystr;

      for (;;)
	switch (*++yyp)
	  {
	  case '\'':
	  case ',':
	    goto do_not_strip_quotes;

	  case '\\':
	    if (*++yyp != '\\')
	      goto do_not_strip_quotes;
	    /* Fall through.  */
	  default:
	    if (yyres)
	      yyres[yyn] = *yyp;
	    yyn++;
	    break;

	  case '"':
	    if (yyres)
	      yyres[yyn] = '\0';
	    return yyn;
	  }
    do_not_strip_quotes: ;
    }

  if (! yyres)
    return yystrlen (yystr);

  return yystpcpy (yyres, yystr) - yyres;
}
# endif

/* Copy into YYRESULT an error message about the unexpected token
   YYCHAR while in state YYSTATE.  Return the number of bytes copied,
   including the terminating null byte.  If YYRESULT is null, do not
   copy anything; just return the number of bytes that would be
   copied.  As a special case, return 0 if an ordinary "syntax error"
   message will do.  Return YYSIZE_MAXIMUM if overflow occurs during
   size calculation.  */
static YYSIZE_T
yysyntax_error (char *yyresult, int yystate, int yychar)
{
  int yyn = yypact[yystate];

  if (! (YYPACT_NINF < yyn && yyn <= YYLAST))
    return 0;
  else
    {
      int yytype = YYTRANSLATE (yychar);
      YYSIZE_T yysize0 = yytnamerr (0, yytname[yytype]);
      YYSIZE_T yysize = yysize0;
      YYSIZE_T yysize1;
      int yysize_overflow = 0;
      enum { YYERROR_VERBOSE_ARGS_MAXIMUM = 5 };
      char const *yyarg[YYERROR_VERBOSE_ARGS_MAXIMUM];
      int yyx;

# if 0
      /* This is so xgettext sees the translatable formats that are
	 constructed on the fly.  */
      YY_("syntax error, unexpected %s");
      YY_("syntax error, unexpected %s, expecting %s");
      YY_("syntax error, unexpected %s, expecting %s or %s");
      YY_("syntax error, unexpected %s, expecting %s or %s or %s");
      YY_("syntax error, unexpected %s, expecting %s or %s or %s or %s");
# endif
      char *yyfmt;
      char const *yyf;
      static char const yyunexpected[] = "syntax error, unexpected %s";
      static char const yyexpecting[] = ", expecting %s";
      static char const yyor[] = " or %s";
      char yyformat[sizeof yyunexpected
		    + sizeof yyexpecting - 1
		    + ((YYERROR_VERBOSE_ARGS_MAXIMUM - 2)
		       * (sizeof yyor - 1))];
      char const *yyprefix = yyexpecting;

      /* Start YYX at -YYN if negative to avoid negative indexes in
	 YYCHECK.  */
      int yyxbegin = yyn < 0 ? -yyn : 0;

      /* Stay within bounds of both yycheck and yytname.  */
      int yychecklim = YYLAST - yyn + 1;
      int yyxend = yychecklim < YYNTOKENS ? yychecklim : YYNTOKENS;
      int yycount = 1;

      yyarg[0] = yytname[yytype];
      yyfmt = yystpcpy (yyformat, yyunexpected);

      for (yyx = yyxbegin; yyx < yyxend; ++yyx)
	if (yycheck[yyx + yyn] == yyx && yyx != YYTERROR)
	  {
	    if (yycount == YYERROR_VERBOSE_ARGS_MAXIMUM)
	      {
		yycount = 1;
		yysize = yysize0;
		yyformat[sizeof yyunexpected - 1] = '\0';
		break;
	      }
	    yyarg[yycount++] = yytname[yyx];
	    yysize1 = yysize + yytnamerr (0, yytname[yyx]);
	    yysize_overflow |= (yysize1 < yysize);
	    yysize = yysize1;
	    yyfmt = yystpcpy (yyfmt, yyprefix);
	    yyprefix = yyor;
	  }

      yyf = YY_(yyformat);
      yysize1 = yysize + yystrlen (yyf);
      yysize_overflow |= (yysize1 < yysize);
      yysize = yysize1;

      if (yysize_overflow)
	return YYSIZE_MAXIMUM;

      if (yyresult)
	{
	  /* Avoid sprintf, as that infringes on the user's name space.
	     Don't have undefined behavior even if the translation
	     produced a string with the wrong number of "%s"s.  */
	  char *yyp = yyresult;
	  int yyi = 0;
	  while ((*yyp = *yyf) != '\0')
	    {
	      if (*yyp == '%' && yyf[1] == 's' && yyi < yycount)
		{
		  yyp += yytnamerr (yyp, yyarg[yyi++]);
		  yyf += 2;
		}
	      else
		{
		  yyp++;
		  yyf++;
		}
	    }
	}
      return yysize;
    }
}
#endif /* YYERROR_VERBOSE */


/*-----------------------------------------------.
| Release the memory associated to this symbol.  |
`-----------------------------------------------*/

/*ARGSUSED*/
#if (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
static void
yydestruct (const char *yymsg, int yytype, YYSTYPE *yyvaluep)
#else
static void
yydestruct (yymsg, yytype, yyvaluep)
    const char *yymsg;
    int yytype;
    YYSTYPE *yyvaluep;
#endif
{
  YYUSE (yyvaluep);

  if (!yymsg)
    yymsg = "Deleting";
  YY_SYMBOL_PRINT (yymsg, yytype, yyvaluep, yylocationp);

  switch (yytype)
    {

      default:
	break;
    }
}

/* Prevent warnings from -Wmissing-prototypes.  */
#ifdef YYPARSE_PARAM
#if defined __STDC__ || defined __cplusplus
int yyparse (void *YYPARSE_PARAM);
#else
int yyparse ();
#endif
#else /* ! YYPARSE_PARAM */
#if defined __STDC__ || defined __cplusplus
int yyparse (void);
#else
int yyparse ();
#endif
#endif /* ! YYPARSE_PARAM */


/* The lookahead symbol.  */
int yychar;

/* The semantic value of the lookahead symbol.  */
YYSTYPE yylval;

/* Number of syntax errors so far.  */
int yynerrs;



/*-------------------------.
| yyparse or yypush_parse.  |
`-------------------------*/

#ifdef YYPARSE_PARAM
#if (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
int
yyparse (void *YYPARSE_PARAM)
#else
int
yyparse (YYPARSE_PARAM)
    void *YYPARSE_PARAM;
#endif
#else /* ! YYPARSE_PARAM */
#if (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
int
yyparse (void)
#else
int
yyparse ()

#endif
#endif
{


    int yystate;
    /* Number of tokens to shift before error messages enabled.  */
    int yyerrstatus;

    /* The stacks and their tools:
       `yyss': related to states.
       `yyvs': related to semantic values.

       Refer to the stacks thru separate pointers, to allow yyoverflow
       to reallocate them elsewhere.  */

    /* The state stack.  */
    yytype_int16 yyssa[YYINITDEPTH];
    yytype_int16 *yyss;
    yytype_int16 *yyssp;

    /* The semantic value stack.  */
    YYSTYPE yyvsa[YYINITDEPTH];
    YYSTYPE *yyvs;
    YYSTYPE *yyvsp;

    YYSIZE_T yystacksize;

  int yyn;
  int yyresult;
  /* Lookahead token as an internal (translated) token number.  */
  int yytoken;
  /* The variables used to return semantic value and location from the
     action routines.  */
  YYSTYPE yyval;

#if YYERROR_VERBOSE
  /* Buffer for error messages, and its allocated size.  */
  char yymsgbuf[128];
  char *yymsg = yymsgbuf;
  YYSIZE_T yymsg_alloc = sizeof yymsgbuf;
#endif

#define YYPOPSTACK(N)   (yyvsp -= (N), yyssp -= (N))

  /* The number of symbols on the RHS of the reduced rule.
     Keep to zero when no symbol should be popped.  */
  int yylen = 0;

  yytoken = 0;
  yyss = yyssa;
  yyvs = yyvsa;
  yystacksize = YYINITDEPTH;

  YYDPRINTF ((stderr, "Starting parse\n"));

  yystate = 0;
  yyerrstatus = 0;
  yynerrs = 0;
  yychar = YYEMPTY; /* Cause a token to be read.  */

  /* Initialize stack pointers.
     Waste one element of value and location stack
     so that they stay on the same level as the state stack.
     The wasted elements are never initialized.  */
  yyssp = yyss;
  yyvsp = yyvs;

  goto yysetstate;

/*------------------------------------------------------------.
| yynewstate -- Push a new state, which is found in yystate.  |
`------------------------------------------------------------*/
 yynewstate:
  /* In all cases, when you get here, the value and location stacks
     have just been pushed.  So pushing a state here evens the stacks.  */
  yyssp++;

 yysetstate:
  *yyssp = yystate;

  if (yyss + yystacksize - 1 <= yyssp)
    {
      /* Get the current used size of the three stacks, in elements.  */
      YYSIZE_T yysize = yyssp - yyss + 1;

#ifdef yyoverflow
      {
	/* Give user a chance to reallocate the stack.  Use copies of
	   these so that the &'s don't force the real ones into
	   memory.  */
	YYSTYPE *yyvs1 = yyvs;
	yytype_int16 *yyss1 = yyss;

	/* Each stack pointer address is followed by the size of the
	   data in use in that stack, in bytes.  This used to be a
	   conditional around just the two extra args, but that might
	   be undefined if yyoverflow is a macro.  */
	yyoverflow (YY_("memory exhausted"),
		    &yyss1, yysize * sizeof (*yyssp),
		    &yyvs1, yysize * sizeof (*yyvsp),
		    &yystacksize);

	yyss = yyss1;
	yyvs = yyvs1;
      }
#else /* no yyoverflow */
# ifndef YYSTACK_RELOCATE
      goto yyexhaustedlab;
# else
      /* Extend the stack our own way.  */
      if (YYMAXDEPTH <= yystacksize)
	goto yyexhaustedlab;
      yystacksize *= 2;
      if (YYMAXDEPTH < yystacksize)
	yystacksize = YYMAXDEPTH;

      {
	yytype_int16 *yyss1 = yyss;
	union yyalloc *yyptr =
	  (union yyalloc *) YYSTACK_ALLOC (YYSTACK_BYTES (yystacksize));
	if (! yyptr)
	  goto yyexhaustedlab;
	YYSTACK_RELOCATE (yyss_alloc, yyss);
	YYSTACK_RELOCATE (yyvs_alloc, yyvs);
#  undef YYSTACK_RELOCATE
	if (yyss1 != yyssa)
	  YYSTACK_FREE (yyss1);
      }
# endif
#endif /* no yyoverflow */

      yyssp = yyss + yysize - 1;
      yyvsp = yyvs + yysize - 1;

      YYDPRINTF ((stderr, "Stack size increased to %lu\n",
		  (unsigned long int) yystacksize));

      if (yyss + yystacksize - 1 <= yyssp)
	YYABORT;
    }

  YYDPRINTF ((stderr, "Entering state %d\n", yystate));

  if (yystate == YYFINAL)
    YYACCEPT;

  goto yybackup;

/*-----------.
| yybackup.  |
`-----------*/
yybackup:

  /* Do appropriate processing given the current state.  Read a
     lookahead token if we need one and don't already have one.  */

  /* First try to decide what to do without reference to lookahead token.  */
  yyn = yypact[yystate];
  if (yyn == YYPACT_NINF)
    goto yydefault;

  /* Not known => get a lookahead token if don't already have one.  */

  /* YYCHAR is either YYEMPTY or YYEOF or a valid lookahead symbol.  */
  if (yychar == YYEMPTY)
    {
      YYDPRINTF ((stderr, "Reading a token: "));
      yychar = YYLEX;
    }

  if (yychar <= YYEOF)
    {
      yychar = yytoken = YYEOF;
      YYDPRINTF ((stderr, "Now at end of input.\n"));
    }
  else
    {
      yytoken = YYTRANSLATE (yychar);
      YY_SYMBOL_PRINT ("Next token is", yytoken, &yylval, &yylloc);
    }

  /* If the proper action on seeing token YYTOKEN is to reduce or to
     detect an error, take that action.  */
  yyn += yytoken;
  if (yyn < 0 || YYLAST < yyn || yycheck[yyn] != yytoken)
    goto yydefault;
  yyn = yytable[yyn];
  if (yyn <= 0)
    {
      if (yyn == 0 || yyn == YYTABLE_NINF)
	goto yyerrlab;
      yyn = -yyn;
      goto yyreduce;
    }

  /* Count tokens shifted since error; after three, turn off error
     status.  */
  if (yyerrstatus)
    yyerrstatus--;

  /* Shift the lookahead token.  */
  YY_SYMBOL_PRINT ("Shifting", yytoken, &yylval, &yylloc);

  /* Discard the shifted token.  */
  yychar = YYEMPTY;

  yystate = yyn;
  *++yyvsp = yylval;

  goto yynewstate;


/*-----------------------------------------------------------.
| yydefault -- do the default action for the current state.  |
`-----------------------------------------------------------*/
yydefault:
  yyn = yydefact[yystate];
  if (yyn == 0)
    goto yyerrlab;
  goto yyreduce;


/*-----------------------------.
| yyreduce -- Do a reduction.  |
`-----------------------------*/
yyreduce:
  /* yyn is the number of a rule to reduce with.  */
  yylen = yyr2[yyn];

  /* If YYLEN is nonzero, implement the default value of the action:
     `$$ = $1'.

     Otherwise, the following line sets YYVAL to garbage.
     This behavior is undocumented and Bison
     users should not rely upon it.  Assigning to YYVAL
     unconditionally makes the parser a bit smaller, and it avoids a
     GCC warning that YYVAL may be used uninitialized.  */
  yyval = yyvsp[1-yylen];


  YY_REDUCE_PRINT (yyn);
  switch (yyn)
    {
        case 3:

/* Line 1455 of yacc.c  */
#line 132 "inform.y"
    { lex_offset = n_strlen(lex_expression); }
    break;

  case 4:

/* Line 1455 of yacc.c  */
#line 134 "inform.y"
    {
		strid_t f;
		f = n_file_name_or_prompt(fileusage_Data|fileusage_BinaryMode,
					  filemode_Write, lex_tail());
		w_glk_put_buffer_stream(f, (char *) z_memory, total_size);
		glk_stream_close(f, NULL);
	}
    break;

  case 5:

/* Line 1455 of yacc.c  */
#line 142 "inform.y"
    { parse_new_alias(lex_tail(), FALSE); }
    break;

  case 6:

/* Line 1455 of yacc.c  */
#line 144 "inform.y"
    { parse_new_alias(lex_tail(), TRUE); }
    break;

  case 7:

/* Line 1455 of yacc.c  */
#line 146 "inform.y"
    { remove_alias(lex_tail()); }
    break;

  case 8:

/* Line 1455 of yacc.c  */
#line 148 "inform.y"
    { automap_init(object_count, lex_tail()); }
    break;

  case 9:

/* Line 1455 of yacc.c  */
#line 150 "inform.y"
    { inform_help(); }
    break;

  case 10:

/* Line 1455 of yacc.c  */
#line 152 "inform.y"
    { op_restart(); exit_debugger = TRUE; read_abort = TRUE;  }
    break;

  case 11:

/* Line 1455 of yacc.c  */
#line 154 "inform.y"
    {
		if(restoregame()) {
		  exit_debugger = TRUE; read_abort = TRUE;
		  if(zversion <= 3)
		    mop_take_branch();
		  else
		    mop_store_result(2);
		} else {
		  infix_print_string("Restore failed.\n");
		} }
    break;

  case 12:

/* Line 1455 of yacc.c  */
#line 165 "inform.y"
    { zword oldop0 = operand[0]; operand[0] = 4; op_output_stream(); operand[0] = oldop0; }
    break;

  case 13:

/* Line 1455 of yacc.c  */
#line 167 "inform.y"
    { zword oldop0 = operand[0]; operand[0] = neg(4); op_output_stream(); operand[0] = oldop0; }
    break;

  case 14:

/* Line 1455 of yacc.c  */
#line 169 "inform.y"
    { zword oldop0 = operand[0]; operand[0] = 1; op_input_stream(); operand[0] = oldop0; exit_debugger = TRUE; }
    break;

  case 15:

/* Line 1455 of yacc.c  */
#line 171 "inform.y"
    { zword oldop0 = operand[0]; operand[0] = 0; op_input_stream(); operand[0] = oldop0; }
    break;

  case 16:

/* Line 1455 of yacc.c  */
#line 173 "inform.y"
    { z_close(); glk_exit();	}
    break;

  case 17:

/* Line 1455 of yacc.c  */
#line 175 "inform.y"
    {
		if(restoreundo()) {
		  read_abort = TRUE; exit_debugger = TRUE;
		} else {
		  infix_print_string("No undo slots.\n");
		} }
    break;

  case 18:

/* Line 1455 of yacc.c  */
#line 182 "inform.y"
    {
		if(restoreredo()) {
		  read_abort = TRUE; exit_debugger = TRUE;
		} else {
		  infix_print_string("No redo slots.\n");
		} }
    break;

  case 19:

/* Line 1455 of yacc.c  */
#line 189 "inform.y"
    {
		strid_t f;
		f = n_file_name_or_prompt(fileusage_Data|fileusage_BinaryMode,
					  filemode_Read, lex_tail());
		if(f) {
		  kill_infix();
		  init_infix(f);
		} }
    break;

  case 20:

/* Line 1455 of yacc.c  */
#line 198 "inform.y"
    { infix_display((yyvsp[(2) - (2)].val));		}
    break;

  case 21:

/* Line 1455 of yacc.c  */
#line 200 "inform.y"
    { inform_result = (yyvsp[(2) - (2)].val);		}
    break;

  case 22:

/* Line 1455 of yacc.c  */
#line 202 "inform.y"
    { infix_auto_display(lex_tail()); }
    break;

  case 23:

/* Line 1455 of yacc.c  */
#line 204 "inform.y"
    { infix_auto_undisplay((yyvsp[(2) - (2)].val).v);	}
    break;

  case 24:

/* Line 1455 of yacc.c  */
#line 206 "inform.y"
    { infix_set_display_enabled((yyvsp[(2) - (2)].val).v, FALSE); }
    break;

  case 25:

/* Line 1455 of yacc.c  */
#line 208 "inform.y"
    { infix_set_display_enabled((yyvsp[(2) - (2)].val).v, TRUE); }
    break;

  case 26:

/* Line 1455 of yacc.c  */
#line 210 "inform.y"
    { infix_move((yyvsp[(4) - (4)].val).v, (yyvsp[(2) - (4)].val).v); 	}
    break;

  case 27:

/* Line 1455 of yacc.c  */
#line 212 "inform.y"
    { infix_object_tree(0);		}
    break;

  case 28:

/* Line 1455 of yacc.c  */
#line 214 "inform.y"
    { infix_object_tree((yyvsp[(2) - (2)].val).v);	}
    break;

  case 29:

/* Line 1455 of yacc.c  */
#line 216 "inform.y"
    {
		if(lex_expression[lex_offset])
		  infix_object_find(lex_tail());
	}
    break;

  case 30:

/* Line 1455 of yacc.c  */
#line 221 "inform.y"
    {
		z_typed v; v.t = Z_GLOBAL;
		for(v.o = 0; v.o <= 245; v.o++) {
		  const char *name = infix_get_name(v);
		  if(v.o) infix_print_string("; ");
		  if(name) {
		    infix_print_string(name);
		  } else {
		    infix_print_char('G');
		    infix_print_number(v.o);
		  }
		  infix_print_char('=');
		  infix_get_val(&v);
		  infix_print_number(v.v);
		}
		infix_print_char(10);
	}
    break;

  case 31:

/* Line 1455 of yacc.c  */
#line 239 "inform.y"
    {
		z_typed v; v.t = Z_GLOBAL;
		for(v.o = 0; v.o <= 245; v.o++) {
		  infix_get_val(&v);
		  if(v.v == (yyvsp[(2) - (2)].val).v) {
		    const char *name = infix_get_name(v);
		    if(name) {
		      infix_print_string(name);
		    } else {
		      infix_print_char('G');
		      infix_print_number(v.o);
		    }
		    infix_print_char(10);
		  }
		} }
    break;

  case 32:

/* Line 1455 of yacc.c  */
#line 255 "inform.y"
    { infix_set_attrib((yyvsp[(2) - (3)].val).v, (yyvsp[(3) - (3)].val).v);	}
    break;

  case 33:

/* Line 1455 of yacc.c  */
#line 257 "inform.y"
    { infix_clear_attrib((yyvsp[(2) - (4)].val).v, (yyvsp[(4) - (4)].val).v); }
    break;

  case 34:

/* Line 1455 of yacc.c  */
#line 259 "inform.y"
    { infix_remove((yyvsp[(2) - (2)].val).v);		}
    break;

  case 35:

/* Line 1455 of yacc.c  */
#line 261 "inform.y"
    { PC=(yyvsp[(2) - (2)].pcoffset); exit_debugger = TRUE;	}
    break;

  case 36:

/* Line 1455 of yacc.c  */
#line 263 "inform.y"
    { set_step(CONT_GO, 1); }
    break;

  case 37:

/* Line 1455 of yacc.c  */
#line 265 "inform.y"
    { set_step(CONT_GO, 1); infix_set_ignore(cur_break, (yyvsp[(2) - (2)].val).v); }
    break;

  case 38:

/* Line 1455 of yacc.c  */
#line 267 "inform.y"
    { set_step(CONT_STEP, 1); }
    break;

  case 39:

/* Line 1455 of yacc.c  */
#line 269 "inform.y"
    { set_step(CONT_STEP, (yyvsp[(2) - (2)].val).v); }
    break;

  case 40:

/* Line 1455 of yacc.c  */
#line 271 "inform.y"
    { set_step(CONT_NEXT, 1); }
    break;

  case 41:

/* Line 1455 of yacc.c  */
#line 273 "inform.y"
    { set_step(CONT_NEXT, (yyvsp[(2) - (2)].val).v); }
    break;

  case 42:

/* Line 1455 of yacc.c  */
#line 275 "inform.y"
    { set_step(CONT_UNTIL, 1); }
    break;

  case 43:

/* Line 1455 of yacc.c  */
#line 277 "inform.y"
    { set_step(CONT_STEPI, 1); }
    break;

  case 44:

/* Line 1455 of yacc.c  */
#line 279 "inform.y"
    { set_step(CONT_STEPI, (yyvsp[(2) - (2)].val).v); }
    break;

  case 45:

/* Line 1455 of yacc.c  */
#line 281 "inform.y"
    { set_step(CONT_NEXTI, 1); }
    break;

  case 46:

/* Line 1455 of yacc.c  */
#line 283 "inform.y"
    { set_step(CONT_NEXTI, (yyvsp[(2) - (2)].val).v); }
    break;

  case 47:

/* Line 1455 of yacc.c  */
#line 285 "inform.y"
    { set_step(CONT_FINISH, 1); }
    break;

  case 48:

/* Line 1455 of yacc.c  */
#line 287 "inform.y"
    { infix_set_break((yyvsp[(2) - (2)].pcoffset));	}
    break;

  case 49:

/* Line 1455 of yacc.c  */
#line 289 "inform.y"
    { int n = infix_set_break((yyvsp[(2) - (3)].pcoffset)); infix_set_cond(n, lex_tail()); }
    break;

  case 50:

/* Line 1455 of yacc.c  */
#line 291 "inform.y"
    { infix_set_cond((yyvsp[(2) - (2)].val).v, lex_tail()); }
    break;

  case 51:

/* Line 1455 of yacc.c  */
#line 293 "inform.y"
    { infix_set_ignore((yyvsp[(2) - (3)].val).v, (yyvsp[(3) - (3)].val).v);	}
    break;

  case 52:

/* Line 1455 of yacc.c  */
#line 295 "inform.y"
    { infix_delete_breakpoint((yyvsp[(2) - (2)].val).v); }
    break;

  case 53:

/* Line 1455 of yacc.c  */
#line 297 "inform.y"
    { infix_show_all_breakpoints(); }
    break;

  case 54:

/* Line 1455 of yacc.c  */
#line 299 "inform.y"
    { infix_show_breakpoint((yyvsp[(2) - (2)].val).v);	}
    break;

  case 55:

/* Line 1455 of yacc.c  */
#line 301 "inform.y"
    { infix_set_break_enabled((yyvsp[(2) - (2)].val).v, FALSE); }
    break;

  case 56:

/* Line 1455 of yacc.c  */
#line 303 "inform.y"
    { infix_set_break_enabled((yyvsp[(2) - (2)].val).v, TRUE); }
    break;

  case 57:

/* Line 1455 of yacc.c  */
#line 305 "inform.y"
    { infix_print_string("The current source language is \"inform\".\n"); }
    break;

  case 58:

/* Line 1455 of yacc.c  */
#line 307 "inform.y"
    { infix_print_string("Current source file is "); infix_print_string(cur_file?cur_file->filename:"unknown"); infix_print_string("\nContains "); infix_print_number(cur_file?cur_file->num_lines:0); infix_print_string(" lines.\nSource language is inform.\n"); }
    break;

  case 59:

/* Line 1455 of yacc.c  */
#line 309 "inform.y"
    { infix_print_string("Source files for which symbols have been read in:\n\n"); infix_list_files(); infix_print_char('\n'); }
    break;

  case 60:

/* Line 1455 of yacc.c  */
#line 311 "inform.y"
    { show_copying(); }
    break;

  case 61:

/* Line 1455 of yacc.c  */
#line 313 "inform.y"
    { show_warranty(); }
    break;

  case 62:

/* Line 1455 of yacc.c  */
#line 315 "inform.y"
    { infix_show_frame(infix_selected_frame); }
    break;

  case 63:

/* Line 1455 of yacc.c  */
#line 317 "inform.y"
    { infix_select_frame((yyvsp[(2) - (2)].val).v); infix_show_frame((yyvsp[(2) - (2)].val).v); }
    break;

  case 64:

/* Line 1455 of yacc.c  */
#line 319 "inform.y"
    { infix_select_frame((yyvsp[(2) - (2)].val).v); }
    break;

  case 65:

/* Line 1455 of yacc.c  */
#line 321 "inform.y"
    { infix_select_frame(infix_selected_frame - 1); infix_show_frame(infix_selected_frame); }
    break;

  case 66:

/* Line 1455 of yacc.c  */
#line 323 "inform.y"
    { infix_select_frame(infix_selected_frame - (yyvsp[(2) - (2)].val).v); infix_show_frame(infix_selected_frame); }
    break;

  case 67:

/* Line 1455 of yacc.c  */
#line 325 "inform.y"
    { infix_select_frame(infix_selected_frame - 1); }
    break;

  case 68:

/* Line 1455 of yacc.c  */
#line 327 "inform.y"
    { infix_select_frame(infix_selected_frame - (yyvsp[(2) - (2)].val).v); }
    break;

  case 69:

/* Line 1455 of yacc.c  */
#line 329 "inform.y"
    { infix_select_frame(infix_selected_frame + 1); infix_show_frame(infix_selected_frame); }
    break;

  case 70:

/* Line 1455 of yacc.c  */
#line 331 "inform.y"
    { infix_select_frame(infix_selected_frame + (yyvsp[(2) - (2)].val).v); infix_show_frame(infix_selected_frame); }
    break;

  case 71:

/* Line 1455 of yacc.c  */
#line 333 "inform.y"
    { infix_select_frame(infix_selected_frame + 1); }
    break;

  case 72:

/* Line 1455 of yacc.c  */
#line 335 "inform.y"
    { infix_select_frame(infix_selected_frame + (yyvsp[(2) - (2)].val).v); }
    break;

  case 73:

/* Line 1455 of yacc.c  */
#line 337 "inform.y"
    { infix_backtrace(0, stack_get_depth()); }
    break;

  case 74:

/* Line 1455 of yacc.c  */
#line 339 "inform.y"
    { infix_backtrace(stack_get_depth() - (yyvsp[(2) - (2)].val).v, (yyvsp[(2) - (2)].val).v); }
    break;

  case 75:

/* Line 1455 of yacc.c  */
#line 341 "inform.y"
    { infix_backtrace(0, (yyvsp[(3) - (3)].val).v); }
    break;

  case 76:

/* Line 1455 of yacc.c  */
#line 349 "inform.y"
    { if((yyvsp[(1) - (1)].val).t == Z_ROUTINE) (yyval.pcoffset) = infix_get_routine_PC((yyvsp[(1) - (1)].val).v); else { infix_location l; infix_decode_fileloc(&l, cur_file?cur_file->filename:"", (yyvsp[(1) - (1)].val).v); (yyval.pcoffset) = l.thisPC; } }
    break;

  case 77:

/* Line 1455 of yacc.c  */
#line 350 "inform.y"
    { infix_location l; infix_decode_fileloc(&l, cur_file?cur_file->filename:"", cur_line + (yyvsp[(2) - (2)].val).v); (yyval.pcoffset) = l.thisPC; }
    break;

  case 78:

/* Line 1455 of yacc.c  */
#line 351 "inform.y"
    { infix_location l; infix_decode_fileloc(&l, cur_file?cur_file->filename:"", cur_line - (yyvsp[(2) - (2)].val).v); (yyval.pcoffset) = l.thisPC; }
    break;

  case 79:

/* Line 1455 of yacc.c  */
#line 352 "inform.y"
    { if((yyvsp[(3) - (3)].val).t == Z_ROUTINE) (yyval.pcoffset) = UNPACKR((yyvsp[(3) - (3)].val).v); else { infix_location l; infix_decode_fileloc(&l, (yyvsp[(1) - (3)].filenum)->filename, (yyvsp[(3) - (3)].val).v); (yyval.pcoffset) = l.thisPC; } }
    break;

  case 80:

/* Line 1455 of yacc.c  */
#line 353 "inform.y"
    { (yyval.pcoffset) = (yyvsp[(2) - (2)].val).v;			}
    break;

  case 81:

/* Line 1455 of yacc.c  */
#line 357 "inform.y"
    {
		if(condlist->condfunc(condlist->val, (yyvsp[(1) - (1)].val).v) ^ condlist->opposite) {
		   (yyval.flag) = TRUE;
		   ignoreeffects++;
		} else
		   (yyval.flag) = FALSE;
	    }
    break;

  case 82:

/* Line 1455 of yacc.c  */
#line 364 "inform.y"
    {
		if((yyvsp[(1) - (3)].flag))
		  (yyval.flag) = TRUE;
		else {
		  if(condlist->condfunc(condlist->val, (yyvsp[(3) - (3)].val).v) ^ condlist->opposite) {
		    (yyval.flag) = TRUE;
		    ignoreeffects++;
		  }
		  else (yyval.flag) = FALSE;
		} }
    break;

  case 83:

/* Line 1455 of yacc.c  */
#line 377 "inform.y"
    { (yyval.zlist) = NULL; }
    break;

  case 84:

/* Line 1455 of yacc.c  */
#line 378 "inform.y"
    { zword_list g; (yyval.zlist) = (yyvsp[(3) - (3)].zlist); g.item = (yyvsp[(1) - (3)].val).v; LEaddm((yyval.zlist), g, n_rmmalloc); }
    break;

  case 87:

/* Line 1455 of yacc.c  */
#line 384 "inform.y"
    { (yyval.val) = (yyvsp[(3) - (3)].val);			}
    break;

  case 88:

/* Line 1455 of yacc.c  */
#line 385 "inform.y"
    { (yyval.val) = (yyvsp[(3) - (3)].val);			}
    break;

  case 89:

/* Line 1455 of yacc.c  */
#line 390 "inform.y"
    { cond_list newcond; newcond.val = (yyvsp[(1) - (2)].val).v; newcond.condfunc = (yyvsp[(2) - (2)].cond).condfunc; newcond.opposite = (yyvsp[(2) - (2)].cond).opposite; LEaddm(condlist, newcond, n_rmmalloc); }
    break;

  case 90:

/* Line 1455 of yacc.c  */
#line 390 "inform.y"
    { if((yyvsp[(4) - (4)].flag)) ignoreeffects--; (yyval.val).v = (yyvsp[(4) - (4)].flag); (yyval.val).t = Z_BOOLEAN; LEremovem(condlist, n_rmfreeone); }
    break;

  case 91:

/* Line 1455 of yacc.c  */
#line 395 "inform.y"
    { (yyval.val) = (yyvsp[(1) - (1)].val);				}
    break;

  case 92:

/* Line 1455 of yacc.c  */
#line 397 "inform.y"
    { (yyval.val).v = 0; (yyval.val).t = Z_BOOLEAN;		}
    break;

  case 93:

/* Line 1455 of yacc.c  */
#line 399 "inform.y"
    { (yyval.val).v = 1; (yyval.val).t = Z_BOOLEAN;		}
    break;

  case 94:

/* Line 1455 of yacc.c  */
#line 401 "inform.y"
    { (yyval.val).v = 0; (yyval.val).t = Z_OBJECT;		}
    break;

  case 95:

/* Line 1455 of yacc.c  */
#line 404 "inform.y"
    { (yyval.val) = (yyvsp[(3) - (3)].val); infix_assign(&(yyvsp[(1) - (3)].val), (yyvsp[(3) - (3)].val).v);	}
    break;

  case 96:

/* Line 1455 of yacc.c  */
#line 407 "inform.y"
    { (yyval.val).v = infix_parent((yyvsp[(3) - (4)].val).v); (yyval.val).t = Z_OBJECT; }
    break;

  case 97:

/* Line 1455 of yacc.c  */
#line 409 "inform.y"
    { (yyval.val).v = infix_child((yyvsp[(3) - (4)].val).v); (yyval.val).t = Z_OBJECT; }
    break;

  case 98:

/* Line 1455 of yacc.c  */
#line 411 "inform.y"
    { (yyval.val).v = infix_sibling((yyvsp[(3) - (4)].val).v); (yyval.val).t = Z_OBJECT; }
    break;

  case 99:

/* Line 1455 of yacc.c  */
#line 413 "inform.y"
    { int n = 0; zword o = infix_child((yyvsp[(3) - (4)].val).v); while(o) { n++; o = infix_sibling(o); } (yyval.val).v = n; (yyval.val).t = Z_NUMBER; }
    break;

  case 100:

/* Line 1455 of yacc.c  */
#line 416 "inform.y"
    {
		  if(!ignoreeffects) {
		    (yyval.val).v = z_random((yyvsp[(3) - (4)].val).v);
		    (yyval.val).t = Z_NUMBER;
		  } else {
		    (yyval.val).v = 0;
		    (yyval.val).t = Z_UNKNOWN;
		  }
		}
    break;

  case 101:

/* Line 1455 of yacc.c  */
#line 426 "inform.y"
    {
		zword locals[16];
		int i = 0;
		zword_list *p;
		if(!ignoreeffects) {
		  for(p = (yyvsp[(3) - (4)].zlist); p && i < 16; p=p->next) {
		    locals[i++] = p->item;
		  }
		  mop_call((yyvsp[(1) - (4)].val).v, i, locals, -2);
		  decode();
		  exit_decoder = FALSE;
		  (yyval.val).v = time_ret; (yyval.val).t = Z_UNKNOWN;
		} else {
		  (yyval.val).v = 0; (yyval.val).t = Z_UNKNOWN;
		}
	      }
    break;

  case 102:

/* Line 1455 of yacc.c  */
#line 443 "inform.y"
    { if((yyvsp[(1) - (2)].val).v == 0) ignoreeffects++; }
    break;

  case 103:

/* Line 1455 of yacc.c  */
#line 444 "inform.y"
    { if((yyvsp[(1) - (4)].val).v == 0) ignoreeffects--; (yyval.val) = z_t((yyvsp[(1) - (4)].val), (yyvsp[(4) - (4)].val), (yyvsp[(1) - (4)].val).v && (yyvsp[(4) - (4)].val).v);	}
    break;

  case 104:

/* Line 1455 of yacc.c  */
#line 445 "inform.y"
    { if((yyvsp[(1) - (2)].val).v != 0) ignoreeffects++; }
    break;

  case 105:

/* Line 1455 of yacc.c  */
#line 446 "inform.y"
    { if((yyvsp[(1) - (4)].val).v != 0) ignoreeffects--; (yyval.val) = z_t((yyvsp[(1) - (4)].val), (yyvsp[(4) - (4)].val), (yyvsp[(1) - (4)].val).v || (yyvsp[(4) - (4)].val).v);	}
    break;

  case 106:

/* Line 1455 of yacc.c  */
#line 448 "inform.y"
    { (yyval.val).v = !((yyvsp[(2) - (2)].val).v); (yyval.val).t = Z_NUMBER;	}
    break;

  case 107:

/* Line 1455 of yacc.c  */
#line 451 "inform.y"
    { (yyval.val) = z_t((yyvsp[(1) - (3)].val), (yyvsp[(3) - (3)].val), (yyvsp[(1) - (3)].val).v + (yyvsp[(3) - (3)].val).v);	}
    break;

  case 108:

/* Line 1455 of yacc.c  */
#line 453 "inform.y"
    { (yyval.val) = z_t((yyvsp[(1) - (3)].val), (yyvsp[(3) - (3)].val), (yyvsp[(1) - (3)].val).v + neg((yyvsp[(3) - (3)].val).v));	}
    break;

  case 109:

/* Line 1455 of yacc.c  */
#line 455 "inform.y"
    { (yyval.val) = z_t((yyvsp[(1) - (3)].val), (yyvsp[(3) - (3)].val), z_mult((yyvsp[(1) - (3)].val).v, (yyvsp[(3) - (3)].val).v));	}
    break;

  case 110:

/* Line 1455 of yacc.c  */
#line 457 "inform.y"
    { (yyval.val) = z_t((yyvsp[(1) - (3)].val), (yyvsp[(3) - (3)].val), z_div((yyvsp[(1) - (3)].val).v, (yyvsp[(3) - (3)].val).v));	}
    break;

  case 111:

/* Line 1455 of yacc.c  */
#line 459 "inform.y"
    { (yyval.val) = z_t((yyvsp[(1) - (3)].val), (yyvsp[(3) - (3)].val), z_mod((yyvsp[(1) - (3)].val).v, (yyvsp[(3) - (3)].val).v));	}
    break;

  case 112:

/* Line 1455 of yacc.c  */
#line 461 "inform.y"
    { (yyval.val) = z_t((yyvsp[(1) - (3)].val), (yyvsp[(3) - (3)].val), (yyvsp[(1) - (3)].val).v & (yyvsp[(3) - (3)].val).v);	}
    break;

  case 113:

/* Line 1455 of yacc.c  */
#line 463 "inform.y"
    { (yyval.val) = z_t((yyvsp[(1) - (3)].val), (yyvsp[(3) - (3)].val), (yyvsp[(1) - (3)].val).v | (yyvsp[(3) - (3)].val).v);	}
    break;

  case 114:

/* Line 1455 of yacc.c  */
#line 465 "inform.y"
    { (yyval.val) = z_t((yyvsp[(2) - (2)].val), (yyvsp[(2) - (2)].val), ~(yyvsp[(2) - (2)].val).v);		}
    break;

  case 115:

/* Line 1455 of yacc.c  */
#line 468 "inform.y"
    { (yyval.val).t = Z_BYTEARRAY; (yyval.val).o = (yyvsp[(1) - (3)].val).v; (yyval.val).p = (yyvsp[(3) - (3)].val).v; infix_get_val(&(yyval.val)); }
    break;

  case 116:

/* Line 1455 of yacc.c  */
#line 470 "inform.y"
    { (yyval.val).t = Z_WORDARRAY; (yyval.val).o = (yyvsp[(1) - (3)].val).v; (yyval.val).p = (yyvsp[(3) - (3)].val).v; infix_get_val(&(yyval.val));	}
    break;

  case 117:

/* Line 1455 of yacc.c  */
#line 473 "inform.y"
    { (yyval.val) = z_t((yyvsp[(2) - (2)].val), (yyvsp[(2) - (2)].val), neg((yyvsp[(2) - (2)].val).v));		}
    break;

  case 118:

/* Line 1455 of yacc.c  */
#line 476 "inform.y"
    { if(!ignoreeffects) infix_assign(&(yyvsp[(2) - (2)].val), ARITHMASK((yyvsp[(2) - (2)].val).v + 1)); (yyval.val) = (yyvsp[(2) - (2)].val); }
    break;

  case 119:

/* Line 1455 of yacc.c  */
#line 478 "inform.y"
    { (yyval.val) = (yyvsp[(1) - (2)].val); if(!ignoreeffects) infix_assign(&(yyvsp[(1) - (2)].val), ARITHMASK((yyvsp[(1) - (2)].val).v + 1)); }
    break;

  case 120:

/* Line 1455 of yacc.c  */
#line 480 "inform.y"
    { if(!ignoreeffects) infix_assign(&(yyvsp[(2) - (2)].val), ARITHMASK((yyvsp[(2) - (2)].val).v + neg(1))); (yyval.val) = (yyvsp[(2) - (2)].val); }
    break;

  case 121:

/* Line 1455 of yacc.c  */
#line 482 "inform.y"
    { (yyval.val) = (yyvsp[(1) - (2)].val); if(!ignoreeffects) infix_assign(&(yyvsp[(1) - (2)].val), ARITHMASK((yyvsp[(1) - (2)].val).v + neg(1))); }
    break;

  case 122:

/* Line 1455 of yacc.c  */
#line 485 "inform.y"
    { zword len; (yyval.val).v = infix_get_proptable((yyvsp[(1) - (3)].val).v, (yyvsp[(3) - (3)].val).v, &len); (yyval.val).t = Z_NUMBER; }
    break;

  case 123:

/* Line 1455 of yacc.c  */
#line 487 "inform.y"
    { infix_get_proptable((yyvsp[(1) - (3)].val).v, (yyvsp[(3) - (3)].val).v, &(yyval.val).v); (yyval.val).t = Z_NUMBER; }
    break;

  case 124:

/* Line 1455 of yacc.c  */
#line 490 "inform.y"
    { (yyval.val).t = Z_OBJPROP; (yyval.val).o = (yyvsp[(1) - (3)].val).v; (yyval.val).p = (yyvsp[(3) - (3)].val).v; infix_get_val(&(yyval.val)); }
    break;

  case 125:

/* Line 1455 of yacc.c  */
#line 498 "inform.y"
    { (yyval.val).v = (yyvsp[(2) - (2)].val).v; (yyval.val).t = Z_NUMBER;		}
    break;

  case 126:

/* Line 1455 of yacc.c  */
#line 500 "inform.y"
    { (yyval.val).v = (yyvsp[(2) - (2)].val).v; (yyval.val).t = Z_OBJECT;		}
    break;

  case 127:

/* Line 1455 of yacc.c  */
#line 502 "inform.y"
    { (yyval.val).v = (yyvsp[(2) - (2)].val).v; (yyval.val).t = Z_ROUTINE;	}
    break;

  case 128:

/* Line 1455 of yacc.c  */
#line 504 "inform.y"
    { (yyval.val).v = (yyvsp[(2) - (2)].val).v; (yyval.val).t = Z_STRING;		}
    break;

  case 129:

/* Line 1455 of yacc.c  */
#line 506 "inform.y"
    { (yyval.val).t = Z_WORDARRAY; (yyval.val).o = z_globaltable; (yyval.val).p = (yyvsp[(2) - (2)].val).v; infix_get_val(&(yyval.val)); }
    break;

  case 130:

/* Line 1455 of yacc.c  */
#line 508 "inform.y"
    { (yyval.val).t = Z_LOCAL; (yyval.val).o = infix_selected_frame; (yyval.val).p = (yyvsp[(2) - (2)].val).v; infix_get_val(&(yyval.val)); }
    break;

  case 131:

/* Line 1455 of yacc.c  */
#line 510 "inform.y"
    { (yyval.val) = (yyvsp[(2) - (3)].val);				}
    break;



/* Line 1455 of yacc.c  */
#line 2823 "y.tab.c"
      default: break;
    }
  YY_SYMBOL_PRINT ("-> $$ =", yyr1[yyn], &yyval, &yyloc);

  YYPOPSTACK (yylen);
  yylen = 0;
  YY_STACK_PRINT (yyss, yyssp);

  *++yyvsp = yyval;

  /* Now `shift' the result of the reduction.  Determine what state
     that goes to, based on the state we popped back to and the rule
     number reduced by.  */

  yyn = yyr1[yyn];

  yystate = yypgoto[yyn - YYNTOKENS] + *yyssp;
  if (0 <= yystate && yystate <= YYLAST && yycheck[yystate] == *yyssp)
    yystate = yytable[yystate];
  else
    yystate = yydefgoto[yyn - YYNTOKENS];

  goto yynewstate;


/*------------------------------------.
| yyerrlab -- here on detecting error |
`------------------------------------*/
yyerrlab:
  /* If not already recovering from an error, report this error.  */
  if (!yyerrstatus)
    {
      ++yynerrs;
#if ! YYERROR_VERBOSE
      yyerror (YY_("syntax error"));
#else
      {
	YYSIZE_T yysize = yysyntax_error (0, yystate, yychar);
	if (yymsg_alloc < yysize && yymsg_alloc < YYSTACK_ALLOC_MAXIMUM)
	  {
	    YYSIZE_T yyalloc = 2 * yysize;
	    if (! (yysize <= yyalloc && yyalloc <= YYSTACK_ALLOC_MAXIMUM))
	      yyalloc = YYSTACK_ALLOC_MAXIMUM;
	    if (yymsg != yymsgbuf)
	      YYSTACK_FREE (yymsg);
	    yymsg = (char *) YYSTACK_ALLOC (yyalloc);
	    if (yymsg)
	      yymsg_alloc = yyalloc;
	    else
	      {
		yymsg = yymsgbuf;
		yymsg_alloc = sizeof yymsgbuf;
	      }
	  }

	if (0 < yysize && yysize <= yymsg_alloc)
	  {
	    (void) yysyntax_error (yymsg, yystate, yychar);
	    yyerror (yymsg);
	  }
	else
	  {
	    yyerror (YY_("syntax error"));
	    if (yysize != 0)
	      goto yyexhaustedlab;
	  }
      }
#endif
    }



  if (yyerrstatus == 3)
    {
      /* If just tried and failed to reuse lookahead token after an
	 error, discard it.  */

      if (yychar <= YYEOF)
	{
	  /* Return failure if at end of input.  */
	  if (yychar == YYEOF)
	    YYABORT;
	}
      else
	{
	  yydestruct ("Error: discarding",
		      yytoken, &yylval);
	  yychar = YYEMPTY;
	}
    }

  /* Else will try to reuse lookahead token after shifting the error
     token.  */
  goto yyerrlab1;


/*---------------------------------------------------.
| yyerrorlab -- error raised explicitly by YYERROR.  |
`---------------------------------------------------*/
yyerrorlab:

  /* Pacify compilers like GCC when the user code never invokes
     YYERROR and the label yyerrorlab therefore never appears in user
     code.  */
  if (/*CONSTCOND*/ 0)
     goto yyerrorlab;

  /* Do not reclaim the symbols of the rule which action triggered
     this YYERROR.  */
  YYPOPSTACK (yylen);
  yylen = 0;
  YY_STACK_PRINT (yyss, yyssp);
  yystate = *yyssp;
  goto yyerrlab1;


/*-------------------------------------------------------------.
| yyerrlab1 -- common code for both syntax error and YYERROR.  |
`-------------------------------------------------------------*/
yyerrlab1:
  yyerrstatus = 3;	/* Each real token shifted decrements this.  */

  for (;;)
    {
      yyn = yypact[yystate];
      if (yyn != YYPACT_NINF)
	{
	  yyn += YYTERROR;
	  if (0 <= yyn && yyn <= YYLAST && yycheck[yyn] == YYTERROR)
	    {
	      yyn = yytable[yyn];
	      if (0 < yyn)
		break;
	    }
	}

      /* Pop the current state because it cannot handle the error token.  */
      if (yyssp == yyss)
	YYABORT;


      yydestruct ("Error: popping",
		  yystos[yystate], yyvsp);
      YYPOPSTACK (1);
      yystate = *yyssp;
      YY_STACK_PRINT (yyss, yyssp);
    }

  *++yyvsp = yylval;


  /* Shift the error token.  */
  YY_SYMBOL_PRINT ("Shifting", yystos[yyn], yyvsp, yylsp);

  yystate = yyn;
  goto yynewstate;


/*-------------------------------------.
| yyacceptlab -- YYACCEPT comes here.  |
`-------------------------------------*/
yyacceptlab:
  yyresult = 0;
  goto yyreturn;

/*-----------------------------------.
| yyabortlab -- YYABORT comes here.  |
`-----------------------------------*/
yyabortlab:
  yyresult = 1;
  goto yyreturn;

#if !defined(yyoverflow) || YYERROR_VERBOSE
/*-------------------------------------------------.
| yyexhaustedlab -- memory exhaustion comes here.  |
`-------------------------------------------------*/
yyexhaustedlab:
  yyerror (YY_("memory exhausted"));
  yyresult = 2;
  /* Fall through.  */
#endif

yyreturn:
  if (yychar != YYEMPTY)
     yydestruct ("Cleanup: discarding lookahead",
		 yytoken, &yylval);
  /* Do not reclaim the symbols of the rule which action triggered
     this YYABORT or YYACCEPT.  */
  YYPOPSTACK (yylen);
  YY_STACK_PRINT (yyss, yyssp);
  while (yyssp != yyss)
    {
      yydestruct ("Cleanup: popping",
		  yystos[*yyssp], yyvsp);
      YYPOPSTACK (1);
    }
#ifndef yyoverflow
  if (yyss != yyssa)
    YYSTACK_FREE (yyss);
#endif
#if YYERROR_VERBOSE
  if (yymsg != yymsgbuf)
    YYSTACK_FREE (yymsg);
#endif
  /* Make sure YYID is used.  */
  return YYID (yyresult);
}



/* Line 1675 of yacc.c  */
#line 514 "inform.y"


#if 0
{ /* fanagling to get emacs indentation sane */
int foo;
#endif

static z_typed z_t(z_typed a, z_typed b, zword v)
{
  z_typed r;
  r.v = ARITHMASK(v);
  if(a.t == Z_NUMBER && b.t == Z_NUMBER)
    r.t = Z_NUMBER;
  else
    r.t = Z_UNKNOWN;
  return r;
}



typedef struct {
  int token;
  const char *name;
} name_token;

static name_token infix_operators[] = {
  { ANDAND,     "&&" },
  { OROR,       "||" },
  { NOTNOT,     "~~" },
  { BYTEARRAY,  "->" },
  { WORDARRAY,  "-->" },
  { NUMBER,     "(number)" },
  { OBJECT,     "(object)" },
  { ROUTINE,    "(routine)" },
  { STRING,     "(string)" },
  { GLOBAL,     "(global)" },
  { LOCAL,      "(local)" },
  { INCREMENT,  "++" },
  { DECREMENT,  "--" },
  { SUPERCLASS, "::" }
};


static name_token infix_keywords[] = {
  { TO,         "to" },
  { IF,         "if" },
  { OR,         "or" },
  { BTRUE,      "true" },
  { BFALSE,     "false" },
  { NOTHING,    "nothing" },
  { PARENT,     "parent" },
  { CHILD,      "child" },
  { SIBLING,    "sibling" },
  { RANDOM,     "random" },
  { CHILDREN,   "children" }
};


/* These are only valid as the first token in an expression.  A single space
   matches at least one typed whitespace character */
static name_token infix_commands[] = {
  { '#',          "#" },
  { HELP,         "help" },
  { ALIAS,        "alias" },
  { RALIAS,       "ralias" },
  { UNALIAS,      "unalias" },
  { DUMPMEM,      "dumpmem" },
  { AUTOMAP,      "automap" },
  { UNDO,         "undo" },
  { REDO,         "redo" },
  { QUIT,         "quit" },
  { RESTORE,      "restore" },
  { RESTART,      "restart" },
  { RESTART,      "run" },
  { RECORDON,	  "recording on" },
  { RECORDOFF,    "recording off" },
  { REPLAY,       "replay" },
  { REPLAYOFF,    "replay off" },
  { SYMBOL_FILE,  "symbol-file" },
  { PRINT,        "print" },
  { PRINT,        "p" },
  { PRINT,        "call" },  /* No void functions in inform */
  { SET,          "set" },
  { MOVE,         "move" },
  { OBJECT_TREE,  "object-tree" },
  { OBJECT_TREE,  "tree" },
  { FIND,         "find" },
  { REMOVE,       "remove" },
  { GIVE,         "give" },
  { LIST_GLOBALS, "globals" },
  { JUMP,         "jump" },
  { CONT,         "continue" },
  { CONT,         "c" },
  { CONT,         "fg" },
  { STEP,         "step" },
  { STEP,         "s" },
  { NEXT,         "next" },
  { NEXT,         "n" },
  { STEPI,        "stepi" },
  { STEPI,        "si" },
  { NEXTI,        "nexti" },
  { NEXTI,        "ni" },
  { UNTIL,        "until" },
  { UNTIL,        "u" },
  { FINISH,       "finish" },
  { BREAK,        "break" },
  { DELETE,       "delete" },
  { DELETE,       "d" },
  { DELETE,       "delete breakpoints" },
  { COND,         "condition" },
  { IGNORE,       "ignore" },
  { FRAME,        "frame" },
  { FRAME,        "f" },
  { SELECT_FRAME, "select-frame" },
  { UP_FRAME,     "up" },
  { DOWN_FRAME,   "down" },
  { DOWN_FRAME,   "do" },
  { UP_SILENTLY,  "up-silently" },
  { DOWN_SILENTLY,"down-silently" },
  { BREAKPOINTS,  "info breakpoints" },
  { BREAKPOINTS,  "info watchpoints" },
  { BREAKPOINTS,  "info break" },
  { DISABLE_BREAK,"disable" },
  { DISABLE_BREAK,"disable breakpoints" },
  { DISABLE_BREAK,"dis" },
  { DISABLE_BREAK,"dis breakpoints" },
  { ENABLE_BREAK, "enable" },
  { ENABLE_BREAK, "enable breakpoints" },
  { LANGUAGE,     "show language" },
  { INFOSOURCE,   "info source" },
  { INFOSOURCES,  "info sources" },
  { COPYING,      "show copying" },
  { WARRANTY,     "show warranty" },
  { BACKTRACE,    "backtrace" },
  { BACKTRACE,    "bt" },
  { BACKTRACE,    "where" },
  { BACKTRACE,    "info stack" },
  { BACKTRACE,    "info s" },
  { DISPLAY,      "display" },
  { UNDISPLAY,    "undisplay" },
  { UNDISPLAY,    "delete display" },
  { DISABLE_DISPLAY,"disable display" },
  { DISABLE_DISPLAY,"dis display" },
  { ENABLE_DISPLAY,"enable display" }
};

#include "dbg_help.h"

static BOOL z_isequal(zword a, zword b)
{
  return (a == b);
}

static BOOL z_isgreat(zword a, zword b)
{
  return is_greaterthan(a, b);
}

static BOOL z_isless(zword a, zword b)
{
  return is_lessthan(a, b);
}

static BOOL infix_provides(zword o, zword p)
{
  zword len;
  return (infix_get_proptable(o, p, &len) != 0);
}

static BOOL infix_in(zword a, zword b)
{
  return infix_parent(a) == b;
}

typedef struct {
  const char *name;
  BOOL (*condfunc)(zword a, zword b);
  BOOL opposite;
} condition;

condition conditionlist[] = {
  { "==",      z_isequal,         FALSE },
  { "~=",      z_isequal,         TRUE },
  { ">",       z_isgreat,         FALSE },
  { "<",       z_isless,          FALSE },
  { "<=",      z_isgreat,         TRUE },
  { ">=",      z_isless,          TRUE },
  { "has",     infix_test_attrib, FALSE },
  { "hasnt",   infix_test_attrib, TRUE },
  { "in",      infix_in,          FALSE },
  { "notin",   infix_in,          TRUE },
/*{ "ofclass", infix_ofclass,     FALSE },*/
  { "provides",infix_provides,    FALSE }
};


static BOOL is_command_identifier(char c)
{
  return isalpha(c) || (c == '-');
}

static BOOL is_identifier(char c)
{
  return isalpha(c) || isdigit(c) || (c == '_');
}

static BOOL is_longer_identifier(char c)
{
  return isalpha(c) || isdigit(c) || (c == '_') || (c == '.') || (c == ':');
}

static int grab_number(z_typed *val)
{
  int len = 0;
  char *endptr;
  char c = lex_expression[lex_offset + len];
  int base = 10;
  long int num;

  /* Don't handle negativity here */
  if(c == '-' || c == '+')
    return 0;
  
  if(c == '$') {
    len++;
    base = 16;
    c = lex_expression[lex_offset + len];
    if(c == '$') {
      len++;
      base = 2;
      c = lex_expression[lex_offset + len];
    }
  }
  
  num = n_strtol(lex_expression + lex_offset + len, &endptr, base);

  if(endptr != lex_expression + lex_offset) {
    len += endptr - lex_expression - lex_offset;
    val->v = num;
    val->t = Z_NUMBER;
    return len;
  }
  return 0;
}


typedef enum { match_None, match_Partial, match_Complete } match_type;

static match_type command_matches(const char *command, const char *expression,
				  unsigned *matchedlen)
{
  unsigned c, e;
  e = 0;

  for(c = 0; command[c]; c++) {
    if(command[c] != expression[e]) {
      if(!is_command_identifier(expression[e])) {
	*matchedlen = e;
	return match_Partial;
      }
      return match_None;
    }

    e++;
    
    if(command[c] == ' ') {
      while(expression[e] == ' ')
	e++;
    }
  }

  if(!is_command_identifier(expression[e])) {
    *matchedlen = e;
    return match_Complete; 
  }

  return match_None;
}


static int grab_command(void)
{
  unsigned i;
  unsigned len;

  unsigned best;
  match_type best_match = match_None;
  unsigned best_len = 0;
  BOOL found = FALSE;
  BOOL ambig = FALSE;

  while(isspace(lex_expression[lex_offset]))
    lex_offset++;

  for(i = 0; i < sizeof(infix_commands) / sizeof(*infix_commands); i++) {
    switch(command_matches(infix_commands[i].name, lex_expression + lex_offset, &len)) {
    case match_Complete:
      if(len > best_len || best_match != match_Complete) {
	best = i;
	best_match = match_Complete;
	best_len = len;
	found = TRUE;
      }
      break;

    case match_Partial:
      if(best_match != match_Complete) {
	if(found)
	  ambig = TRUE;
	best = i;
	best_match = match_Partial;
	best_len = len;
	found = TRUE;
      }

    case match_None:
      ;
    }
  }

  if(ambig && best_match != match_Complete) {
    infix_print_string("Ambiguous command.\n");
    return 0;
  }

  if(found) {
    lex_offset += best_len;
    return infix_commands[best].token;
  }

  infix_print_string("Undefined command.\n");
  return 0;
}


static void inform_help(void)
{
  int command;
  unsigned i;
  BOOL is_command = FALSE;
  
  for(i = lex_offset; lex_expression[i]; i++)
    if(!isspace(lex_expression[i]))
      is_command = TRUE;

  if(!is_command) {
    infix_print_string("Help is available on the following commands:\n");
    for(i = 0; i < sizeof(command_help) / sizeof(*command_help); i++) {
      unsigned j;
      for(j = 0; j < sizeof(infix_commands) / sizeof(*infix_commands); j++)
	if(command_help[i].token == infix_commands[j].token) {
	  infix_print_char('\'');
	  infix_print_string(infix_commands[j].name);
	  infix_print_char('\'');
	  break;
	}
      infix_print_char(' ');
    }
    infix_print_string("\n");
    return;
  }
  
  command = grab_command();
  if(command) {
    for(i = 0; i < sizeof(command_help) / sizeof(*command_help); i++) {
      if(command_help[i].token == command) {
	infix_print_string(command_help[i].name);
	infix_print_char(10);
	return;
      }
    }
    infix_print_string("No help available for that command.\n");
  }
}


void process_debug_command(const char *buffer)
{
#ifdef YYDEBUG
  yydebug = 1;
#endif
  lex_expression = buffer;
  lex_offset = 0;
  ignoreeffects = 0;
  yyparse();
  n_rmfree();
}

BOOL exp_has_locals(const char *exp)
{
  return FALSE;
}

z_typed evaluate_expression(const char *exp, unsigned frame)
{
  unsigned old_frame = infix_selected_frame;
  char *new_exp = (char *) n_malloc(n_strlen(exp) + 5);
  n_strcpy(new_exp, "set ");
  n_strcat(new_exp, exp);

  infix_selected_frame = frame;
  process_debug_command(new_exp);
  infix_selected_frame = old_frame;

  n_free(new_exp);

  return inform_result;
}

static void yyerror(const char *s)
{
  infix_print_string(s);
  infix_print_char(10);
}

static int yylex(void)
{
  unsigned i, len, longer;
  BOOL check_command = FALSE;

  if(lex_offset == 0)
    check_command = TRUE;

  while(isspace(lex_expression[lex_offset]))
    lex_offset++;

  if(check_command) {
    return grab_command();
  }

  if((len = grab_number(&yylval.val)) != 0) {
    lex_offset += len;
    return NUM;
  }

  for(i = 0; i < sizeof(infix_operators) / sizeof(*infix_operators); i++) {
    if(n_strncmp(infix_operators[i].name, lex_expression + lex_offset,
	       n_strlen(infix_operators[i].name)) == 0) {
      lex_offset += n_strlen(infix_operators[i].name);
      return infix_operators[i].token;
    }
  }

  for(i = 0; i < sizeof(conditionlist) / sizeof(*conditionlist); i++) {
    len = n_strlen(conditionlist[i].name);
    if(len
       && n_strncmp(conditionlist[i].name,
		   lex_expression + lex_offset, len) == 0
       && !(is_identifier(conditionlist[i].name[len-1])
	    && is_identifier(lex_expression[lex_offset + len]))) {

      lex_offset += len;
      yylval.cond.condfunc = conditionlist[i].condfunc;
      yylval.cond.opposite = conditionlist[i].opposite;
      return CONDITION;
    }
  }

  if((len = infix_find_file(&yylval.filenum, lex_expression + lex_offset)) != 0) {
    lex_offset += len;
    return DFILE;
  }


  for(len = 0; is_identifier(lex_expression[lex_offset + len]); len++)
    ;

  if(!len)
    return lex_expression[lex_offset++];

  for(i = 0; i < sizeof(infix_keywords) / sizeof(*infix_keywords); i++) {
    if(n_strmatch(infix_keywords[i].name, lex_expression + lex_offset, len)) {
      lex_offset += len;
      return infix_keywords[i].token;
    }
  }

  for(longer = len; is_longer_identifier(lex_expression[lex_offset + longer]); longer++)
    ;

  if(infix_find_symbol(&yylval.val, lex_expression + lex_offset, longer)) {
    lex_offset += longer;
    return NUM;
  }

  if(infix_find_symbol(&yylval.val, lex_expression + lex_offset, len)) {
    lex_offset += len;
    return NUM;
  }

  infix_print_string("Unknown identifier \"");
  for(i = 0; i < len; i++)
    infix_print_char(lex_expression[lex_offset + i]);
  infix_print_string("\"\n");

  return 0;
}

#endif /* DEBUGGING */

