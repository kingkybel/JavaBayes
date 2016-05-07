/* XMLBIFv02.java */
/* Generated By:JavaCC: Do not edit this line. XMLBIFv02.java */
package Parsers.XMLBIFv02;

import InterchangeFormat.*;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Definition of the Interchange Format class and its
 * variables. The IFBayesNet ifbn contains the
 * parsed Bayesian network.
 */
public class XMLBIFv02 extends InterchangeFormat implements XMLBIFv02Constants {
    IFBayesNet ifbn;

    /**
     * Retrieve the bayes-net interface.
     */
    public IFBayesNet get_ifbn()
    {
        return(ifbn);
    }

    String pcdata() throws ParseException
    {
        StringBuffer p = new StringBuffer("");
        Token t;
        while (true)
        {
            t = getToken(1);
            if ((t.kind == 0) || (t.kind == SOT) || (t.kind == EOT))
            {
                break;
            }
            else
            {
                p.append(t.image);
                getNextToken();
            }
        }
        return(p.toString());
    }

    void glob_heading() throws ParseException
    {
        Token t;
        while (true)
        {
            t = getToken(1);
            if (t.kind == 0)
            {
                break;
            }
            else
            {
                if (t.kind == SOT)
                {
                    getNextToken(); t = getToken(1);
                    if (t.kind == BIF)
                    {
                        getNextToken(); t = getToken(1);
                        if (t.kind == CT)
                        {
                            getNextToken();
                            break;
                        }
                    }
                    else
                    {
                        getNextToken();
                    }
                }
                else
                {
                    getNextToken();
                }
            }
            getNextToken();
        }
    }

//
// THE INTERCHANGE FORMAT GRAMMAR STARTS HERE.
//

/**
 * Basic parsing function. First looks for a Network Declaration,
 * then looks for an arbitrary number of VariableDeclaration or
 * ProbabilityDeclaration non-terminals. The objects are
 * in the vectors ifbn.pvs and ifbn.upfs.
 */
  final public void CompilationUnit() throws ParseException {IFProbabilityVariable pv;
    IFProbabilityFunction upf;
    OpenTag();
glob_heading();
    NetworkDeclaration();
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case SOT:{
        ;
        break;
        }
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      jj_consume_token(SOT);
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case VARIABLE:{
        pv = VariableDeclaration();
ifbn.add(pv);
        break;
        }
      case PROBABILITY:{
        upf = ProbabilityDeclaration();
ifbn.add(upf);
        break;
        }
      default:
        jj_la1[1] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    jj_consume_token(EOT);
    jj_consume_token(NETWORK);
    jj_consume_token(CT);
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case EOT:{
      jj_consume_token(EOT);
      jj_consume_token(BIF);
      jj_consume_token(CT);
      break;
      }
    case 0:{
      jj_consume_token(0);
      break;
      }
    default:
      jj_la1[2] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final public void OpenTag() throws ParseException {
    jj_consume_token(OPENTAG);
  }

/**
 * Detect and initialize the network.
 */
  final public void NetworkDeclaration() throws ParseException {String s, ss;
    ArrayList properties = new ArrayList();
    jj_consume_token(SOT);
    jj_consume_token(NETWORK);
    jj_consume_token(CT);
    jj_consume_token(SOT);
    jj_consume_token(NAME);
    s = getString();
    jj_consume_token(EOT);
    jj_consume_token(NAME);
    jj_consume_token(CT);
    label_2:
    while (true) {
      if (jj_2_1(2)) {
        ;
      } else {
        break label_2;
      }
      ss = Property();
properties.add(ss);
    }
ifbn = new IFBayesNet(s, properties);
  }

/**
 * Detect a variable declaration.
 */
  final public IFProbabilityVariable VariableDeclaration() throws ParseException {String s;
    IFProbabilityVariable pv;
    jj_consume_token(VARIABLE);
    jj_consume_token(CT);
    s = ProbabilityVariableName();
    ProbabilityVariableType();
    pv = VariableContent(s);
    jj_consume_token(EOT);
    jj_consume_token(VARIABLE);
    jj_consume_token(CT);
{if ("" != null) return(pv);}
    throw new Error("Missing return statement in function");
  }

  final public String ProbabilityVariableName() throws ParseException {String s;
    jj_consume_token(SOT);
    jj_consume_token(NAME);
    s = getString();
    jj_consume_token(EOT);
    jj_consume_token(NAME);
    jj_consume_token(CT);
{if ("" != null) return(s);}
    throw new Error("Missing return statement in function");
  }

  final public void ProbabilityVariableType() throws ParseException {String values[] = null;
    jj_consume_token(SOT);
    jj_consume_token(TYPE);
    jj_consume_token(CT);
    jj_consume_token(DISCRETE);
    jj_consume_token(EOT);
    jj_consume_token(TYPE);
    jj_consume_token(CT);
  }

  final public IFProbabilityVariable VariableContent(String name) throws ParseException {int i;
    String s, v, svalues[];
    ArrayList properties = new ArrayList();
    ArrayList values = new ArrayList();
    Iterator e;
    IFProbabilityVariable pv = new IFProbabilityVariable();
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case SOT:{
        ;
        break;
        }
      default:
        jj_la1[3] = jj_gen;
        break label_3;
      }
      if (jj_2_2(2)) {
        s = Property();
properties.add(s);
      } else {
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case SOT:{
          v = VariableValue();
values.add(v);
          break;
          }
        default:
          jj_la1[4] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
      }
    }
pv.set_name(name);
        pv.set_properties(properties);
        svalues = new String[ values.size() ];
        for (e = values.iterator(), i = 0; e.hasNext(); i++)
        {
            svalues[i] = (String)(e.next());
            pv.set_values(svalues);
            {if ("" != null) return(pv);}
        }
    throw new Error("Missing return statement in function");
  }

  final public String VariableValue() throws ParseException {String s;
    jj_consume_token(SOT);
    jj_consume_token(VALUE);
    s = getString();
    jj_consume_token(EOT);
    jj_consume_token(VALUE);
    jj_consume_token(CT);
{if ("" != null) return(s);}
    throw new Error("Missing return statement in function");
  }

/**
 * Detect a probability declaration.
 */
  final public IFProbabilityFunction ProbabilityDeclaration() throws ParseException {String vs[];
    IFProbabilityFunction upf = new IFProbabilityFunction();
    jj_consume_token(PROBABILITY);
    jj_consume_token(CT);
    ProbabilityContent(upf);
    jj_consume_token(EOT);
    jj_consume_token(PROBABILITY);
    jj_consume_token(CT);
{if ("" != null) return(upf);}
    throw new Error("Missing return statement in function");
  }

  final public void ProbabilityContent(IFProbabilityFunction upf) throws ParseException {int i, j;
    double def[] = null;
    double tab[] = null;
    String s, vs[];
    IFProbabilityEntry entry = null;
    Iterator e;

    ArrayList fors = new ArrayList();
    ArrayList givens = new ArrayList();
    ArrayList properties = new ArrayList();
    ArrayList entries = new ArrayList();
    ArrayList defaults = new ArrayList();
    ArrayList tables = new ArrayList();
    label_4:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case SOT:{
        ;
        break;
        }
      default:
        jj_la1[5] = jj_gen;
        break label_4;
      }
      jj_consume_token(SOT);
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case FOR:{
        s = ProbabilityFor();
fors.add(s);
        break;
        }
      case GIVEN:{
        s = ProbabilityGiven();
givens.add(s);
        break;
        }
      case SOT:{
        s = Property();
properties.add(s);
        break;
        }
      case DEFAUL:{
        def = ProbabilityDefault();
defaults.add(def);
        break;
        }
      case ENTRY:{
        entry = ProbabilityEntry();
entries.add(entry);
        break;
        }
      case TABLE:{
        tab = ProbabilityTable();
tables.add(tab);
        break;
        }
      default:
        jj_la1[6] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
upf.set_properties(properties);
            upf.set_defaults(defaults);
            upf.set_entries(entries);
            upf.set_tables(tables);
            upf.set_conditional_index(fors.size());
            vs = new String[ fors.size() + givens.size() ];
            for (e = fors.iterator(), i = 0; e.hasNext(); i++)
            {
                vs[i] = (String)(e.next());
            }
            for (e = givens.iterator(), j = i; e.hasNext(); j++)
            {
                vs[j] = (String)(e.next());
            }
            upf.set_variables(vs);
  }

  final public String ProbabilityFor() throws ParseException {String s;
    jj_consume_token(FOR);
    s = getString();
    jj_consume_token(EOT);
    jj_consume_token(FOR);
    jj_consume_token(CT);
{if ("" != null) return(s);}
    throw new Error("Missing return statement in function");
  }

  final public String ProbabilityGiven() throws ParseException {String s;
    jj_consume_token(GIVEN);
    s = getString();
    jj_consume_token(EOT);
    jj_consume_token(GIVEN);
    jj_consume_token(CT);
{if ("" != null) return(s);}
    throw new Error("Missing return statement in function");
  }

  final public IFProbabilityEntry ProbabilityEntry() throws ParseException {int i;
    Iterator e;
    String variable_name, vs[];
    ArrayList v_list = new ArrayList();
    double d[];
    jj_consume_token(ENTRY);
    jj_consume_token(CT);
    label_5:
    while (true) {
      if (jj_2_3(2)) {
        ;
      } else {
        break label_5;
      }
      jj_consume_token(SOT);
      jj_consume_token(VALUE);
      variable_name = getString();
      jj_consume_token(EOT);
      jj_consume_token(VALUE);
      jj_consume_token(CT);
v_list.add(variable_name);
    }
    d = ProbabilityTable();
    jj_consume_token(EOT);
    jj_consume_token(ENTRY);
    jj_consume_token(CT);
vs = new String[v_list.size()];
        for (e=v_list.iterator(), i=0; e.hasNext(); i++)
        {
            vs[i] = (String)(e.next());
        }
        {if ("" != null) return( new IFProbabilityEntry(vs, d) );}
    throw new Error("Missing return statement in function");
  }

  final public double[] ProbabilityDefault() throws ParseException {double d[];
    jj_consume_token(DEFAUL);
    jj_consume_token(CT);
    d = FloatingPointList();
    jj_consume_token(EOT);
    jj_consume_token(DEFAUL);
    jj_consume_token(CT);
{if ("" != null) return(d);}
    throw new Error("Missing return statement in function");
  }

  final public double[] ProbabilityTable() throws ParseException {double d[];
    jj_consume_token(TABLE);
    jj_consume_token(CT);
    d = FloatingPointList();
    jj_consume_token(EOT);
    jj_consume_token(TABLE);
    jj_consume_token(CT);
{if ("" != null) return(d);}
    throw new Error("Missing return statement in function");
  }

//
// Some general purpose non-terminals.
//

/**
 * Pick a list of non-negative floating numbers.
 */
  final public double[] FloatingPointList() throws ParseException {int i;
    Double d;
    double ds[];
    ArrayList d_list = new ArrayList();
    Iterator e;
    d = FloatingPointNumber();
d_list.add(d);
    label_6:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case NON_NEGATIVE_NUMBER:{
        ;
        break;
        }
      default:
        jj_la1[7] = jj_gen;
        break label_6;
      }
      d = FloatingPointNumber();
d_list.add(d);
    }
ds = new double[d_list.size()];
        for (e=d_list.iterator(), i=0; e.hasNext(); i++)
        {
            d = (Double)(e.next());
            ds[i] = d.doubleValue();
        }
        {if ("" != null) return(ds);}
    throw new Error("Missing return statement in function");
  }

/**
 * Pick a non-negative floating number; necessary to allow
 * ignored characters and comments to exist in the middle
 * of a FloatingPointList().
 */
  final public Double FloatingPointNumber() throws ParseException {Token t;
    t = jj_consume_token(NON_NEGATIVE_NUMBER);
{if ("" != null) return( Double.valueOf(t.image) );}
    throw new Error("Missing return statement in function");
  }

/**
 * Property definition.
 */
  final public String Property() throws ParseException {String s;
    jj_consume_token(SOT);
    jj_consume_token(PROPERTY);
    s = getString();
    jj_consume_token(EOT);
    jj_consume_token(PROPERTY);
    jj_consume_token(CT);
{if ("" != null) return(s);}
    throw new Error("Missing return statement in function");
  }

/**
 * Retrieve String.
 */
  final public String getString() throws ParseException {
    jj_consume_token(CT);
{if ("" != null) return( pcdata() );}
    throw new Error("Missing return statement in function");
  }

  private boolean jj_2_1(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  private boolean jj_2_2(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_2(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(1, xla); }
  }

  private boolean jj_2_3(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_3(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(2, xla); }
  }

  private boolean jj_3_3()
 {
    if (jj_scan_token(SOT)) return true;
    if (jj_scan_token(VALUE)) return true;
    return false;
  }

  private boolean jj_3_2()
 {
    if (jj_3R_7()) return true;
    return false;
  }

  private boolean jj_3R_7()
 {
    if (jj_scan_token(SOT)) return true;
    if (jj_scan_token(PROPERTY)) return true;
    return false;
  }

  private boolean jj_3_1()
 {
    if (jj_3R_7()) return true;
    return false;
  }

  /** Generated Token Manager. */
  public XMLBIFv02TokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  private int jj_gen;
  final private int[] jj_la1 = new int[8];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x8,0x84000,0x11,0x8,0x8,0x8,0x10f08,0x200000,};
   }
  final private JJCalls[] jj_2_rtns = new JJCalls[3];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  /** Constructor with InputStream. */
  public XMLBIFv02(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public XMLBIFv02(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new XMLBIFv02TokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 8; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 8; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor. */
  public XMLBIFv02(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new XMLBIFv02TokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 8; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 8; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor with generated Token Manager. */
  public XMLBIFv02(XMLBIFv02TokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 8; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(XMLBIFv02TokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 8; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  @SuppressWarnings("serial")
  static private final class LookaheadSuccess extends java.lang.Error { }
  final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk_f() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      jj_entries_loop: for (java.util.Iterator<?> it = jj_expentries.iterator(); it.hasNext();) {
        int[] oldentry = (int[])(it.next());
        if (oldentry.length == jj_expentry.length) {
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              continue jj_entries_loop;
            }
          }
          jj_expentries.add(jj_expentry);
          break jj_entries_loop;
        }
      }
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[24];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 8; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 24; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

  private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 3; i++) {
    try {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
            case 1: jj_3_2(); break;
            case 2: jj_3_3(); break;
          }
        }
        p = p.next;
      } while (p != null);
      } catch(LookaheadSuccess ls) { }
    }
    jj_rescan = false;
  }

  private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}
