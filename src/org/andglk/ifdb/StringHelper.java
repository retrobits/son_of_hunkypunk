/*
 * Static String formatting and query routines.
 * Copyright (C) 2001-2005 Stephen Ostermiller
 * http://ostermiller.org/contact.pl?regarding=Java+Utilities
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * See COPYING.TXT for details.
 */

package org.andglk.ifdb;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Utilities for String formatting, manipulation, and queries.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/StringHelper.html">ostermiller.org</a>.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
public class StringHelper {
  private static HashMap<String,Integer> htmlEntities = new HashMap<String,Integer>();
  static {
    htmlEntities.put("nbsp", new Integer(160));
    htmlEntities.put("iexcl", new Integer(161));
    htmlEntities.put("cent", new Integer(162));
    htmlEntities.put("pound", new Integer(163));
    htmlEntities.put("curren", new Integer(164));
    htmlEntities.put("yen", new Integer(165));
    htmlEntities.put("brvbar", new Integer(166));
    htmlEntities.put("sect", new Integer(167));
    htmlEntities.put("uml", new Integer(168));
    htmlEntities.put("copy", new Integer(169));
    htmlEntities.put("ordf", new Integer(170));
    htmlEntities.put("laquo", new Integer(171));
    htmlEntities.put("not", new Integer(172));
    htmlEntities.put("shy", new Integer(173));
    htmlEntities.put("reg", new Integer(174));
    htmlEntities.put("macr", new Integer(175));
    htmlEntities.put("deg", new Integer(176));
    htmlEntities.put("plusmn", new Integer(177));
    htmlEntities.put("sup2", new Integer(178));
    htmlEntities.put("sup3", new Integer(179));
    htmlEntities.put("acute", new Integer(180));
    htmlEntities.put("micro", new Integer(181));
    htmlEntities.put("para", new Integer(182));
    htmlEntities.put("middot", new Integer(183));
    htmlEntities.put("cedil", new Integer(184));
    htmlEntities.put("sup1", new Integer(185));
    htmlEntities.put("ordm", new Integer(186));
    htmlEntities.put("raquo", new Integer(187));
    htmlEntities.put("frac14", new Integer(188));
    htmlEntities.put("frac12", new Integer(189));
    htmlEntities.put("frac34", new Integer(190));
    htmlEntities.put("iquest", new Integer(191));
    htmlEntities.put("Agrave", new Integer(192));
    htmlEntities.put("Aacute", new Integer(193));
    htmlEntities.put("Acirc", new Integer(194));
    htmlEntities.put("Atilde", new Integer(195));
    htmlEntities.put("Auml", new Integer(196));
    htmlEntities.put("Aring", new Integer(197));
    htmlEntities.put("AElig", new Integer(198));
    htmlEntities.put("Ccedil", new Integer(199));
    htmlEntities.put("Egrave", new Integer(200));
    htmlEntities.put("Eacute", new Integer(201));
    htmlEntities.put("Ecirc", new Integer(202));
    htmlEntities.put("Euml", new Integer(203));
    htmlEntities.put("Igrave", new Integer(204));
    htmlEntities.put("Iacute", new Integer(205));
    htmlEntities.put("Icirc", new Integer(206));
    htmlEntities.put("Iuml", new Integer(207));
    htmlEntities.put("ETH", new Integer(208));
    htmlEntities.put("Ntilde", new Integer(209));
    htmlEntities.put("Ograve", new Integer(210));
    htmlEntities.put("Oacute", new Integer(211));
    htmlEntities.put("Ocirc", new Integer(212));
    htmlEntities.put("Otilde", new Integer(213));
    htmlEntities.put("Ouml", new Integer(214));
    htmlEntities.put("times", new Integer(215));
    htmlEntities.put("Oslash", new Integer(216));
    htmlEntities.put("Ugrave", new Integer(217));
    htmlEntities.put("Uacute", new Integer(218));
    htmlEntities.put("Ucirc", new Integer(219));
    htmlEntities.put("Uuml", new Integer(220));
    htmlEntities.put("Yacute", new Integer(221));
    htmlEntities.put("THORN", new Integer(222));
    htmlEntities.put("szlig", new Integer(223));
    htmlEntities.put("agrave", new Integer(224));
    htmlEntities.put("aacute", new Integer(225));
    htmlEntities.put("acirc", new Integer(226));
    htmlEntities.put("atilde", new Integer(227));
    htmlEntities.put("auml", new Integer(228));
    htmlEntities.put("aring", new Integer(229));
    htmlEntities.put("aelig", new Integer(230));
    htmlEntities.put("ccedil", new Integer(231));
    htmlEntities.put("egrave", new Integer(232));
    htmlEntities.put("eacute", new Integer(233));
    htmlEntities.put("ecirc", new Integer(234));
    htmlEntities.put("euml", new Integer(235));
    htmlEntities.put("igrave", new Integer(236));
    htmlEntities.put("iacute", new Integer(237));
    htmlEntities.put("icirc", new Integer(238));
    htmlEntities.put("iuml", new Integer(239));
    htmlEntities.put("eth", new Integer(240));
    htmlEntities.put("ntilde", new Integer(241));
    htmlEntities.put("ograve", new Integer(242));
    htmlEntities.put("oacute", new Integer(243));
    htmlEntities.put("ocirc", new Integer(244));
    htmlEntities.put("otilde", new Integer(245));
    htmlEntities.put("ouml", new Integer(246));
    htmlEntities.put("divide", new Integer(247));
    htmlEntities.put("oslash", new Integer(248));
    htmlEntities.put("ugrave", new Integer(249));
    htmlEntities.put("uacute", new Integer(250));
    htmlEntities.put("ucirc", new Integer(251));
    htmlEntities.put("uuml", new Integer(252));
    htmlEntities.put("yacute", new Integer(253));
    htmlEntities.put("thorn", new Integer(254));
    htmlEntities.put("yuml", new Integer(255));
    htmlEntities.put("fnof", new Integer(402));
    htmlEntities.put("Alpha", new Integer(913));
    htmlEntities.put("Beta", new Integer(914));
    htmlEntities.put("Gamma", new Integer(915));
    htmlEntities.put("Delta", new Integer(916));
    htmlEntities.put("Epsilon", new Integer(917));
    htmlEntities.put("Zeta", new Integer(918));
    htmlEntities.put("Eta", new Integer(919));
    htmlEntities.put("Theta", new Integer(920));
    htmlEntities.put("Iota", new Integer(921));
    htmlEntities.put("Kappa", new Integer(922));
    htmlEntities.put("Lambda", new Integer(923));
    htmlEntities.put("Mu", new Integer(924));
    htmlEntities.put("Nu", new Integer(925));
    htmlEntities.put("Xi", new Integer(926));
    htmlEntities.put("Omicron", new Integer(927));
    htmlEntities.put("Pi", new Integer(928));
    htmlEntities.put("Rho", new Integer(929));
    htmlEntities.put("Sigma", new Integer(931));
    htmlEntities.put("Tau", new Integer(932));
    htmlEntities.put("Upsilon", new Integer(933));
    htmlEntities.put("Phi", new Integer(934));
    htmlEntities.put("Chi", new Integer(935));
    htmlEntities.put("Psi", new Integer(936));
    htmlEntities.put("Omega", new Integer(937));
    htmlEntities.put("alpha", new Integer(945));
    htmlEntities.put("beta", new Integer(946));
    htmlEntities.put("gamma", new Integer(947));
    htmlEntities.put("delta", new Integer(948));
    htmlEntities.put("epsilon", new Integer(949));
    htmlEntities.put("zeta", new Integer(950));
    htmlEntities.put("eta", new Integer(951));
    htmlEntities.put("theta", new Integer(952));
    htmlEntities.put("iota", new Integer(953));
    htmlEntities.put("kappa", new Integer(954));
    htmlEntities.put("lambda", new Integer(955));
    htmlEntities.put("mu", new Integer(956));
    htmlEntities.put("nu", new Integer(957));
    htmlEntities.put("xi", new Integer(958));
    htmlEntities.put("omicron", new Integer(959));
    htmlEntities.put("pi", new Integer(960));
    htmlEntities.put("rho", new Integer(961));
    htmlEntities.put("sigmaf", new Integer(962));
    htmlEntities.put("sigma", new Integer(963));
    htmlEntities.put("tau", new Integer(964));
    htmlEntities.put("upsilon", new Integer(965));
    htmlEntities.put("phi", new Integer(966));
    htmlEntities.put("chi", new Integer(967));
    htmlEntities.put("psi", new Integer(968));
    htmlEntities.put("omega", new Integer(969));
    htmlEntities.put("thetasym", new Integer(977));
    htmlEntities.put("upsih", new Integer(978));
    htmlEntities.put("piv", new Integer(982));
    htmlEntities.put("bull", new Integer(8226));
    htmlEntities.put("hellip", new Integer(8230));
    htmlEntities.put("prime", new Integer(8242));
    htmlEntities.put("Prime", new Integer(8243));
    htmlEntities.put("oline", new Integer(8254));
    htmlEntities.put("frasl", new Integer(8260));
    htmlEntities.put("weierp", new Integer(8472));
    htmlEntities.put("image", new Integer(8465));
    htmlEntities.put("real", new Integer(8476));
    htmlEntities.put("trade", new Integer(8482));
    htmlEntities.put("alefsym", new Integer(8501));
    htmlEntities.put("larr", new Integer(8592));
    htmlEntities.put("uarr", new Integer(8593));
    htmlEntities.put("rarr", new Integer(8594));
    htmlEntities.put("darr", new Integer(8595));
    htmlEntities.put("harr", new Integer(8596));
    htmlEntities.put("crarr", new Integer(8629));
    htmlEntities.put("lArr", new Integer(8656));
    htmlEntities.put("uArr", new Integer(8657));
    htmlEntities.put("rArr", new Integer(8658));
    htmlEntities.put("dArr", new Integer(8659));
    htmlEntities.put("hArr", new Integer(8660));
    htmlEntities.put("forall", new Integer(8704));
    htmlEntities.put("part", new Integer(8706));
    htmlEntities.put("exist", new Integer(8707));
    htmlEntities.put("empty", new Integer(8709));
    htmlEntities.put("nabla", new Integer(8711));
    htmlEntities.put("isin", new Integer(8712));
    htmlEntities.put("notin", new Integer(8713));
    htmlEntities.put("ni", new Integer(8715));
    htmlEntities.put("prod", new Integer(8719));
    htmlEntities.put("sum", new Integer(8721));
    htmlEntities.put("minus", new Integer(8722));
    htmlEntities.put("lowast", new Integer(8727));
    htmlEntities.put("radic", new Integer(8730));
    htmlEntities.put("prop", new Integer(8733));
    htmlEntities.put("infin", new Integer(8734));
    htmlEntities.put("ang", new Integer(8736));
    htmlEntities.put("and", new Integer(8743));
    htmlEntities.put("or", new Integer(8744));
    htmlEntities.put("cap", new Integer(8745));
    htmlEntities.put("cup", new Integer(8746));
    htmlEntities.put("int", new Integer(8747));
    htmlEntities.put("there4", new Integer(8756));
    htmlEntities.put("sim", new Integer(8764));
    htmlEntities.put("cong", new Integer(8773));
    htmlEntities.put("asymp", new Integer(8776));
    htmlEntities.put("ne", new Integer(8800));
    htmlEntities.put("equiv", new Integer(8801));
    htmlEntities.put("le", new Integer(8804));
    htmlEntities.put("ge", new Integer(8805));
    htmlEntities.put("sub", new Integer(8834));
    htmlEntities.put("sup", new Integer(8835));
    htmlEntities.put("nsub", new Integer(8836));
    htmlEntities.put("sube", new Integer(8838));
    htmlEntities.put("supe", new Integer(8839));
    htmlEntities.put("oplus", new Integer(8853));
    htmlEntities.put("otimes", new Integer(8855));
    htmlEntities.put("perp", new Integer(8869));
    htmlEntities.put("sdot", new Integer(8901));
    htmlEntities.put("lceil", new Integer(8968));
    htmlEntities.put("rceil", new Integer(8969));
    htmlEntities.put("lfloor", new Integer(8970));
    htmlEntities.put("rfloor", new Integer(8971));
    htmlEntities.put("lang", new Integer(9001));
    htmlEntities.put("rang", new Integer(9002));
    htmlEntities.put("loz", new Integer(9674));
    htmlEntities.put("spades", new Integer(9824));
    htmlEntities.put("clubs", new Integer(9827));
    htmlEntities.put("hearts", new Integer(9829));
    htmlEntities.put("diams", new Integer(9830));
    htmlEntities.put("quot", new Integer(34));
    htmlEntities.put("amp", new Integer(38));
    htmlEntities.put("lt", new Integer(60));
    htmlEntities.put("gt", new Integer(62));
    htmlEntities.put("OElig", new Integer(338));
    htmlEntities.put("oelig", new Integer(339));
    htmlEntities.put("Scaron", new Integer(352));
    htmlEntities.put("scaron", new Integer(353));
    htmlEntities.put("Yuml", new Integer(376));
    htmlEntities.put("circ", new Integer(710));
    htmlEntities.put("tilde", new Integer(732));
    htmlEntities.put("ensp", new Integer(8194));
    htmlEntities.put("emsp", new Integer(8195));
    htmlEntities.put("thinsp", new Integer(8201));
    htmlEntities.put("zwnj", new Integer(8204));
    htmlEntities.put("zwj", new Integer(8205));
    htmlEntities.put("lrm", new Integer(8206));
    htmlEntities.put("rlm", new Integer(8207));
    htmlEntities.put("ndash", new Integer(8211));
    htmlEntities.put("mdash", new Integer(8212));
    htmlEntities.put("lsquo", new Integer(8216));
    htmlEntities.put("rsquo", new Integer(8217));
    htmlEntities.put("sbquo", new Integer(8218));
    htmlEntities.put("ldquo", new Integer(8220));
    htmlEntities.put("rdquo", new Integer(8221));
    htmlEntities.put("bdquo", new Integer(8222));
    htmlEntities.put("dagger", new Integer(8224));
    htmlEntities.put("Dagger", new Integer(8225));
    htmlEntities.put("permil", new Integer(8240));
    htmlEntities.put("lsaquo", new Integer(8249));
    htmlEntities.put("rsaquo", new Integer(8250));
    htmlEntities.put("euro", new Integer(8364));
  }

  /**
   * Turn any HTML escape entities in the string into
   * characters and return the resulting string.
   *
   * @param s String to be unescaped.
   * @return unescaped String.
   * @throws NullPointerException if s is null.
   *
   * @since ostermillerutils 1.00.00
   */
  public static String unescapeHTML(String s){
	if (s == null) return s;
    StringBuffer result = new StringBuffer(s.length());
    int ampInd = s.indexOf("&");
    int lastEnd = 0;
    while (ampInd >= 0){
      int nextAmp = s.indexOf("&", ampInd+1);
      int nextSemi = s.indexOf(";", ampInd+1);
      if (nextSemi != -1 && (nextAmp == -1 || nextSemi < nextAmp)){
        int value = -1;
        String escape = s.substring(ampInd+1,nextSemi);
        try {
          if (escape.startsWith("#")){
            value = Integer.parseInt(escape.substring(1), 10);
          } else {
            if (htmlEntities.containsKey(escape)){
              value = ((Integer)(htmlEntities.get(escape))).intValue();
            }
          }
        } catch (NumberFormatException x){
        }
        result.append(s.substring(lastEnd, ampInd));
        lastEnd = nextSemi + 1;
        if (value >= 0 && value <= 0xffff){
          result.append((char)value);
        } else {
          result.append("&").append(escape).append(";");
        }
      }
      ampInd = nextAmp;
    }
    result.append(s.substring(lastEnd));
    return result.toString();
  }
}